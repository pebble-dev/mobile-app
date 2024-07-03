package io.rebble.cobble.shared.data

import io.rebble.libpebblecommon.packets.MusicControl

data class PlayState(
        val playbackState: MusicControl.PlaybackState,
        val playbackPosition: Int,
        /**
         * Playback rate in percent (0-100)
         */
        val playbackRate: Int,
        val shuffleState: MusicControl.ShuffleState,
        val repeatState: MusicControl.RepeatState
)
