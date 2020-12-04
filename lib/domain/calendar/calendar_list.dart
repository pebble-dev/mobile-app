import 'package:device_calendar/device_calendar.dart';
import 'package:cobble/domain/calendar/calendar_permission.dart';
import 'package:cobble/domain/preferences.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'device_calendar_plugin_provider.dart';
import 'selectable_calendar.dart';

class CalendarList extends StateNotifier<List<SelectableCalendar>> {
  final DeviceCalendarPlugin _deviceCalendarPlugin;
  final bool _hasPermission;
  final Future<SharedPreferences> _preferencesFuture;
  List<String> _blacklistedCalendars = List.empty();

  CalendarList(
      this._deviceCalendarPlugin, this._hasPermission, this._preferencesFuture)
      : super(const []) {
    _load();
  }

  List<SelectableCalendar> getAllCalendars() {
    return state;
  }
  
  Future<void> _load() async {
    if (!_hasPermission) {
      return;
    }

    final preferences = await _preferencesFuture;
    _blacklistedCalendars =
        preferences.getStringList(_PREFERENCES_KEY_BLACKLISTED_CALENDARS) ??
            List.empty();

    final calendars = await _deviceCalendarPlugin.retrieveCalendars();

    state = calendars.data
        .map((c) => SelectableCalendar(
            c.name, c.id, !_blacklistedCalendars.contains(c.id)))
        .toList();
  }

  void setCalendarEnabled(String id, bool enabled) async {
    if (enabled && _blacklistedCalendars.contains(id)) {
      final newBlacklist =
      _blacklistedCalendars.where((element) => element != id).toList();
      final preferences = await _preferencesFuture;

      _blacklistedCalendars = newBlacklist;
      await preferences.setStringList(
          _PREFERENCES_KEY_BLACKLISTED_CALENDARS, newBlacklist);
      await _load();
    } else if (!enabled && !_blacklistedCalendars.contains(id)) {
      final newBlacklist = [..._blacklistedCalendars, id];
      final preferences = await _preferencesFuture;

      _blacklistedCalendars = newBlacklist;
      await preferences.setStringList(
          _PREFERENCES_KEY_BLACKLISTED_CALENDARS, newBlacklist);
      await _load();
    }
  }
}

final calendarListProvider = StateNotifierProvider<CalendarList>((ref) {
  final deviceCalendarPlugin = ref.read(deviceCalendarPluginProvider);
  final hasPermission = ref.watch(calendarPermissionProvider.state);
  final sharedPreferences = ref.read(sharedPreferencesProvider);

  return CalendarList(deviceCalendarPlugin, hasPermission, sharedPreferences);
});

const _PREFERENCES_KEY_BLACKLISTED_CALENDARS = "blacklisted_calendars";
