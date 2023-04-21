// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cohorts_firmwares.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CohortsFirmwares _$CohortsFirmwaresFromJson(Map<String, dynamic> json) {
  $checkKeys(
    json,
    allowedKeys: const ['normal', 'recovery'],
  );
  return CohortsFirmwares(
    normal: json['normal'] == null
        ? null
        : CohortsFirmware.fromJson(json['normal'] as Map<String, dynamic>),
    recovery: json['recovery'] == null
        ? null
        : CohortsFirmware.fromJson(json['recovery'] as Map<String, dynamic>),
  );
}

Map<String, dynamic> _$CohortsFirmwaresToJson(CohortsFirmwares instance) =>
    <String, dynamic>{
      'normal': instance.normal,
      'recovery': instance.recovery,
    };
