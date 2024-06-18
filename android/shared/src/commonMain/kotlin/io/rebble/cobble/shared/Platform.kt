package io.rebble.cobble.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform