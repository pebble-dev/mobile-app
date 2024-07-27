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
import io.rebble.cobble.datasources.PermissionChangeBus
import io.rebble.cobble.datasources.notificationPermissionFlow
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.cobble.util.Debouncer
import io.rebble.libpebblecommon.packets.MusicControl
import io.rebble.libpebblecommon.services.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
) : CobbleHandler {
    private var currentMediaController: MediaController? = null
    private var hasPermission: Boolean = false

    private val playStateDebouncer = Debouncer(
            debouncingTimeMs = 500L,
            triggerFirstImmediately = true,
            scope = coroutineScope
    )

    private val trackDebouncer = Debouncer(
            debouncingTimeMs = 500L,
            triggerFirstImmediately = true,
            scope = coroutineScope
    )

    private val volumDebouncer = Debouncer(
            debouncingTimeMs = 500L,
            triggerFirstImmediately = true,
            scope = coroutineScope
    )

    private fun onMediaPlayerChanged(newPlayer: MediaController?) {
        Timber.d("New Player %s %s", newPlayer?.packageName, newPlayer.hashCode())

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

            sendCurrentTrackUpdate(newPlayer.metadata)
            sendPlayStateUpdate(newPlayer.playbackState)
            newPlayer.playbackInfo?.let { sendVolumeUpdate(it) }
        }
    }

    private fun disposeCurrentMediaController() {
        Timber.d("Dispose %s %s", currentMediaController?.packageName, currentMediaController?.hashCode())
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

    private fun sendCurrentTrackUpdate(metadata: MediaMetadata?) {
        Timber.d("Send track %s", metadata?.keySet()?.toList())

        val updateTrackObject = when {
            !hasPermission -> {
                MusicControl.UpdateCurrentTrack(
                        "No permission",
                        "",
                        "Check Rebble app"
                )
            }

            metadata != null -> {
                MusicControl.UpdateCurrentTrack(
                        metadata.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "",
                        metadata.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: "",
                        metadata.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "",
                        metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).toInt(),
                        metadata.getLong(MediaMetadata.METADATA_KEY_NUM_TRACKS).toInt(),
                        metadata.getLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER).toInt()
                )
            }

            else -> {
                MusicControl.UpdateCurrentTrack(
                        "",
                        "",
                        ""
                )
            }
        }

        trackDebouncer.executeDebouncing {
            Timber.d("transmit track")
            musicService.send(updateTrackObject)
        }
    }


    private fun sendVolumeUpdate(playbackInfo: MediaController.PlaybackInfo) {
        Timber.d("Send volume update %s", playbackInfo)
        volumDebouncer.executeDebouncing {
            Timber.d("Transmit volume")
            musicService.send(MusicControl.UpdateVolumeInfo(
                    (100f * playbackInfo.currentVolume / playbackInfo.maxVolume)
                            .roundToInt()
                            .toUByte()
            ))
        }

    }

    private fun sendPlayStateUpdate(playbackState: PlaybackState?) {
        Timber.d("Send play state %s", playbackState)

        val state = when (playbackState?.state) {
            PlaybackState.STATE_PLAYING,
            PlaybackState.STATE_BUFFERING ->
                MusicControl.PlaybackState.Playing

            PlaybackState.STATE_REWINDING,
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS ->
                MusicControl.PlaybackState.Rewinding

            PlaybackState.STATE_FAST_FORWARDING,
            PlaybackState.STATE_SKIPPING_TO_NEXT -> MusicControl.PlaybackState.FastForwarding

            PlaybackState.STATE_PAUSED,
            PlaybackState.STATE_STOPPED -> MusicControl.PlaybackState.Paused

            else -> MusicControl.PlaybackState.Unknown
        }

        val timeSinceLastPositionUpdate = SystemClock.elapsedRealtime() -
                (playbackState?.lastPositionUpdateTime ?: SystemClock.elapsedRealtime())
        val position = (playbackState?.position ?: 0) + timeSinceLastPositionUpdate

        val playbackSpeed = playbackState?.playbackSpeed ?: 1f
        playStateDebouncer.executeDebouncing {
            Timber.d("Transmit play state")
            musicService.send(MusicControl.UpdatePlayStateInfo(
                    state,
                    position.toUInt(),
                    (playbackSpeed * 100f).roundToInt().toUInt(),
                    MusicControl.ShuffleState.Unknown,
                    MusicControl.RepeatState.Unknown
            ))
        }
    }

    private fun listenForPlayerChanges() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            @Suppress("EXPERIMENTAL_API_USAGE")
            PermissionChangeBus.notificationPermissionFlow(context)
                    .flatMapLatest { hasNotificationPermission ->
                        this@MusicHandler.hasPermission = hasNotificationPermission

                        if (hasNotificationPermission) {
                            activeMediaSessionProvider.asFlow()
                        } else {
                            sendCurrentTrackUpdate(null)
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
                        currentMediaController?.playbackInfo?.let { sendVolumeUpdate(it) }
                    }

                    MusicControl.Message.VolumeDown -> {
                        currentMediaController?.adjustVolume(AudioManager.ADJUST_LOWER, 0)
                        currentMediaController?.playbackInfo?.let { sendVolumeUpdate(it) }
                    }

                    MusicControl.Message.GetCurrentTrack -> {
                        sendCurrentTrackUpdate(currentMediaController?.metadata)
                    }

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
            sendVolumeUpdate(info)
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            sendPlayStateUpdate(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            sendCurrentTrackUpdate(metadata)
        }

        override fun onSessionDestroyed() {
            Timber.d("Session destroyed")
            disposeCurrentMediaController()
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