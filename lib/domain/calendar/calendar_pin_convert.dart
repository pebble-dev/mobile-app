import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:device_calendar/device_calendar.dart';

extension CalendarEventConverter on Event {
  List<TimelineAttribute> getAttributes(Calendar calendar) {
    return [
      TimelineAttribute.icon(TimelineIcon.TIMELINE_CALENDAR),
      TimelineAttribute.title(title),
      // TODO localize
      TimelineAttribute.headings(["Description", "Calendar"]),
      TimelineAttribute.paragraphs([description, calendar.name])
    ];
  }
}
