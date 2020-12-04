import 'dart:ui';

import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:collection/collection.dart';
import 'package:json_annotation/json_annotation.dart';

import 'game_state.dart';

part 'timeline_attribute.g.dart';

/// Single timeline pin attribute
/// Attributes consist of id, one value (of any of the supported types) and
/// optionally maxLength on array types
///
/// Use factory methods to create attributes with valid values.
@JsonSerializable(includeIfNull: false, createFactory: false)
class TimelineAttribute {
  final int id;
  final String string;
  final List<String> listOfString;
  final int uint8;
  final int uint32;
  final int maxLength;

  TimelineAttribute({
    this.id,
    this.string,
    this.listOfString,
    this.uint8,
    this.uint32,
    this.maxLength,
  });

  Map<String, dynamic> toMap() {
    return _$TimelineAttributeToJson(this);
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is TimelineAttribute &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          string == other.string &&
          ListEquality().equals(listOfString, other.listOfString) &&
          uint8 == other.uint8 &&
          uint32 == other.uint32 &&
          maxLength == other.maxLength;

  @override
  @JsonKey(ignore: true)
  int get hashCode =>
      id.hashCode ^
      string.hashCode ^
      listOfString.hashCode ^
      uint8.hashCode ^
      uint32.hashCode ^
      maxLength.hashCode;

  @override
  String toString() {
    return 'TimelineAttribute{id: $id, string: $string, listOfString: $listOfString, uint8: $uint8, uint32: $uint32, maxLength: $maxLength}';
  }

  static TimelineAttribute title(String title) {
    return TimelineAttribute(
      id: 1,
      string: title,
      maxLength: 64,
    );
  }

  static TimelineAttribute subtitle(String subtitle) {
    return TimelineAttribute(
      id: 2,
      string: subtitle,
      maxLength: 64,
    );
  }

  static TimelineAttribute body(String body) {
    return TimelineAttribute(
      id: 3,
      string: body,
      maxLength: 512,
    );
  }

  static TimelineAttribute tinyIcon(TimelineIcon icon) {
    // Pebble requires most significant bit on all icon numbers to be high.
    // Not sure why it was done that way

    return TimelineAttribute(
      id: 4,
      uint32: icon.toProtocolNumber() | 0x80000000,
    );
  }

  static TimelineAttribute smallIcon(TimelineIcon icon) {
    return TimelineAttribute(
      id: 5,
      uint32: icon.toProtocolNumber() | 0x80000000,
    );
  }

  static TimelineAttribute largeIcon(TimelineIcon icon) {
    return TimelineAttribute(
      id: 6,
      uint32: icon.toProtocolNumber() | 0x80000000,
    );
  }

  static TimelineAttribute ancsAction(int action) {
    return TimelineAttribute(
      id: 7,
      uint32: action,
    );
  }

  static TimelineAttribute cannedResponse(List<String> responses) {
    return TimelineAttribute(
      id: 8,
      listOfString: responses,
      maxLength: 512,
    );
  }

  static TimelineAttribute shortTitle(String shortTitle) {
    return TimelineAttribute(
      id: 9,
      string: shortTitle,
      maxLength: 64,
    );
  }

  static TimelineAttribute locationName(String locationName) {
    return TimelineAttribute(
      id: 11,
      string: locationName,
      maxLength: 64,
    );
  }

  static TimelineAttribute sender(String sender) {
    return TimelineAttribute(
      id: 12,
      string: sender,
      maxLength: 64,
    );
  }

  static TimelineAttribute launchCode(int launchCode) {
    return TimelineAttribute(
      id: 13,
      uint32: launchCode,
    );
  }

  static TimelineAttribute lastUpdated(DateTime time) {
    return TimelineAttribute(
      id: 14,
      uint32: (time.millisecondsSinceEpoch / 1000).round(),
    );
  }

  static TimelineAttribute rankAway(String rank) {
    return TimelineAttribute(
      id: 15,
      string: rank,
    );
  }

  static TimelineAttribute rankHome(String rank) {
    return TimelineAttribute(
      id: 16,
      string: rank,
    );
  }

  static TimelineAttribute nameAway(String name) {
    return TimelineAttribute(
      id: 17,
      string: name,
    );
  }

  static TimelineAttribute nameHome(String name) {
    return TimelineAttribute(
      id: 18,
      string: name,
    );
  }

  static TimelineAttribute recordAway(String record) {
    return TimelineAttribute(
      id: 19,
      string: record,
    );
  }

  static TimelineAttribute recordHome(String record) {
    return TimelineAttribute(
      id: 20,
      string: record,
    );
  }

  static TimelineAttribute scoreAway(String score) {
    return TimelineAttribute(
      id: 21,
      string: score,
    );
  }

  static TimelineAttribute scoreHome(String score) {
    return TimelineAttribute(
      id: 22,
      string: score,
    );
  }

  static TimelineAttribute sportsGameState(TimelineGameState state) {
    return TimelineAttribute(
      id: 23,
      uint8: state.toProtocolNumber(),
    );
  }

  static TimelineAttribute broadcaster(String name) {
    return TimelineAttribute(
      id: 24,
      string: name,
    );
  }

  static TimelineAttribute headings(List<String> headings) {
    return TimelineAttribute(id: 25, listOfString: headings, maxLength: 128);
  }

  static TimelineAttribute paragraphs(List<String> paragraphs) {
    return TimelineAttribute(id: 26, listOfString: paragraphs, maxLength: 1024);
  }

  static TimelineAttribute foregroundColor(Color color) {
    return TimelineAttribute(
      id: 27,
      uint8: color.toProtocolNumber(),
    );
  }

  static TimelineAttribute primaryColor(Color color) {
    return TimelineAttribute(
      id: 28,
      uint8: color.toProtocolNumber(),
    );
  }

  static TimelineAttribute secondaryColor(Color color) {
    return TimelineAttribute(
      id: 29,
      uint8: color.toProtocolNumber(),
    );
  }

  static TimelineAttribute displayRecurring(bool recurring) {
    return TimelineAttribute(
      id: 31,
      uint8: recurring ? 1 : 0,
    );
  }

  static TimelineAttribute shortSubtitle(String shortSubtitle) {
    return TimelineAttribute(
      id: 36,
      string: shortSubtitle,
      maxLength: 64,
    );
  }

  static TimelineAttribute timestamp(DateTime timestamp) {
    return TimelineAttribute(
      id: 37,
      uint32: timestamp.millisecondsSinceEpoch ~/ 1000,
    );
  }

  static TimelineAttribute displayTime(bool displayTime) {
    return TimelineAttribute(
      id: 38,
      uint8: displayTime ? 1 : 0,
    );
  }

  static subtitleTemplateString(String templateString) {
    return TimelineAttribute(id: 47, string: templateString, maxLength: 150);
  }

  static icon(TimelineIcon icon) {
    return TimelineAttribute(
      id: 48,
      uint8: icon.toProtocolNumber(),
    );
  }
}

extension ColorAttribute on Color {
  int toProtocolNumber() {
    return ((alpha ~/ 85) << 6) |
        ((red ~/ 85) << 4) |
        ((green ~/ 85) << 2) |
        (blue ~/ 85);
  }
}
