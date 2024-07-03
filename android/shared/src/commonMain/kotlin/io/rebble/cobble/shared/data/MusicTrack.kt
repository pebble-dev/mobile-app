package io.rebble.cobble.shared.data

import kotlin.time.Duration

data class MusicTrack (
        val artist: String?,
        val album: String?,
        val title: String?,
        val duration: Duration,
        val trackCount: Int,
        val currentTrack: Int,
)