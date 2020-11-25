import 'package:device_calendar/device_calendar.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:state_notifier/state_notifier.dart';

class CalendarPermission extends StateNotifier<bool> {
  DeviceCalendarPlugin _plugin;

  CalendarPermission(this._plugin) : super(false) {
    _plugin.hasPermissions().then((value) {
      state = value.data;
    });
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
