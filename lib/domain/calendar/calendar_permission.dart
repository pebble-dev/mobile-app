import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:state_notifier/state_notifier.dart';

class CalendarPermission extends StateNotifier<bool> {
  DeviceCalendarPlugin _plugin;
  Future<void> loadFuture;

  CalendarPermission(this._plugin) : super(false) {
    loadFuture = _load();
  }

  Future<bool> hasCalendarPermission() async {
    await loadFuture;
    return state;
  }

  Future<void> _load() async {
    state = (await _plugin.hasPermissions()).data;
  }

  void requestPermission() {
    if (!state) {
      _plugin.requestPermissions().then((value) {
        state = value.data;
      });
    }
  }
}

final calendarPermissionProvider = StateNotifierProvider((ctx) {
  final calendarPlugin = ctx.read(deviceCalendarPluginProvider);
  return CalendarPermission(calendarPlugin);
});
