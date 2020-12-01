import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/selectable_calendar.dart';
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
  test("Generate generic pin from calendar event", () {
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
      TimelineAttribute.icon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.headings([
        "Description",
        "Calendar",
      ]),
      TimelineAttribute.paragraphs([
        "Going out with some friends",
        "Test@Calendar",
      ]),
    ].toSet();

    expect(event.getAttributes(TEST_CALENDAR).toSet(), expectedAttributes);
  });
}
