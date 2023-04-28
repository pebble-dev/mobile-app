package io.rebble.cobble.pigeons

fun BooleanWrapper(value: Boolean) = Pigeons.BooleanWrapper().also {
    it.value = value
}

fun NumberWrapper(value: Number) = Pigeons.NumberWrapper().also {
    it.value = value.toLong()
}

fun ListWrapper(value: List<*>) = Pigeons.ListWrapper().also {
    it.value = if (value is ArrayList<*>) {
        value
    } else {
        ArrayList(value)
    }
}

// Provide public proxy to some package-only methods

//fun Pigeons.PebbleScanDevicePigeon.toMapExt() = toMap()