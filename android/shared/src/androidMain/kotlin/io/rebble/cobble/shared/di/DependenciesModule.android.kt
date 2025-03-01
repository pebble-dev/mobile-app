package io.rebble.cobble.shared.di

import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.FileStorage
import io.rebble.cobble.shared.AndroidPlatformContext
import io.rebble.cobble.shared.PlatformContext

actual fun makePlatformCacheStorage(platformContext: PlatformContext): CacheStorage {
    val dir =
        (platformContext as AndroidPlatformContext).applicationContext.cacheDir.resolve(
            "http_cache"
        )
    dir.mkdir()
    return FileStorage(dir)
}