// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_pin.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TimelinePinCWProxy {
  TimelinePin itemId(Uuid? itemId);

  TimelinePin parentId(Uuid? parentId);

  TimelinePin backingId(String? backingId);

  TimelinePin timestamp(DateTime? timestamp);

  TimelinePin duration(int? duration);

  TimelinePin type(TimelinePinType? type);

  TimelinePin isVisible(bool isVisible);

  TimelinePin isFloating(bool isFloating);

  TimelinePin isAllDay(bool isAllDay);

  TimelinePin persistQuickView(bool persistQuickView);

  TimelinePin layout(TimelinePinLayout? layout);

  TimelinePin attributesJson(String? attributesJson);

  TimelinePin actionsJson(String? actionsJson);

  TimelinePin nextSyncAction(NextSyncAction? nextSyncAction);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `TimelinePin(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// TimelinePin(...).copyWith(id: 12, name: "My name")
  /// ````
  TimelinePin call({
    Uuid? itemId,
    Uuid? parentId,
    String? backingId,
    DateTime? timestamp,
    int? duration,
    TimelinePinType? type,
    bool? isVisible,
    bool? isFloating,
    bool? isAllDay,
    bool? persistQuickView,
    TimelinePinLayout? layout,
    String? attributesJson,
    String? actionsJson,
    NextSyncAction? nextSyncAction,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfTimelinePin.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfTimelinePin.copyWith.fieldName(...)`
class _$TimelinePinCWProxyImpl implements _$TimelinePinCWProxy {
  const _$TimelinePinCWProxyImpl(this._value);

  final TimelinePin _value;

  @override
  TimelinePin itemId(Uuid? itemId) => this(itemId: itemId);

  @override
  TimelinePin parentId(Uuid? parentId) => this(parentId: parentId);

  @override
  TimelinePin backingId(String? backingId) => this(backingId: backingId);

  @override
  TimelinePin timestamp(DateTime? timestamp) => this(timestamp: timestamp);

  @override
  TimelinePin duration(int? duration) => this(duration: duration);

  @override
  TimelinePin type(TimelinePinType? type) => this(type: type);

  @override
  TimelinePin isVisible(bool isVisible) => this(isVisible: isVisible);

  @override
  TimelinePin isFloating(bool isFloating) => this(isFloating: isFloating);

  @override
  TimelinePin isAllDay(bool isAllDay) => this(isAllDay: isAllDay);

  @override
  TimelinePin persistQuickView(bool persistQuickView) =>
      this(persistQuickView: persistQuickView);

  @override
  TimelinePin layout(TimelinePinLayout? layout) => this(layout: layout);

  @override
  TimelinePin attributesJson(String? attributesJson) =>
      this(attributesJson: attributesJson);

  @override
  TimelinePin actionsJson(String? actionsJson) =>
      this(actionsJson: actionsJson);

  @override
  TimelinePin nextSyncAction(NextSyncAction? nextSyncAction) =>
      this(nextSyncAction: nextSyncAction);

  @override

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `TimelinePin(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// TimelinePin(...).copyWith(id: 12, name: "My name")
  /// ````
  TimelinePin call({
    Object? itemId = const $CopyWithPlaceholder(),
    Object? parentId = const $CopyWithPlaceholder(),
    Object? backingId = const $CopyWithPlaceholder(),
    Object? timestamp = const $CopyWithPlaceholder(),
    Object? duration = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? isVisible = const $CopyWithPlaceholder(),
    Object? isFloating = const $CopyWithPlaceholder(),
    Object? isAllDay = const $CopyWithPlaceholder(),
    Object? persistQuickView = const $CopyWithPlaceholder(),
    Object? layout = const $CopyWithPlaceholder(),
    Object? attributesJson = const $CopyWithPlaceholder(),
    Object? actionsJson = const $CopyWithPlaceholder(),
    Object? nextSyncAction = const $CopyWithPlaceholder(),
  }) {
    return TimelinePin(
      itemId: itemId == const $CopyWithPlaceholder()
          ? _value.itemId
          // ignore: cast_nullable_to_non_nullable
          : itemId as Uuid?,
      parentId: parentId == const $CopyWithPlaceholder()
          ? _value.parentId
          // ignore: cast_nullable_to_non_nullable
          : parentId as Uuid?,
      backingId: backingId == const $CopyWithPlaceholder()
          ? _value.backingId
          // ignore: cast_nullable_to_non_nullable
          : backingId as String?,
      timestamp: timestamp == const $CopyWithPlaceholder()
          ? _value.timestamp
          // ignore: cast_nullable_to_non_nullable
          : timestamp as DateTime?,
      duration: duration == const $CopyWithPlaceholder()
          ? _value.duration
          // ignore: cast_nullable_to_non_nullable
          : duration as int?,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as TimelinePinType?,
      isVisible: isVisible == const $CopyWithPlaceholder() || isVisible == null
          ? _value.isVisible
          // ignore: cast_nullable_to_non_nullable
          : isVisible as bool,
      isFloating:
          isFloating == const $CopyWithPlaceholder() || isFloating == null
              ? _value.isFloating
              // ignore: cast_nullable_to_non_nullable
              : isFloating as bool,
      isAllDay: isAllDay == const $CopyWithPlaceholder() || isAllDay == null
          ? _value.isAllDay
          // ignore: cast_nullable_to_non_nullable
          : isAllDay as bool,
      persistQuickView: persistQuickView == const $CopyWithPlaceholder() ||
              persistQuickView == null
          ? _value.persistQuickView
          // ignore: cast_nullable_to_non_nullable
          : persistQuickView as bool,
      layout: layout == const $CopyWithPlaceholder()
          ? _value.layout
          // ignore: cast_nullable_to_non_nullable
          : layout as TimelinePinLayout?,
      attributesJson: attributesJson == const $CopyWithPlaceholder()
          ? _value.attributesJson
          // ignore: cast_nullable_to_non_nullable
          : attributesJson as String?,
      actionsJson: actionsJson == const $CopyWithPlaceholder()
          ? _value.actionsJson
          // ignore: cast_nullable_to_non_nullable
          : actionsJson as String?,
      nextSyncAction: nextSyncAction == const $CopyWithPlaceholder()
          ? _value.nextSyncAction
          // ignore: cast_nullable_to_non_nullable
          : nextSyncAction as NextSyncAction?,
    );
  }
}

extension $TimelinePinCopyWith on TimelinePin {
  /// Returns a callable class that can be used as follows: `instanceOfTimelinePin.copyWith(...)` or like so:`instanceOfTimelinePin.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TimelinePinCWProxy get copyWith => _$TimelinePinCWProxyImpl(this);

  /// Copies the object with the specific fields set to `null`. If you pass `false` as a parameter, nothing will be done and it will be ignored. Don't do it. Prefer `copyWith(field: null)` or `TimelinePin(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// TimelinePin(...).copyWithNull(firstField: true, secondField: true)
  /// ````
  TimelinePin copyWithNull({
    bool itemId = false,
    bool parentId = false,
    bool backingId = false,
    bool timestamp = false,
    bool duration = false,
    bool type = false,
    bool layout = false,
    bool attributesJson = false,
    bool actionsJson = false,
    bool nextSyncAction = false,
  }) {
    return TimelinePin(
      itemId: itemId == true ? null : this.itemId,
      parentId: parentId == true ? null : this.parentId,
      backingId: backingId == true ? null : this.backingId,
      timestamp: timestamp == true ? null : this.timestamp,
      duration: duration == true ? null : this.duration,
      type: type == true ? null : this.type,
      isVisible: isVisible,
      isFloating: isFloating,
      isAllDay: isAllDay,
      persistQuickView: persistQuickView,
      layout: layout == true ? null : this.layout,
      attributesJson: attributesJson == true ? null : this.attributesJson,
      actionsJson: actionsJson == true ? null : this.actionsJson,
      nextSyncAction: nextSyncAction == true ? null : this.nextSyncAction,
    );
  }
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelinePin _$TimelinePinFromJson(Map<String, dynamic> json) => TimelinePin(
      itemId: const UuidConverter().fromJson(json['itemId'] as String?),
      parentId: const UuidConverter().fromJson(json['parentId'] as String?),
      backingId: json['backingId'] as String?,
      timestamp: const NumberDateTimeConverter()
          .fromJson((json['timestamp'] as num?)?.toInt()),
      duration: (json['duration'] as num?)?.toInt(),
      type: $enumDecodeNullable(_$TimelinePinTypeEnumMap, json['type']),
      isVisible: json['isVisible'] == null
          ? true
          : const BooleanNumberConverter()
              .fromJson((json['isVisible'] as num).toInt()),
      isFloating: json['isFloating'] == null
          ? false
          : const BooleanNumberConverter()
              .fromJson((json['isFloating'] as num).toInt()),
      isAllDay: json['isAllDay'] == null
          ? false
          : const BooleanNumberConverter()
              .fromJson((json['isAllDay'] as num).toInt()),
      persistQuickView: json['persistQuickView'] == null
          ? false
          : const BooleanNumberConverter()
              .fromJson((json['persistQuickView'] as num).toInt()),
      layout: $enumDecodeNullable(_$TimelinePinLayoutEnumMap, json['layout']),
      attributesJson: json['attributesJson'] as String?,
      actionsJson: json['actionsJson'] as String?,
      nextSyncAction:
          $enumDecodeNullable(_$NextSyncActionEnumMap, json['nextSyncAction']),
    );

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
