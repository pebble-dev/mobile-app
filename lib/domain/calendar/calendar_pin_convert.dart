import 'package:cobble/domain/calendar/selectable_calendar.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/db/models/timeline_pin_layout.dart';
import 'package:cobble/domain/db/models/timeline_pin_type.dart';
import 'package:cobble/domain/timeline/timeline_action.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:cobble/util/string_extensions.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:uuid_type/uuid_type.dart';

extension CalendarEventConverter on Event {
  List<TimelineAttribute> getAttributes(SelectableCalendar calendar) {
    final List<String> headings = [];
    final List<String> paragraphs = [];

    if (description != null) {
      headings.add("");
      paragraphs.add(_transformDescription(description));
    }

    if (attendees != null && attendees.isNotEmpty) {
      headings.add("Attendees");
      paragraphs.add(attendees
          .map((e) => e.name)
          .where((element) => element.trim().isNotEmpty)
          .join(", "));
    }

    if (recurrenceRule != null) {
      String recurrenceText = "Unknown";
      switch (recurrenceRule.recurrenceFrequency) {
        case RecurrenceFrequency.Daily:
          recurrenceText = "Repeats daily.";
          break;
        case RecurrenceFrequency.Weekly:
          recurrenceText = "Repeats weekly.";
          break;
        case RecurrenceFrequency.Monthly:
          recurrenceText = "Repeats monthly.";
          break;
        case RecurrenceFrequency.Yearly:
          recurrenceText = "Repeats yearly.";
          break;
      }

      headings.add("Recurrence");
      paragraphs.add(recurrenceText);
    }

    headings.add("Calendar");
    paragraphs.add(calendar.name);

    return [
      TimelineAttribute.tinyIcon(TimelineIcon.timelineCalendar),
      TimelineAttribute.title(title),
      if (location != null) TimelineAttribute.locationName(location),
      if (recurrenceRule != null) TimelineAttribute.displayRecurring(true),
      TimelineAttribute.headings(headings),
      TimelineAttribute.paragraphs(paragraphs)
    ];
  }

  List<TimelineAction> getActions() {
    return [
      TimelineAction(calendarActionRemove, actionTypeGeneric, [
        TimelineAttribute.title("Remove"),
      ]),
      TimelineAction(calendarActionMuteCalendar, actionTypeGeneric, [
        TimelineAttribute.title("Mute calendar"),
      ]),
    ];
  }

  TimelinePin generateBasicEventData(
      String attributesJson, String actionsJson) {
    return TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: createCompositeBackingId(),
        timestamp: start,
        duration: end.difference(start).inMinutes,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: allDay,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        attributesJson: attributesJson,
        actionsJson: actionsJson,
        nextSyncAction: NextSyncAction.Upload);
  }

  /// Calendar event is unique by two attributes: event ID and event start
  /// timestamp (since same event can be recurring event and have multiple
  /// starts with same ID)
  ///
  /// To ease processing, we insert composite ID into database that contains
  /// both
  String createCompositeBackingId() {
    return "${calendarId}T${eventId}T${start.millisecondsSinceEpoch}";
  }

  String _transformDescription(String rawDescription) {
    RegExp htmlTags = RegExp(r"<[^>]*>", multiLine: true, caseSensitive: true);

    return rawDescription.replaceAll(htmlTags, "").trimWithEllipsis(500);
  }
}

class CalendarEventId {
  final String calendarId;
  final String eventId;
  final DateTime startTime;

  CalendarEventId(this.calendarId, this.eventId, this.startTime);

  static CalendarEventId fromTimelinePin(TimelinePin pin) {
    final backingId = pin.backingId;
    final split = backingId.split("T");
    if (split.length != 3) {
      return null;
    }

    return CalendarEventId(
      split[0],
      split[1],
      DateTime.fromMillisecondsSinceEpoch(int.parse(split[2]), isUtc: true),
    );
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is CalendarEventId &&
          runtimeType == other.runtimeType &&
          calendarId == other.calendarId &&
          eventId == other.eventId &&
          startTime == other.startTime;

  @override
  int get hashCode =>
      calendarId.hashCode ^ eventId.hashCode ^ startTime.hashCode;

  @override
  String toString() {
    return 'CalendarEventId{calendarId: $calendarId, eventId: $eventId, startTime: $startTime}';
  }
}

final calendarWatchappId = Uuid("6c6c6fc2-1912-4d25-8396-3547d1dfac5b");

const calendarActionRemove = 0;
const calendarActionMuteCalendar = 1;
