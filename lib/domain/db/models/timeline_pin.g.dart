// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_pin.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

extension TimelinePinCopyWith on TimelinePin {
  TimelinePin copyWith({
    String actionsJson,
    String attributesJson,
    String backingId,
    int duration,
    bool isAllDay,
    bool isFloating,
    bool isVisible,
    Uuid itemId,
    TimelinePinLayout layout,
    NextSyncAction nextSyncAction,
    Uuid parentId,
    bool persistQuickView,
    DateTime timestamp,
    TimelinePinType type,
  }) {
    return TimelinePin(
      actionsJson: actionsJson ?? this.actionsJson,
      attributesJson: attributesJson ?? this.attributesJson,
      backingId: backingId ?? this.backingId,
      duration: duration ?? this.duration,
      isAllDay: isAllDay ?? this.isAllDay,
      isFloating: isFloating ?? this.isFloating,
      isVisible: isVisible ?? this.isVisible,
      itemId: itemId ?? this.itemId,
      layout: layout ?? this.layout,
      nextSyncAction: nextSyncAction ?? this.nextSyncAction,
      parentId: parentId ?? this.parentId,
      persistQuickView: persistQuickView ?? this.persistQuickView,
      timestamp: timestamp ?? this.timestamp,
      type: type ?? this.type,
    );
  }

  TimelinePin copyWithNull({
    bool actionsJson = false,
    bool attributesJson = false,
    bool backingId = false,
    bool duration = false,
    bool isAllDay = false,
    bool isFloating = false,
    bool isVisible = false,
    bool itemId = false,
    bool layout = false,
    bool nextSyncAction = false,
    bool parentId = false,
    bool persistQuickView = false,
    bool timestamp = false,
    bool type = false,
  }) {
    return TimelinePin(
      actionsJson: actionsJson == true ? null : this.actionsJson,
      attributesJson: attributesJson == true ? null : this.attributesJson,
      backingId: backingId == true ? null : this.backingId,
      duration: duration == true ? null : this.duration,
      isAllDay: isAllDay == true ? null : this.isAllDay,
      isFloating: isFloating == true ? null : this.isFloating,
      isVisible: isVisible == true ? null : this.isVisible,
      itemId: itemId == true ? null : this.itemId,
      layout: layout == true ? null : this.layout,
      nextSyncAction: nextSyncAction == true ? null : this.nextSyncAction,
      parentId: parentId == true ? null : this.parentId,
      persistQuickView: persistQuickView == true ? null : this.persistQuickView,
      timestamp: timestamp == true ? null : this.timestamp,
      type: type == true ? null : this.type,
    );
  }
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelinePin _$TimelinePinFromJson(Map<String, dynamic> json) {
  return TimelinePin(
    itemId: const UuidConverter().fromJson(json['itemId'] as String),
    parentId: const UuidConverter().fromJson(json['parentId'] as String),
    backingId: json['backingId'] as String,
    timestamp:
        const NumberDateTimeConverter().fromJson(json['timestamp'] as int),
    duration: json['duration'] as int,
    type: _$enumDecodeNullable(_$TimelinePinTypeEnumMap, json['type']),
    isVisible:
        const BooleanNumberConverter().fromJson(json['isVisible'] as int),
    isFloating:
        const BooleanNumberConverter().fromJson(json['isFloating'] as int),
    isAllDay: const BooleanNumberConverter().fromJson(json['isAllDay'] as int),
    persistQuickView: const BooleanNumberConverter()
        .fromJson(json['persistQuickView'] as int),
    layout: _$enumDecodeNullable(_$TimelinePinLayoutEnumMap, json['layout']),
    attributesJson: json['attributesJson'] as String,
    actionsJson: json['actionsJson'] as String,
    nextSyncAction:
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
  TimelinePinType.notification: 'notification',
  TimelinePinType.pin: 'pin',
  TimelinePinType.reminder: 'reminder',
};

const _$TimelinePinLayoutEnumMap = {
  TimelinePinLayout.genericPin: 'genericPin',
  TimelinePinLayout.calendarPin: 'calendarPin',
  TimelinePinLayout.genericReminder: 'genericReminder',
  TimelinePinLayout.genericNotification: 'genericNotification',
  TimelinePinLayout.commNotification: 'commNotification',
  TimelinePinLayout.weatherPin: 'weatherPin',
  TimelinePinLayout.sportsPin: 'sportsPin',
};

const _$NextSyncActionEnumMap = {
  NextSyncAction.Nothing: 'Nothing',
  NextSyncAction.Upload: 'Upload',
  NextSyncAction.Delete: 'Delete',
  NextSyncAction.Ignore: 'Ignore',
  NextSyncAction.DeleteThenIgnore: 'DeleteThenIgnore',
};
