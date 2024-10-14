// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cohorts_firmware.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CohortsFirmware _$CohortsFirmwareFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const [
      'url',
      'sha-256',
      'friendlyVersion',
      'timestamp',
      'notes'
    ],
  );
  return CohortsFirmware(
    url: json['url'] as String,
    sha256: json['sha-256'] as String,
    friendlyVersion: json['friendlyVersion'] as String,
    timestamp: _dateTimeFromJson((json['timestamp'] as num).toInt()),
    notes: json['notes'] as String,
  );
}

Map<String, dynamic> _$CohortsFirmwareToJson(CohortsFirmware instance) =>
    <String, dynamic>{
      'url': instance.url,
      'sha-256': instance.sha256,
      'friendlyVersion': instance.friendlyVersion,
      'timestamp': _dateTimeToJson(instance.timestamp),
      'notes': instance.notes,
    };
