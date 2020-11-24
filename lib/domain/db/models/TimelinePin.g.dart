// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'TimelinePin.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelinePin _$TimelinePinFromJson(Map<String, dynamic> json) {
  return TimelinePin(
    const UuidConverter().fromJson(json['itemId'] as String),
    const UuidConverter().fromJson(json['parentId'] as String),
    json['backingId'] as String,
    const NumberDateTimeConverter().fromJson(json['timestamp'] as int),
    json['duration'] as int,
    _$enumDecodeNullable(_$TimelinePinTypeEnumMap, json['type']),
    const BooleanNumberConverter().fromJson(json['isVisible'] as int),
    const BooleanNumberConverter().fromJson(json['isFloating'] as int),
    const BooleanNumberConverter().fromJson(json['isAllDay'] as int),
    const BooleanNumberConverter().fromJson(json['persistQuickView'] as int),
    _$enumDecodeNullable(_$TimelinePinLayoutEnumMap, json['layout']),
    json['attributesJson'] as String,
    json['actionsJson'] as String,
    _$enumDecodeNullable(_$NextSyncActionEnumMap, json['nextSyncAction']),
  );
}

Map<String, dynamic> _$TimelinePinToJson(TimelinePin instance) =>
    <String, dynamic>{
      'itemId': const UuidConverter().toJson(instance.itemId),
      'parentId': const UuidConverter().toJson(instance.parentId),
      'backingId': instance.backingId,
      'timestamp': const NumberDateTimeConverter().toJson(instance.timestamp),
      'duration': instance.duration,
      'type': _$TimelinePinTypeEnumMap[instance.type],
      'isVisible': const BooleanNumberConverter().toJson(instance.isVisible),
      'isFloating': const BooleanNumberConverter().toJson(instance.isFloating),
      'isAllDay': const BooleanNumberConverter().toJson(instance.isAllDay),
      'persistQuickView':
          const BooleanNumberConverter().toJson(instance.persistQuickView),
      'layout': _$TimelinePinLayoutEnumMap[instance.layout],
      'attributesJson': instance.attributesJson,
      'actionsJson': instance.actionsJson,
      'nextSyncAction': _$NextSyncActionEnumMap[instance.nextSyncAction],
    };

T _$enumDecode<T>(
  Map<T, dynamic> enumValues,
  dynamic source, {
  T unknownValue,
}) {
  if (source == null) {
    throw ArgumentError('A value must be provided. Supported values: '
        '${enumValues.values.join(', ')}');
  }

  final value = enumValues.entries
      .singleWhere((e) => e.value == source, orElse: () => null)
      ?.key;

  if (value == null && unknownValue == null) {
    throw ArgumentError('`$source` is not one of the supported values: '
        '${enumValues.values.join(', ')}');
  }
  return value ?? unknownValue;
}

T _$enumDecodeNullable<T>(
  Map<T, dynamic> enumValues,
  dynamic source, {
  T unknownValue,
}) {
  if (source == null) {
    return null;
  }
  return _$enumDecode<T>(enumValues, source, unknownValue: unknownValue);
}

const _$TimelinePinTypeEnumMap = {
  TimelinePinType.NOTIFICATION: 'NOTIFICATION',
  TimelinePinType.PIN: 'PIN',
  TimelinePinType.REMINDER: 'REMINDER',
};

const _$TimelinePinLayoutEnumMap = {
  TimelinePinLayout.GENERIC_PIN: 'GENERIC_PIN',
  TimelinePinLayout.CALENDAR_PIN: 'CALENDAR_PIN',
  TimelinePinLayout.GENERIC_REMINDER: 'GENERIC_REMINDER',
  TimelinePinLayout.GENERIC_NOTIFICATION: 'GENERIC_NOTIFICATION',
  TimelinePinLayout.COMM_NOTIFICATION: 'COMM_NOTIFICATION',
  TimelinePinLayout.WEATHER_PIN: 'WEATHER_PIN',
  TimelinePinLayout.SPORTS_PIN: 'SPORTS_PIN',
};

const _$NextSyncActionEnumMap = {
  NextSyncAction.Nothing: 'Nothing',
  NextSyncAction.Upload: 'Upload',
  NextSyncAction.Delete: 'Delete',
};
