import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';

import 'CalendarPermission.dart';
import 'SelectableCalendar.dart';

class CalendarList extends StateNotifier<List<SelectableCalendar>> {
  bool hasPermission;

  CalendarList(this.hasPermission) : super(const []) {
    _load();
  }

  void _load() async {
    if (!hasPermission) {
      return;
    }

    final calendars = await DeviceCalendarPlugin().retrieveCalendars();

    state = calendars.data
        .map((c) => SelectableCalendar(c.name, c.id, false))
        .toList();

    // do something
  }
}

final calendarListProvider = StateNotifierProvider<CalendarList>((ref) {
  final hasPermission = ref.watch(calendarPermissionProvider.state);

  return CalendarList(hasPermission);
});
