import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/selectable_calendar.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/db/models/timeline_pin_layout.dart';
import 'package:cobble/domain/db/models/timeline_pin_type.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:flutter_test/flutter_test.dart';

final TEST_CALENDAR = SelectableCalendar(
  "Test@Calendar",
  "10",
  true,
);

void main() {
  test("Generate pin from basic event", () {
    final event = Event("10",
        eventId: "33",
        title: "The Event",
        start: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          10, //Hour
          30, // Minute
        ),
        end: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          11, //Hour
          30, // Minute
        ));

    final expectedPin = TimelinePin(
        parentId: CALENDAR_WATCHAPP_ID,
        backingId: "33T1605954600000",
        timestamp: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          10, //Hour
          30, // Minute
        ),
        duration: 60,
        type: TimelinePinType.PIN,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.CALENDAR_PIN,
        nextSyncAction: NextSyncAction.Upload);

    expect(event.generateBasicEventData(null, null), expectedPin);
  });

  test("Generate pin from all day event", () {
    final event = Event(
      "10",
      eventId: "33",
      title: "The Event",
      start: DateTime.utc(
        2020, // Year
        11, // Month
        21, // Day
        10, //Hour
        30, // Minute
      ),
      end: DateTime.utc(
        2020, // Year
        11, // Month
        21, // Day
        11, //Hour
        30, // Minute
      ),
      allDay: true,
    );

    final expectedPin = TimelinePin(
        parentId: CALENDAR_WATCHAPP_ID,
        backingId: "33T1605954600000",
        timestamp: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          10, //Hour
          30, // Minute
        ),
        duration: 60,
        type: TimelinePinType.PIN,
        isVisible: true,
        isFloating: false,
        isAllDay: true,
        persistQuickView: false,
        layout: TimelinePinLayout.CALENDAR_PIN,
        nextSyncAction: NextSyncAction.Upload);

    expect(event.generateBasicEventData(null, null), expectedPin);
  });

  test("Generate attributes from basic event", () {
    final event = Event("10",
        title: "The Event",
        start: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          10, //Hour
          30, // Minute
        ),
        end: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          11, //Hour
          30, // Minute
        ));

    final expectedAttributes = [
      TimelineAttribute.title("The Event"),
      TimelineAttribute.tinyIcon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.headings([
        "Calendar",
      ]),
      TimelineAttribute.paragraphs([
        "Test@Calendar",
      ]),
    ].toSet();

    expect(event.getAttributes(TEST_CALENDAR).toSet(), expectedAttributes);
  });

  test("Generate attributes from event with description", () {
    final event = Event(
      "10",
      title: "The Event",
      start: DateTime.utc(
        2020, // Year
        11, // Month
        21, // Day
        10, //Hour
        30, // Minute
      ),
      end: DateTime.utc(
        2020, // Year
        11, // Month
        21, // Day
        11, //Hour
        30, // Minute
      ),
      description: "Going out with some friends",
    );

    final expectedAttributes = [
      TimelineAttribute.title("The Event"),
      TimelineAttribute.tinyIcon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.headings([
        "",
        "Calendar",
      ]),
      TimelineAttribute.paragraphs([
        "Going out with some friends",
        "Test@Calendar",
      ]),
    ].toSet();

    expect(event.getAttributes(TEST_CALENDAR).toSet(), expectedAttributes);
  });

  test("Generate attributes from event with location", () {
    final event = Event("10",
        title: "The Event",
        start: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          10, //Hour
          30, // Minute
        ),
        end: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          11, //Hour
          30, // Minute
        ));

    event.location = "Rebble headquarters";

    final expectedAttributes = [
      TimelineAttribute.title("The Event"),
      TimelineAttribute.tinyIcon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.locationName("Rebble headquarters"),
      TimelineAttribute.headings([
        "Calendar",
      ]),
      TimelineAttribute.paragraphs([
        "Test@Calendar",
      ]),
    ].toSet();

    expect(event.getAttributes(TEST_CALENDAR).toSet(), expectedAttributes);
  });

  test("Generate attributes from event with attendees", () {
    final event = Event("10",
        title: "The Event",
        start: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          10, //Hour
          30, // Minute
        ),
        end: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          11, //Hour
          30, // Minute
        ),
        attendees: [
          Attendee(name: "John"),
          Attendee(name: "Jane"),
          Attendee(name: " "),
        ]);

    final expectedAttributes = [
      TimelineAttribute.title("The Event"),
      TimelineAttribute.tinyIcon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.headings([
        "Attendees",
        "Calendar",
      ]),
      TimelineAttribute.paragraphs([
        "John, Jane",
        "Test@Calendar",
      ]),
    ].toSet();

    expect(event.getAttributes(TEST_CALENDAR).toSet(), expectedAttributes);
  });

  test("Generate attributes from recurring event", () {
    final event = Event("10",
        title: "The Event",
        start: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          10, //Hour
          30, // Minute
        ),
        end: DateTime.utc(
          2020, // Year
          11, // Month
          21, // Day
          11, //Hour
          30, // Minute
        ),
        recurrenceRule: RecurrenceRule(RecurrenceFrequency.Weekly));

    final expectedAttributes = [
      TimelineAttribute.title("The Event"),
      TimelineAttribute.tinyIcon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.displayRecurring(true),
      TimelineAttribute.headings([
        "Recurrence",
        "Calendar",
      ]),
      TimelineAttribute.paragraphs([
        "Repeats weekly.",
        "Test@Calendar",
      ]),
    ].toSet();

    expect(event.getAttributes(TEST_CALENDAR).toSet(), expectedAttributes);
  });
}
