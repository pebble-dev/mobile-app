package io.rebble.cobble.bluetooth.workarounds

import android.content.Context

interface WorkaroundDescriptor {
    val name: String

    fun isNeeded(context: Context): Boolean

    companion object {
        val allWorkarounds = emptyList<WorkaroundDescriptor>()
    }
}