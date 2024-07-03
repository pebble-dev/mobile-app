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
import io.rebble.cobble.handlers.CobbleHandler
import io.rebble.cobble.shared.data.MusicTrack
import io.rebble.cobble.shared.data.PlayState
import io.rebble.cobble.shared.domain.music.MusicSync
import io.rebble.cobble.shared.domain.music.PlatformMusicController
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
import kotlin.time.Duration.Companion.milliseconds

class MusicHandler @Inject constructor(
        private val context: Context,
        private val coroutineScope: CoroutineScope,
        private val musicService: MusicService,
        private val activeMediaSessionProvider: ActiveMediaSessionProvider,
        private val packageManager: PackageManager
) : CobbleHandler, PlatformMusicController {
    private var currentMediaController: MediaController? = null
    private var hasPermission: Boolean = false
    //TODO: inject this
    private val musicSync = MusicSync(coroutineScope, musicService, this)

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
                null
            }
        }

        musicSync.updateTrack(metadata?.toMusicTrack())
    }


    private fun sendVolumeUpdate(playbackInfo: MediaController.PlaybackInfo) {
        val volNorm = playbackInfo.getPebbleVolume()
        musicSync.updateVolume(volNorm)
    }

    private fun sendPlayStateUpdate(playbackState: PlaybackState?) {
        Timber.d("Send play state %s", playbackState)

        val timeSinceLastPositionUpdate = SystemClock.elapsedRealtime() -
                (playbackState?.lastPositionUpdateTime ?: SystemClock.elapsedRealtime())

        val playState = playbackState?.toPlayState(timeSinceLastPositionUpdate)
        musicSync.updatePlayState(playState)
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
                            musicSync.updateTrack(null)
                            flowOf(null)
                        }
                    }.collect {
                        onMediaPlayerChanged(it)
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
        listenForPlayerChanges()

        coroutineScope.coroutineContext.job.invokeOnCompletion {
            disposeCurrentMediaController()
        }
    }

    override fun play() {
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

    override fun pause() {
        currentMediaController?.transportControls?.pause()
    }

    override fun skipToNext() {
        currentMediaController?.transportControls?.skipToNext()
    }

    override fun skipToPrevious() {
        currentMediaController?.transportControls?.skipToPrevious()
    }

    override fun volumeUp() {
        currentMediaController?.adjustVolume(AudioManager.ADJUST_RAISE, 0)
    }

    override fun volumeDown() {
        currentMediaController?.adjustVolume(AudioManager.ADJUST_LOWER, 0)
    }

    override val isPlaying: Boolean
        get() = currentMediaController?.isPlaying() == true
    override val currentTrack: MusicTrack?
        get() = currentMediaController?.metadata?.toMusicTrack()
    override val currentVolume: Int
        get() = currentMediaController?.playbackInfo?.getPebbleVolume() ?: 0
}

private fun MediaMetadata.toMusicTrack(): MusicTrack {
    return MusicTrack(
            this.getString(MediaMetadata.METADATA_KEY_ARTIST),
            this.getString(MediaMetadata.METADATA_KEY_ALBUM),
            this.getString(MediaMetadata.METADATA_KEY_TITLE),
            this.getLong(MediaMetadata.METADATA_KEY_DURATION).milliseconds,
            this.getLong(MediaMetadata.METADATA_KEY_NUM_TRACKS).toInt(),
            this.getLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER).toInt()
    )
}

private fun PlaybackState.toPlayState(timeSinceLastPositionUpdate: Long): PlayState {
    val playbackState = when (state) {
        PlaybackState.STATE_PLAYING,
        PlaybackState.STATE_BUFFERING -> MusicControl.PlaybackState.Playing

        PlaybackState.STATE_REWINDING,
        PlaybackState.STATE_SKIPPING_TO_PREVIOUS -> MusicControl.PlaybackState.Rewinding

        PlaybackState.STATE_FAST_FORWARDING,
        PlaybackState.STATE_SKIPPING_TO_NEXT -> MusicControl.PlaybackState.FastForwarding

        PlaybackState.STATE_PAUSED,
        PlaybackState.STATE_STOPPED -> MusicControl.PlaybackState.Paused

        else -> MusicControl.PlaybackState.Unknown
    }

    return PlayState(
            playbackState,
            (position + timeSinceLastPositionUpdate).toInt(),
            (playbackSpeed * 100f).roundToInt(),
            MusicControl.ShuffleState.Unknown,
            MusicControl.RepeatState.Unknown
    )
}

/**
 * Returns the volume of the media session in Pebble volume format (0-100)
 */
private fun MediaController.PlaybackInfo.getPebbleVolume(): Int {
    return (100f * currentVolume / maxVolume).roundToInt()
}