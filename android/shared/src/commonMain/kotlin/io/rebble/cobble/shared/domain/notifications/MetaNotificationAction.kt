package io.rebble.cobble.shared.domain.notifications

enum class MetaNotificationAction {
    Dismiss,
    Open,
    MutePackage,
    MuteChannel;

    companion object {
        val metaActionLength = entries.size
    }
}