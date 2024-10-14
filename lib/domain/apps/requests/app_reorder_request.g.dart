// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app_reorder_request.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AppReorderRequest _$AppReorderRequestFromJson(Map<String, dynamic> json) =>
    AppReorderRequest(
      Uuid.parse(json['uuid'] as String),
      (json['newPosition'] as num).toInt(),
    );

Map<String, dynamic> _$AppReorderRequestToJson(AppReorderRequest instance) =>
    <String, dynamic>{
      'uuid': _uuidToString(instance.uuid),
      'newPosition': instance.newPosition,
    };
