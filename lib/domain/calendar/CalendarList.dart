import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../Preferences.dart';
import 'CalendarPermission.dart';
import 'SelectableCalendar.dart';

class CalendarList extends StateNotifier<List<SelectableCalendar>> {
  final bool hasPermission;
  final Future<SharedPreferences> preferencesFuture;
  List<String> blacklistedCalendars = List.empty();

  CalendarList(this.hasPermission, this.preferencesFuture) : super(const []) {
    _load();
  }

  Future<void> _load() async {
    if (!hasPermission) {
      return;
    }

    final preferences = await preferencesFuture;
    blacklistedCalendars =
        preferences.getStringList(_PREFERENCES_KEY_BLACKLISTED_CALENDARS) ??
            List.empty();

    final calendars = await DeviceCalendarPlugin().retrieveCalendars();

    state = calendars.data
        .map((c) => SelectableCalendar(
            c.name, c.id, !blacklistedCalendars.contains(c.id)))
        .toList();

    // do something
  }

  void setCalendarEnabled(String id, bool enabled) async {
    if (enabled && blacklistedCalendars.contains(id)) {
      final newBlacklist =
          blacklistedCalendars.where((element) => element != id).toList();
      final preferences = await preferencesFuture;

      blacklistedCalendars = newBlacklist;
      await preferences.setStringList(
          _PREFERENCES_KEY_BLACKLISTED_CALENDARS, newBlacklist);
      await _load();
    } else if (!enabled && !blacklistedCalendars.contains(id)) {
      final newBlacklist = [...blacklistedCalendars, id];
      final preferences = await preferencesFuture;

      blacklistedCalendars = newBlacklist;
      await preferences.setStringList(
          _PREFERENCES_KEY_BLACKLISTED_CALENDARS, newBlacklist);
      await _load();
    }
  }
}

final calendarListProvider = StateNotifierProvider<CalendarList>((ref) {
  final hasPermission = ref.watch(calendarPermissionProvider.state);
  final sharedPreferences = ref.read(sharedPreferencesProvider);

  return CalendarList(hasPermission, sharedPreferences);
});

const _PREFERENCES_KEY_BLACKLISTED_CALENDARS = "blacklisted_calendars";
