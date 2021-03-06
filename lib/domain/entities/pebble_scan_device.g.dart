// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pebble_scan_device.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PebbleScanDevice _$PebbleScanDeviceFromJson(Map<String, dynamic> json) {
  return PebbleScanDevice(
    json['name'] as String?,
    json['address'] as int?,
    json['version'] as String?,
    json['serialNumber'] as String?,
    json['color'] as int?,
    json['runningPRF'] as bool?,
    json['firstUse'] as bool?,
  );
}

Map<String, dynamic> _$PebbleScanDeviceToJson(PebbleScanDevice instance) =>
    <String, dynamic>{
      'name': instance.name,
      'address': instance.address,
      'version': instance.version,
      'serialNumber': instance.serialNumber,
      'color': instance.color,
      'runningPRF': instance.runningPRF,
      'firstUse': instance.firstUse,
    };
