package io.rebble.fossil.bridges

import io.rebble.fossil.pigeons.Pigeons

fun BooleanWrapper(value: Boolean) = Pigeons.BooleanWrapper().also {
    it.value = value
}

fun NumberWrapper(value: Number) = Pigeons.NumberWrapper().also {
    it.value = value.toLong()
}