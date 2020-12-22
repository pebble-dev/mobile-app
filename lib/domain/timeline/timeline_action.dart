import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:collection/collection.dart';
import 'package:json_annotation/json_annotation.dart';

part 'timeline_action.g.dart';

@JsonSerializable(includeIfNull: false, createFactory: false)
class TimelineAction {
  final int actionId;
  final int actionType;
  final List<TimelineAttribute> attributes;

  TimelineAction(this.actionId, this.actionType, this.attributes);

  Map<String, dynamic> toJson() {
    return _$TimelineActionToJson(this);
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is TimelineAction &&
          runtimeType == other.runtimeType &&
          actionId == other.actionId &&
          actionType == other.actionType &&
          ListEquality().equals(attributes, other.attributes);

  @override
  @JsonKey(ignore: true)
  int get hashCode =>
      actionId.hashCode ^ actionType.hashCode ^ attributes.hashCode;

  @override
  String toString() {
    return 'TimelineAction{actionId: $actionId, '
        'actionType: $actionType, '
        'attributes: $attributes}';
  }
}

const int actionTypeAncsDismiss = 0x01;
const int actionTypeGeneric = 0x02;
const int actionTypeResponse = 0x03;
const int actionTypeDismiss = 0x04;
const int actionTypeHttp = 0x05;
const int actionTypeSnooze = 0x06;
const int actionTypeOpenWatchapp = 0x07;
const int actionTypeEmpty = 0x08;
const int actionTypeRemove = 0x09;
const int actionTypeOpenPin = 0x0a;
