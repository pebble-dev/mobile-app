import 'package:cobble/domain/calendar/calendar_permission.dart';
import 'package:cobble/domain/preferences.dart';
import 'package:cobble/util/state_provider_extension.dart';
import 'package:cobble/util/stream_extensions.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'device_calendar_plugin_provider.dart';
import 'selectable_calendar.dart';

class CalendarList extends StateNotifier<AsyncValue<List<SelectableCalendar>>> {
  final DeviceCalendarPlugin _deviceCalendarPlugin;
  final CalendarPermission _calendarPermission;
  final Future<SharedPreferences> _preferencesFuture;
  List<String> _blacklistedCalendars = List.empty();

  CalendarList(this._deviceCalendarPlugin, this._calendarPermission,
      this._preferencesFuture)
      : super(AsyncValue.loading()) {
    _refresh();
  }

  Future<void> _refresh() async {
    state = await load();
  }

  /// In-memory state is not transferred between isolates
  /// To be able to load calendar events in another isolate with
  /// proper selected calendar, we expose this load method.
  Future<AsyncValue<List<SelectableCalendar>>> load() async {
    final permissionValue =
        await _calendarPermission.streamWithExistingValue.firstSuccessOrError();

    if (permissionValue is AsyncError) {
      return AsyncValue.error((permissionValue as AsyncError).error);
    }

    if (permissionValue.data.value == false) {
      return AsyncValue.error([ResultError(0, "No permission")]);
    }

    final preferences = await _preferencesFuture;
    await preferences.reload();
    _blacklistedCalendars =
        preferences.getStringList(_PREFERENCES_KEY_BLACKLISTED_CALENDARS) ??
            List.empty();

    final calendars = await _deviceCalendarPlugin.retrieveCalendars();
    if (!calendars.isSuccess) {
      return AsyncValue.error(calendars.errors);
    } else {
      return AsyncValue.data(calendars.data
          .map((c) => SelectableCalendar(
              c.name, c.id, !_blacklistedCalendars.contains(c.id)))
          .toList());
    }
  }

  Future<void> setCalendarEnabled(String id, bool enabled) async {
    List<String> newBlacklist;
    if (enabled && _blacklistedCalendars.contains(id)) {
      newBlacklist =
          _blacklistedCalendars.where((element) => element != id).toList();
    } else if (!enabled && !_blacklistedCalendars.contains(id)) {
      newBlacklist = [..._blacklistedCalendars, id];
    }

    final preferences = await _preferencesFuture;

    _blacklistedCalendars = newBlacklist;
    await preferences.setStringList(
        _PREFERENCES_KEY_BLACKLISTED_CALENDARS, newBlacklist);
    await _refresh();
  }
}

final calendarListProvider = StateNotifierProvider<CalendarList>((ref) {
  final deviceCalendarPlugin = ref.read(deviceCalendarPluginProvider);
  final calendarPermissions = ref.watch(calendarPermissionProvider);
  final sharedPreferences = ref.read(sharedPreferencesProvider);

  return CalendarList(
      deviceCalendarPlugin, calendarPermissions, sharedPreferences);
});

const _PREFERENCES_KEY_BLACKLISTED_CALENDARS = "blacklisted_calendars";
