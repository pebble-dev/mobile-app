package io.rebble.cobble.shared.database

enum class NextSyncAction(val value: Int) {
    Nothing(0),
    Upload(1),
    Delete(2),
    Ignore(3),
    DeleteThenIgnore(4);

    companion object {
        fun fromInt(value: Int): NextSyncAction {
            return when (value) {
                0 -> Nothing
                1 -> Upload
                2 -> Delete
                3 -> Ignore
                4 -> DeleteThenIgnore
                else -> throw IllegalArgumentException("Unknown value $value")
            }
        }
    }
}