// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_action.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Map<String, dynamic> _$TimelineActionToJson(TimelineAction instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('actionId', instance.actionId);
  writeNotNull('actionType', instance.actionType);
  writeNotNull('attributes', instance.attributes);
  return val;
}
