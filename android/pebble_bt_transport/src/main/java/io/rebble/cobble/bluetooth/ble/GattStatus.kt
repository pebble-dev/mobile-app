package io.rebble.cobble.bluetooth.ble

import android.bluetooth.BluetoothGatt
import java.util.Locale

class GattStatus(val value: Int) {
    override fun toString(): String {
        val err = BluetoothGatt::class.java.declaredFields.find { p ->
            p.type.name == "int" &&
                    p.name.startsWith("GATT_") &&
                    p.getInt(null) == value
        }
        var ret = err?.name?.replace("GATT", "")?.replace("_", "")?.toLowerCase(Locale.ROOT)?.capitalize()
                ?: "Unknown error"
        ret += " (${value})"
        return ret
    }

    fun isSuccess(): Boolean {
        return value == BluetoothGatt.GATT_SUCCESS
    }
}