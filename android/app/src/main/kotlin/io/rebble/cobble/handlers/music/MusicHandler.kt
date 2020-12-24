package io.rebble.cobble.handlers.music

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.SystemClock
import android.view.KeyEvent
import androidx.lifecycle.asFlow
import io.rebble.cobble.handlers.PebbleMessageHandler
import io.rebble.cobble.notifications.NotificationListener
import io.rebble.libpebblecommon.packets.MusicControl
import io.rebble.libpebblecommon.services.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

class MusicHandler @Inject constructor(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val musicService: MusicService,
        private val activeMediaSessionProvider: ActiveMediaSessionProvider,
        private val packageManager: PackageManager
) : PebbleMessageHandler {
    private var currentMediaController: MediaController? = null

    private fun onMediaPlayerChanged(newPlayer: MediaController?) {
        Timber.d("New Player %s", newPlayer?.packageName)

        disposeCurrentMediaController()
        this.currentMediaController = newPlayer

        if (newPlayer == null) {
            return
        }

        newPlayer.registerCallback(callback)

        coroutineScope.launch(Dispatchers.Main.immediate) {
            val name = packageManager
                    .getPackageInfo(newPlayer.packageName, 0)
                    .applicationInfo
                    .loadLabel(packageManager)
                    .toString()

            musicService.send(MusicControl.UpdatePlayerInfo(
                    newPlayer.packageName,
                    name
            ))

            sendCurrentTrackUpdate()
            sendPlayStateUpdate()
            sendVolumeUpdate()
        }
    }

    private fun disposeCurrentMediaController() {
        Timber.d("Dispose %s", currentMediaController?.packageName)
        currentMediaController?.unregisterCallback(callback)
        currentMediaController = null
    }

    private fun beginPlayback() {
        val currentMediaController = currentMediaController
        Timber.d("Begin playback %s", currentMediaController?.packageName)
        if (currentMediaController != null) {
            currentMediaController.transportControls.play()
        } else {
            // Simulate play button to start playback of the last active app
            val audioService = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioService.dispatchMediaKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY)
            )
            audioService.dispatchMediaKeyEvent(
                    KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY)
            )
        }
    }

    private fun sendCurrentTrackUpdate() {
        Timber.d("Send track %s %s", currentMediaController, currentMediaController?.metadata?.keySet()?.toList())

        val metadata = currentMediaController?.metadata ?: return

        coroutineScope.launch(Dispatchers.Main.immediate) {
            musicService.send(MusicControl.UpdateCurrentTrack(
                    metadata.getString(MediaMetadata.METADATA_KEY_ARTIST),
                    metadata.getString(MediaMetadata.METADATA_KEY_ALBUM),
                    metadata.getString(MediaMetadata.METADATA_KEY_TITLE),
                    metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).toInt(),
                    metadata.getLong(MediaMetadata.METADATA_KEY_NUM_TRACKS).toInt(),
                    metadata.getLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER).toInt()
            ))
        }
    }

    private fun sendVolumeUpdate() {
        Timber.d("Send volume %s", currentMediaController)

        val playbackInfo = currentMediaController?.playbackInfo ?: return

        coroutineScope.launch(Dispatchers.Main.immediate) {
            musicService.send(MusicControl.UpdateVolumeInfo(
                    (100f * playbackInfo.currentVolume / playbackInfo.maxVolume)
                            .roundToInt()
                            .toUByte()
            ))
        }

    }

    private fun sendPlayStateUpdate() {
        Timber.d("Send play state %s %s", currentMediaController, currentMediaController?.playbackState)
        val playbackState = currentMediaController?.playbackState ?: return

        val state = when (playbackState.state) {
            PlaybackState.STATE_PLAYING ->
                MusicControl.PlaybackState.Playing
            PlaybackState.STATE_REWINDING,
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS ->
                MusicControl.PlaybackState.Rewinding
            PlaybackState.STATE_FAST_FORWARDING,
            PlaybackState.STATE_SKIPPING_TO_NEXT -> MusicControl.PlaybackState.FastForwarding
            PlaybackState.STATE_PAUSED -> MusicControl.PlaybackState.Paused
            else -> MusicControl.PlaybackState.Unknown
        }

        val timeSinceLastPositionUpdate = SystemClock.elapsedRealtime() -
                playbackState.lastPositionUpdateTime
        val position = playbackState.position + timeSinceLastPositionUpdate

        coroutineScope.launch(Dispatchers.Main.immediate) {
            musicService.send(MusicControl.UpdatePlayStateInfo(
                    state,
                    position.toUInt(),
                    (playbackState.playbackSpeed * 100f).roundToInt().toUInt(),
                    MusicControl.ShuffleState.Unknown,
                    MusicControl.RepeatState.Unknown
            ))
        }

    }

    private fun listenForPlayerChanges() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            @Suppress("EXPERIMENTAL_API_USAGE")
            NotificationListener.isActive.flatMapLatest { notificationServiceActive ->
                if (notificationServiceActive) {
                    activeMediaSessionProvider.asFlow()
                } else {
                    flowOf(null)
                }
            }.collect {
                onMediaPlayerChanged(it)
            }
        }
    }

    private fun listenForIncomingMessages() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            for (msg in musicService.receivedMessages) {
                Timber.d("Received music packet %s %s", msg.message, currentMediaController?.packageName)
                when (msg.message) {
                    MusicControl.Message.PlayPause -> {
                        if (currentMediaController?.isPlaying() == true) {
                            currentMediaController?.transportControls?.pause()
                        } else {
                            beginPlayback()
                        }
                    }
                    MusicControl.Message.Pause -> {
                        beginPlayback()
                    }
                    MusicControl.Message.Play -> {
                        currentMediaController?.transportControls?.pause()
                    }
                    MusicControl.Message.NextTrack -> {
                        currentMediaController?.transportControls?.skipToNext()
                    }
                    MusicControl.Message.PreviousTrack -> {
                        currentMediaController?.transportControls?.skipToPrevious()
                    }
                    MusicControl.Message.VolumeUp -> {
                        currentMediaController?.adjustVolume(AudioManager.ADJUST_RAISE, 0)
                        sendVolumeUpdate()
                    }
                    MusicControl.Message.VolumeDown -> {
                        currentMediaController?.adjustVolume(AudioManager.ADJUST_LOWER, 0)
                        sendVolumeUpdate()
                    }
                    MusicControl.Message.GetCurrentTrack -> sendCurrentTrackUpdate()
                    MusicControl.Message.UpdateCurrentTrack,
                    MusicControl.Message.UpdatePlayStateInfo,
                    MusicControl.Message.UpdateVolumeInfo,
                    MusicControl.Message.UpdatePlayerInfo,
                    -> Unit
                }
            }
        }
    }

    private val callback = object : MediaController.Callback() {
        override fun onAudioInfoChanged(info: MediaController.PlaybackInfo) {
            sendVolumeUpdate()
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            sendPlayStateUpdate()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            sendCurrentTrackUpdate()
        }
    }

    init {
        listenForIncomingMessages()
        listenForPlayerChanges()

        coroutineScope.coroutineContext.job.invokeOnCompletion {
            disposeCurrentMediaController()
        }
    }
}