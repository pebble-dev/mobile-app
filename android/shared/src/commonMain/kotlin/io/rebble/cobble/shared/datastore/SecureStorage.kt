package io.rebble.cobble.shared.datastore

abstract class SecureStorage {
    protected abstract fun putString(key: String, value: String?)
    protected abstract fun getString(key: String): String?

    var token: String?
        get() = getString("token")
        set(value) {
            if (value == null) {
                putString("token", null)
            } else {
                putString("token", value)
            }
        }
}