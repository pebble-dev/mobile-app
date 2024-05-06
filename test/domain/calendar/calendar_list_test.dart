import 'dart:async';

import 'package:cobble/domain/calendar/calendar_list.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/domain/calendar/selectable_calendar.dart';
import 'package:cobble/domain/permissions.dart';
import 'package:cobble/domain/preferences.dart';
import 'package:cobble/util/container_extensions.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../fakes/fake_device_calendar_plugin.dart';
import '../../fakes/fake_permissions_check.dart';
import '../../fakes/memory_shared_preferences.dart';

void main() {
  test('CalendarList should report list of calendars', () async {
    final calendarPlugin = FakeDeviceCalendarPlugin();
    final permissionCheck = FakePermissionCheck();
    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      permissionCheckProvider.overrideWithValue(permissionCheck),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences()))
    ]);

    calendarPlugin.reportedCalendars = [
      Calendar(id: "22", name: "Calendar A", color: 0xFFFFFFFF),
      Calendar(id: "34", name: "Calendar B", color: 0xFFFFFFFF),
      Calendar(id: "18", name: "Calendar C", color: 0xFFFFFFFF)
    ];

    final expectedReceivedCalendars = [
      SelectableCalendar("Calendar A", "22", true, 0xFFFFFFFF),
      SelectableCalendar("Calendar B", "34", true, 0xFFFFFFFF),
      SelectableCalendar("Calendar C", "18", true, 0xFFFFFFFF)
    ];

    final receivedCalendars = (await container
            .readUntilFirstSuccessOrError(calendarListProvider))
        .value;

    expect(receivedCalendars, expectedReceivedCalendars);
  });

  test(
      'CalendarList should not report list of calendars '
      'if there is no calendar permission', () async {
    final calendarPlugin = FakeDeviceCalendarPlugin();
    final permissionCheck = FakePermissionCheck();
    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      permissionCheckProvider.overrideWithValue(permissionCheck),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences()))
    ]);

    calendarPlugin.reportedCalendars = [
      Calendar(id: "22", name: "Calendar A"),
      Calendar(id: "34", name: "Calendar B"),
      Calendar(id: "18", name: "Calendar C")
    ];

    permissionCheck.reportedCalendarPermission = false;

    final receivedCalendars = await container
        .readUntilFirstSuccessOrError(calendarListProvider);

    expect(receivedCalendars, isA<AsyncError>());
  });

  test('CalendarList should be able to disable calendar', () async {
    final calendarPlugin = FakeDeviceCalendarPlugin();
    final permissionCheck = FakePermissionCheck();
    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      permissionCheckProvider.overrideWithValue(permissionCheck),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences()))
    ]);

    calendarPlugin.reportedCalendars = [
      Calendar(id: "22", name: "Calendar A", color: 0xFFFFFFFF),
      Calendar(id: "34", name: "Calendar B", color: 0xFFFFFFFF),
      Calendar(id: "18", name: "Calendar C", color: 0xFFFFFFFF)
    ];

    await container
        .listen<dynamic>(calendarListProvider.notifier, (previous, value) {})
        .read()
        .setCalendarEnabled("22", false);

    final expectedReceivedCalendars = [
      SelectableCalendar("Calendar A", "22", false, 0xFFFFFFFF),
      SelectableCalendar("Calendar B", "34", true, 0xFFFFFFFF),
      SelectableCalendar("Calendar C", "18", true, 0xFFFFFFFF)
    ];

    final receivedCalendars = (await container
            .readUntilFirstSuccessOrError(calendarListProvider))
        .value;

    expect(receivedCalendars, expectedReceivedCalendars);
  });

  test('CalendarList should be able to re-enable calendar', () async {
    final calendarPlugin = FakeDeviceCalendarPlugin();
    final permissionCheck = FakePermissionCheck();
    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      permissionCheckProvider.overrideWithValue(permissionCheck),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences()))
    ]);

    calendarPlugin.reportedCalendars = [
      Calendar(id: "22", name: "Calendar A", color: 0xFFFFFFFF),
      Calendar(id: "34", name: "Calendar B", color: 0xFFFFFFFF),
      Calendar(id: "18", name: "Calendar C", color: 0xFFFFFFFF)
    ];

    await container
        .listen<CalendarList>(calendarListProvider.notifier, (previous, value) {})
        .read()
        .setCalendarEnabled("22", false);
    await container
        .listen<CalendarList>(calendarListProvider.notifier, (previous, value) {})
        .read()
        .setCalendarEnabled("22", true);

    final expectedReceivedCalendars = [
      SelectableCalendar("Calendar A", "22", true, 0xFFFFFFFF),
      SelectableCalendar("Calendar B", "34", true, 0xFFFFFFFF),
      SelectableCalendar("Calendar C", "18", true, 0xFFFFFFFF)
    ];

    final receivedCalendars = (await container
            .readUntilFirstSuccessOrError(calendarListProvider))
        .value;

    expect(receivedCalendars, expectedReceivedCalendars);
  });
}
