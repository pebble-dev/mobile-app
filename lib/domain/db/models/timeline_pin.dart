import 'package:cobble/domain/db/converters/sql_json_converters.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin_type.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';

import 'timeline_pin_layout.dart';

part 'timeline_pin.g.dart';

@JsonSerializable()
@UuidConverter()
@BooleanNumberConverter()
@NumberDateTimeConverter()
@CopyWith(generateCopyWithNull: true)
class TimelinePin {
  /// Unique UUID of the item
  final Uuid itemId;

  /// UUID of the watchapp that owns this pin
  final Uuid parentId;

  /// ID of the item that is backing this pin
  /// (for example ID of the calendar event)
  /// can be null
  final String backingId;

  /// Date of the pin start
  final DateTime timestamp;

  /// Duration of the event in minutes
  final int duration;

  /// Type of the pin
  final TimelinePinType type;

  /// ???
  final bool isVisible;

  /// ???
  final bool isFloating;

  /// Whether pin spans throughout the whole day (Duration ignored???)
  final bool isAllDay;

  /// ???
  final bool persistQuickView;

  /// UI Layout of the pin
  final TimelinePinLayout layout;

  /// JSON of the timeline pin attributes. Those attributes must
  /// correspond to the specified [layout].
  final String attributesJson;

  /// JSON of the pin actions. Can be *null* if pin has no actions.
  final String actionsJson;

  /// Action that should be performed for this pin
  /// when the next sync-to-watch is performed
  final NextSyncAction nextSyncAction;

  TimelinePin(
      {this.itemId,
      this.parentId,
      this.backingId,
      this.timestamp,
      this.duration,
      this.type,
      this.isVisible,
      this.isFloating,
      this.isAllDay,
      this.persistQuickView,
      this.layout,
      this.attributesJson,
      this.actionsJson,
      this.nextSyncAction});

  Map<String, dynamic> toMap() {
    return _$TimelinePinToJson(this);
  }

  factory TimelinePin.fromMap(Map<String, dynamic> map) {
    return _$TimelinePinFromJson(map);
  }

  @override
  String toString() {
    return 'TimelinePin{itemId: $itemId, parentId: $parentId, backingId: $backingId, timestamp: $timestamp, duration: $duration, type: $type, isVisible: $isVisible, isFloating: $isFloating, isAllDay: $isAllDay, persistQuickView: $persistQuickView, layout: $layout, attributesJson: $attributesJson, actionsJson: $actionsJson, nextSyncAction: $nextSyncAction}';
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
          other is TimelinePin &&
              runtimeType == other.runtimeType &&
              itemId == other.itemId &&
              parentId == other.parentId &&
              backingId == other.backingId &&
              timestamp == other.timestamp &&
              duration == other.duration &&
              type == other.type &&
              isVisible == other.isVisible &&
              isFloating == other.isFloating &&
              isAllDay == other.isAllDay &&
              persistQuickView == other.persistQuickView &&
              layout == other.layout &&
              attributesJson == other.attributesJson &&
              actionsJson == other.actionsJson &&
              nextSyncAction == other.nextSyncAction;

  @override
  @JsonKey(ignore: true)
  int get hashCode =>
      itemId.hashCode ^
      parentId.hashCode ^
      backingId.hashCode ^
      timestamp.hashCode ^
      duration.hashCode ^
      type.hashCode ^
      isVisible.hashCode ^
      isFloating.hashCode ^
      isAllDay.hashCode ^
      persistQuickView.hashCode ^
      layout.hashCode ^
      attributesJson.hashCode ^
      actionsJson.hashCode ^
      nextSyncAction.hashCode;

  static Map<NextSyncAction, String> nextSyncActionEnumMap() {
    return _$NextSyncActionEnumMap;
  }
}
