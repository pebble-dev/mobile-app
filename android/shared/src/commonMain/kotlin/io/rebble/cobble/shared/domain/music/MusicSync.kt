package io.rebble.cobble.shared.domain.music

import io.rebble.cobble.shared.data.MusicTrack
import io.rebble.cobble.shared.data.PlayState
import io.rebble.libpebblecommon.packets.MusicControl
import io.rebble.libpebblecommon.services.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlin.time.Duration.Companion.milliseconds

class MusicSync(
        private val scope: CoroutineScope,
        private val musicService: MusicService,
        private val platformMusicController: PlatformMusicController
) {
    private val trackFlow = MutableStateFlow<MusicTrack?>(null)
    private val playStateFlow = MutableStateFlow<PlayState?>(null)
    private val volumeFlow = MutableStateFlow<Int?>(null)
    private val debounce = 500.milliseconds

    init {
        trackFlow.debounce(debounce).onEach {
            if (it == null) {
                musicService.send(MusicControl.UpdateCurrentTrack(
                        it?.artist ?: "",
                        it?.album ?: "",
                        it?.title ?: ""
                ))
            } else {
                musicService.send(MusicControl.UpdateCurrentTrack(
                        it.artist ?: "",
                        it.album ?: "",
                        it.title ?: "",
                        it.duration.inWholeMilliseconds.toInt(),
                        it.trackCount,
                        it.currentTrack
                ))
            }
        }.launchIn(scope)

        playStateFlow.debounce(debounce).filterNotNull().onEach {
            musicService.send(MusicControl.UpdatePlayStateInfo(
                    it.playbackState,
                    it.playbackPosition.toUInt(),
                    it.playbackRate.toUInt(),
                    it.shuffleState,
                    it.repeatState
            ))
        }.launchIn(scope)

        volumeFlow.debounce(debounce).filterNotNull().onEach {
            musicService.send(MusicControl.UpdateVolumeInfo(it.toUByte()))
        }.launchIn(scope)

        listenForIncomingMessages()
    }

    /**
     * Update the currently playing track.
     * @param track Currently playing track, or null if no track is playing.
     */
    fun updateTrack(track: MusicTrack?) {
        trackFlow.value = track
    }

    /**
     * Update the play state of the music player.
     * @param playState Play state, or null if no track is playing.
     */
    fun updatePlayState(playState: PlayState?) {
        playStateFlow.value = playState
    }

    /**
     * Update the volume of the music player.
     * @param volume Volume in range 0-100 (%)
     */
    fun updateVolume(volume: Int) {
        volumeFlow.value = volume
    }

    private fun listenForIncomingMessages() {
        musicService.receivedMessages.receiveAsFlow().onEach { msg ->
            when (msg.message) {
                MusicControl.Message.PlayPause -> {
                    if (platformMusicController.isPlaying) {
                        platformMusicController.pause()
                    } else {
                        platformMusicController.play()
                    }
                }

                MusicControl.Message.Pause -> { //TODO: investigate why play/pause is swapped
                    platformMusicController.play()
                }

                MusicControl.Message.Play -> {
                    platformMusicController.pause()
                }

                MusicControl.Message.NextTrack -> {
                    platformMusicController.skipToNext()
                }

                MusicControl.Message.PreviousTrack -> {
                    platformMusicController.skipToPrevious()
                }

                MusicControl.Message.VolumeUp -> {
                    platformMusicController.volumeUp()
                    volumeFlow.value = platformMusicController.currentVolume
                }

                MusicControl.Message.VolumeDown -> {
                    platformMusicController.volumeDown()
                    volumeFlow.value = platformMusicController.currentVolume
                }

                MusicControl.Message.GetCurrentTrack -> {
                    trackFlow.value = platformMusicController.currentTrack
                }

                MusicControl.Message.UpdateCurrentTrack,
                MusicControl.Message.UpdatePlayStateInfo,
                MusicControl.Message.UpdateVolumeInfo,
                MusicControl.Message.UpdatePlayerInfo,
                -> Unit
            }
        }.launchIn(scope)
    }
}