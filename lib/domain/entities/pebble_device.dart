import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';

class PebbleFirmware {
  final int timestamp;
  final String version;
  final String gitHash;
  final bool isRecovery;
  final PebbleHardwarePlatform hardwarePlatform;
  final int metadataVersion;

  PebbleFirmware(this.timestamp, this.version, this.gitHash, this.isRecovery,
      this.hardwarePlatform, this.metadataVersion);

  static PebbleFirmware fromPigeon(PebbleFirmwarePigeon pigeon) {
    return PebbleFirmware(
      pigeon.timestamp,
      pigeon.version,
      pigeon.gitHash,
      pigeon.isRecovery,
      pebbleHardwarePlatformFromNumber(pigeon.hardwarePlatform),
      pigeon.metadataVersion,
    );
  }
}

class PebbleDevice {
  final String name;
  final int address;
  final PebbleFirmware runningFirmware;
  final PebbleFirmware recoveryFirmware;
  final WatchModel model;
  final int bootloaderTimestamp;
  final String board;
  final String serial;
  final String language;
  final int languageVersion;
  final bool isUnfaithful;

  PebbleDevice(
      this.name,
      this.address,
      this.runningFirmware,
      this.recoveryFirmware,
      this.model,
      this.bootloaderTimestamp,
      this.board,
      this.serial,
      this.language,
      this.languageVersion,
      this.isUnfaithful);

  static PebbleDevice fromPigeon(PebbleDevicePigeon pigeon) {
    if (pigeon == null) {
      return null;
    }

    return PebbleDevice(
      pigeon.name,
      pigeon.address,
      PebbleFirmware.fromPigeon(pigeon.runningFirmware),
      PebbleFirmware.fromPigeon(pigeon.recoveryFirmware),
      watchModelFromNumber(pigeon.model),
      pigeon.bootloaderTimestamp,
      pigeon.board,
      pigeon.serial,
      pigeon.language,
      pigeon.languageVersion,
      pigeon.isUnfaithful,
    );
  }
}
