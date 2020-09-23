package io.rebble.fossil.pigeons

fun BooleanWrapper(value: Boolean) = Pigeons.BooleanWrapper().also {
    it.value = value
}

fun NumberWrapper(value: Number) = Pigeons.NumberWrapper().also {
    it.value = value.toLong()
}

fun Pigeons.BooleanWrapper.toMapExt() = toMap()