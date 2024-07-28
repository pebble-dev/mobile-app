package io.rebble.cobble.datasources

import android.content.SharedPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
inline fun <T> SharedPreferences.flow(
        key: String,
        crossinline mapper: (preferences: SharedPreferences, key: String) -> T): Flow<T> {

    return callbackFlow {
        trySend(mapper(this@flow, key)).isSuccess

        val listener = SharedPreferences
                .OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences,
                                                    changedKey: String? ->

                    if (changedKey == key) {
                        trySend(mapper(sharedPreferences, key)).isSuccess
                    }
                }

        registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}