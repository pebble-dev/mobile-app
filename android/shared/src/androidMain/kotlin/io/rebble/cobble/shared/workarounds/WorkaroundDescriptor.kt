package io.rebble.cobble.shared.workarounds

import android.content.Context

interface WorkaroundDescriptor {
    val name: String

    fun isNeeded(context: Context): Boolean

    companion object {
        val allWorkarounds =
            listOf<WorkaroundDescriptor>(
                UnboundWatchBeforeConnecting
            )
    }
}