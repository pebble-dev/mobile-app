import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/util/state_provider_extension.dart';
import 'package:cobble/util/stream_extensions.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:state_notifier/state_notifier.dart';

import 'result_converter.dart';

class CalendarPermission extends StateNotifier<AsyncValue<bool>> {
  DeviceCalendarPlugin _plugin;

  CalendarPermission(this._plugin) : super(AsyncValue.loading()) {
    _load();
  }

  Future<void> _load() async {
    state = (await _plugin.hasPermissions()).toAsyncValue();
  }

  Future<bool> requestPermission() async {
    final data = (await streamWithExistingValue.firstSuccessOrError()).data;

    if (data != null) {
      final newValue = await _plugin.requestPermissions();
      state = newValue.toAsyncValue();
      return true;
    } else {
      return false;
    }
  }
}

final calendarPermissionProvider =
    StateNotifierProvider<CalendarPermission>((ctx) {
  final calendarPlugin = ctx.read(deviceCalendarPluginProvider);
  return CalendarPermission(calendarPlugin);
});
