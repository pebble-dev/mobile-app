import 'package:cobble/domain/calendar/selectable_calendar.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/db/models/timeline_pin_layout.dart';
import 'package:cobble/domain/db/models/timeline_pin_type.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:uuid_type/uuid_type.dart';

extension CalendarEventConverter on Event {
  List<TimelineAttribute> getAttributes(SelectableCalendar calendar) {
    final List<String> headings = [];
    final List<String> paragraphs = [];

    if (attendees != null && attendees.isNotEmpty) {
      headings.add("Attendees");
      paragraphs.add(attendees
          .map((e) => e.name)
          .where((element) => element.trim().isNotEmpty)
          .join(", "));
    }

    headings.add("Calendar");
    paragraphs.add(calendar.name);

    return [
      TimelineAttribute.icon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.title(title),
      if (description != null) TimelineAttribute.body(description),
      if (location != null) TimelineAttribute.locationName(location),
      TimelineAttribute.headings(headings),
      TimelineAttribute.paragraphs(paragraphs)
    ];
  }

  TimelinePin generateBasicEventData(
      String attributesJson, String actionsJson) {
    return TimelinePin(
        itemId: null,
        parentId: CALENDAR_WATCHAPP_ID,
        backingId: createCompositeBackingId(),
        timestamp: start,
        duration: end.difference(start).inMinutes,
        type: TimelinePinType.PIN,
        isVisible: true,
        isFloating: true,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.CALENDAR_PIN,
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
    return "${eventId}T${start.millisecondsSinceEpoch}";
  }
}

final CALENDAR_WATCHAPP_ID = Uuid("6c6c6fc2-1912-4d25-8396-3547d1dfac5b");
