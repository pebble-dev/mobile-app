package io.rebble.cobble.shared.domain.music

import io.rebble.cobble.shared.data.MusicTrack

interface PlatformMusicController {
    fun play()
    fun pause()
    fun skipToNext()
    fun skipToPrevious()
    fun volumeUp()
    fun volumeDown()

    val isPlaying: Boolean
    val currentTrack: MusicTrack?

    /**
     * Current volume of the music player. Range 0-100 (%)
     */
    val currentVolume: Int
}