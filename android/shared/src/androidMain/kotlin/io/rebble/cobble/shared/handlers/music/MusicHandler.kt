package io.rebble.cobble.shared.handlers.music

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.SystemClock
import android.view.KeyEvent
import androidx.lifecycle.asFlow
import io.rebble.cobble.shared.domain.PermissionChangeBus
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.shared.domain.notificationPermissionFlow
import io.rebble.cobble.shared.handlers.CobbleHandler
import io.rebble.libpebblecommon.packets.MusicControl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import kotlin.math.roundToInt

class MusicHandler(private val pebbleDevice: PebbleDevice): CobbleHandler, KoinComponent {
    private val context: Context by inject()
    private val packageManager: PackageManager by inject()
    private val activeMediaSessionProvider = ActiveMediaSessionProvider()
    private val musicService = pebbleDevice.musicService

    private var currentMediaController: MediaController? = null
    private var hasPermission: Boolean = false

    private val musicControl = MutableSharedFlow<MusicControl>(4)

    init {
        pebbleDevice.negotiationScope.launch {
            val connectionScope = pebbleDevice.connectionScope.filterNotNull().first()

            musicControl.filterIsInstance<MusicControl.UpdateCurrentTrack>().debounce(200).onEach {
                Timber.d("Update current track %s %s %s %s", it.title.get(), it.artist.get(), it.album.get(), it.trackLength.get())
                musicService.send(it)
            }.launchIn(connectionScope)

            musicControl.filterIsInstance<MusicControl.UpdatePlayStateInfo>().debounce(200).onEach {
                Timber.d("Update play state %s %s %s", it.state.get(), it.trackPosition.get(), it.playRate.get())
                musicService.send(it)
            }.launchIn(connectionScope)

            musicControl.filterIsInstance<MusicControl.UpdateVolumeInfo>().debounce(200).onEach {
                Timber.d("Update volume %s", it.volumePercent.get())
                musicService.send(it)
            }.launchIn(connectionScope)
            musicControl.filterIsInstance<MusicControl.UpdatePlayerInfo>().debounce(200).onEach {
                Timber.d("Update player info %s %s", it.name.get(), it.pkg.get())
                musicService.send(it)
            }.launchIn(connectionScope)
        }
    }

    private fun onMediaPlayerChanged(newPlayer: MediaController?) {
        Timber.d("New Player %s %s", newPlayer?.packageName, newPlayer.hashCode())

        disposeCurrentMediaController()
        this.currentMediaController = newPlayer

        if (newPlayer == null) {
            return
        }

        newPlayer.registerCallback(callback)

        sendPlayerInfoUpdate(newPlayer)
        sendCurrentTrackUpdate(newPlayer.metadata)
        sendPlayStateUpdate(newPlayer.playbackState)
        newPlayer.playbackInfo?.let { sendVolumeUpdate(it) }
    }

    private fun sendPlayerInfoUpdate(mediaController: MediaController) {
        val name = packageManager
                .getPackageInfo(mediaController.packageName, 0)
                .applicationInfo
                .loadLabel(packageManager)
                .toString()

        if (!musicControl.tryEmit(MusicControl.UpdatePlayerInfo(name, mediaController.packageName))) {
            Timber.w("Failed to emit player info")
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
        Timber.d("Artist present: %s, Album present: %s, Title present: %s",
                metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) != null,
                metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) != null,
                metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) != null)

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
                Timber.d("No metadata")
                MusicControl.UpdateCurrentTrack(
                        "",
                        "",
                        ""
                )
            }
        }

        if (!musicControl.tryEmit(updateTrackObject)) {
            Timber.w("Failed to emit track update")
        }
    }


    private fun sendVolumeUpdate(playbackInfo: MediaController.PlaybackInfo) {
        Timber.d("Send volume update %s", playbackInfo)
        val packet = MusicControl.UpdateVolumeInfo(
                (100f * playbackInfo.currentVolume / playbackInfo.maxVolume)
                        .roundToInt()
                        .toUByte()
        )
        if (!musicControl.tryEmit(packet)) {
            Timber.w("Failed to emit volume update")
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
        val packet = MusicControl.UpdatePlayStateInfo(
                state,
                position.toUInt(),
                (playbackSpeed * 100f).roundToInt().toUInt(),
                MusicControl.ShuffleState.Unknown,
                MusicControl.RepeatState.Unknown
        )
        if (!musicControl.tryEmit(packet)) {
            Timber.w("Failed to emit play state update")
        }
    }

    private fun listenForPlayerChanges(connectionScope: CoroutineScope) {
        connectionScope.launch(Dispatchers.Main.immediate) {
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

    private fun listenForIncomingMessages(connectionScope: CoroutineScope) {
        connectionScope.launch(Dispatchers.Main.immediate) {
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
                        Timber.d("Watch requested playback info")
                        currentMediaController?.let { sendPlayerInfoUpdate(it) }
                        sendPlayStateUpdate(currentMediaController?.playbackState)
                        currentMediaController?.playbackInfo?.let { sendVolumeUpdate(it) }
                        sendCurrentTrackUpdate(currentMediaController?.metadata)
                    }

                    else -> {
                        Timber.w("Unknown message %s", msg.message)
                    }
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
        pebbleDevice.negotiationScope.launch {
            val connectionScope = pebbleDevice.connectionScope.filterNotNull().first()
            listenForIncomingMessages(connectionScope)
            listenForPlayerChanges(connectionScope)
            connectionScope.coroutineContext.job.invokeOnCompletion {
                disposeCurrentMediaController()
            }
        }
    }
}