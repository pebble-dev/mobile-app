package io.rebble.cobble.data

import io.rebble.cobble.bluetooth.BluetoothPebbleDevice
import io.rebble.cobble.bluetooth.EmulatedPebbleDevice
import io.rebble.cobble.shared.domain.common.PebbleDevice
import io.rebble.cobble.pigeons.Pigeons
import io.rebble.libpebblecommon.packets.WatchFirmwareVersion
import io.rebble.libpebblecommon.packets.WatchVersion

fun WatchVersion.WatchVersionResponse?.toPigeon(
        btDevice: PebbleDevice?,
        model: Int?
): Pigeons.PebbleDevicePigeon {
    // Pigeon does not appear to allow null values. We have to set some dummy values instead

    return Pigeons.PebbleDevicePigeon().also {
        it.name = btDevice?.let {
            if (btDevice is EmulatedPebbleDevice) "[Emulator]"
            else if (btDevice is BluetoothPebbleDevice) btDevice.bluetoothDevice.name.orEmpty()
            else error("Unknown PebbleDevice type")
        } ?: ""
        it.address = btDevice?.address ?: ""
        it.runningFirmware = this?.running?.toPigeon() ?: blankWatchFirwmareVersion()
        it.recoveryFirmware = this?.recovery?.toPigeon() ?: blankWatchFirwmareVersion()
        it.model = model?.toLong() ?: 0L
        it.bootloaderTimestamp = this?.bootloaderTimestamp?.get()?.toLong() ?: 0
        it.board = this?.board?.get() ?: ""
        it.serial = this?.serial?.get() ?: ""
        it.language = this?.language?.get() ?: ""
        it.isUnfaithful = this?.isUnfaithful?.get() ?: false
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

private fun blankWatchFirwmareVersion() = Pigeons.PebbleFirmwarePigeon().also {
    it.timestamp = 0L
    it.version = ""
    it.gitHash = ""
    it.isRecovery = false
    it.hardwarePlatform = 0L
    it.metadataVersion = 0L
}