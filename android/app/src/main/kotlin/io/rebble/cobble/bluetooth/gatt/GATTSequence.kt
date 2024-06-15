package io.rebble.cobble.bluetooth.gatt

class GATTSequence {
    private var seq = 0
    val current get() = seq

    val next
        get() = run {
            val current = seq
            seq = (seq + 1) % 32
            current
        }

    fun reset() {
        seq = 0
    }
}