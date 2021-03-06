import 'package:cobble/domain/permissions.dart';
import 'package:cobble/domain/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'device_calendar_plugin_provider.dart';
import 'selectable_calendar.dart';

class CalendarList extends StateNotifier<AsyncValue<List<SelectableCalendar>>> {
  final DeviceCalendarPlugin _deviceCalendarPlugin;
  final PermissionCheck _permissionCheck;
  final Future<SharedPreferences> _preferencesFuture;
  List<String>? _blacklistedCalendars = List.empty();

  CalendarList(this._deviceCalendarPlugin, this._permissionCheck,
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
    final hasCalendarPermission =
        await _permissionCheck.hasCalendarPermission();

    if (hasCalendarPermission.value == false) {
      return AsyncValue.error([ResultError(0, "No permission")]);
    }

    final preferences = await _preferencesFuture;
    await preferences.reload();
    _blacklistedCalendars =
        preferences.getStringList(_preferencesKeyBlacklistedCalendars) ??
            List.empty();

    final calendars = await _deviceCalendarPlugin.retrieveCalendars();
    if (!calendars.isSuccess) {
      return AsyncValue.error(calendars.errors);
    } else {
      return AsyncValue.data(calendars.data
          .map((c) => SelectableCalendar(
              c.name, c.id, !_blacklistedCalendars!.contains(c.id)))
          .toList());
    }
  }

  Future<void> setCalendarEnabled(String id, bool enabled) async {
    List<String>? newBlacklist;
    if (enabled && _blacklistedCalendars!.contains(id)) {
      newBlacklist =
          _blacklistedCalendars!.where((element) => element != id).toList();
    } else if (!enabled && !_blacklistedCalendars!.contains(id)) {
      newBlacklist = [..._blacklistedCalendars!, id];
    }

    final preferences = await _preferencesFuture;

    _blacklistedCalendars = newBlacklist;
    await preferences.setStringList(
      _preferencesKeyBlacklistedCalendars,
      newBlacklist!,
    );
    await _refresh();
  }
}

final AutoDisposeStateNotifierProvider<CalendarList>? calendarListProvider =
    StateNotifierProvider.autoDispose<CalendarList>((ref) {
  // Use auto-dispose to ensure calendar list is reloaded every time user
  // re-opens the screen since we cannot propagate change notifications
  // between background and UI isolate

  final deviceCalendarPlugin = ref.read(deviceCalendarPluginProvider);
  final permissionCheck = ref.watch(permissionCheckProvider);
  final sharedPreferences = ref.read(sharedPreferencesProvider);

  return CalendarList(deviceCalendarPlugin, permissionCheck, sharedPreferences);
});

const _preferencesKeyBlacklistedCalendars = "blacklisted_calendars";
