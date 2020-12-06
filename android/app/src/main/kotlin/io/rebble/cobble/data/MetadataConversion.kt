package io.rebble.cobble.data

import android.bluetooth.BluetoothDevice
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.cobble.util.macAddressToLong
import io.rebble.libpebblecommon.packets.WatchFirmwareVersion
import io.rebble.libpebblecommon.packets.WatchVersion

fun WatchVersion.WatchVersionResponse.toPigeon(
        btDevice: BluetoothDevice,
        model: Int?
): Pigeons.PebbleDevicePigeon {
    return Pigeons.PebbleDevicePigeon().also {
        it.name = btDevice.name
        it.address = btDevice.address.macAddressToLong()
        it.runningFirmware = running.toPigeon()
        it.recoveryFirmware = recovery.toPigeon()
        it.model = model?.toLong() ?: 0L
        it.bootloaderTimestamp = bootloaderTimestamp.get().toLong()
        it.board = board.get()
        it.serial = serial.get()
        it.language = language.get()
        it.isUnfaithful = isUnfaithful.get()
    }
}

fun WatchFirmwareVersion.toPigeon(): Pigeons.PebbleFirmwarePigeon {
    return Pigeons.PebbleFirmwarePigeon().also {
        it.timestamp = timestamp.get().toLong()
        it.version = versionTag.get()
        it.gitHash = gitHash.get()
        it.isRecovery = isRecovery.get()
        it.hardwarePlatform = hardwarePlatform.get().toLong()
        it.metadataVersion = metadataVersion.get().toLong()
    }
}
