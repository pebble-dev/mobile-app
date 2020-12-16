import 'dart:async';

import 'package:cobble/domain/calendar/calendar_list.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/domain/calendar/selectable_calendar.dart';
import 'package:cobble/domain/permissions.dart';
import 'package:cobble/domain/preferences.dart';
import 'package:cobble/util/container_extensions.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hooks_riverpod/all.dart';

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
      Calendar(id: "22", name: "Calendar A"),
      Calendar(id: "34", name: "Calendar B"),
      Calendar(id: "18", name: "Calendar C")
    ];

    final expectedReceivedCalendars = [
      SelectableCalendar("Calendar A", "22", true),
      SelectableCalendar("Calendar B", "34", true),
      SelectableCalendar("Calendar C", "18", true)
    ];

    final receivedCalendars = (await container
            .readUntilFirstSuccessOrError(calendarListProvider.state))
        .data
        ?.value;

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
        .readUntilFirstSuccessOrError(calendarListProvider.state);

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
      Calendar(id: "22", name: "Calendar A"),
      Calendar(id: "34", name: "Calendar B"),
      Calendar(id: "18", name: "Calendar C")
    ];

    await container.read(calendarListProvider).setCalendarEnabled("22", false);

    final expectedReceivedCalendars = [
      SelectableCalendar("Calendar A", "22", false),
      SelectableCalendar("Calendar B", "34", true),
      SelectableCalendar("Calendar C", "18", true)
    ];

    final receivedCalendars = (await container
        .readUntilFirstSuccessOrError(calendarListProvider.state))
        .data
        ?.value;

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
      Calendar(id: "22", name: "Calendar A"),
      Calendar(id: "34", name: "Calendar B"),
      Calendar(id: "18", name: "Calendar C")
    ];

    await container.read(calendarListProvider).setCalendarEnabled("22", false);
    await container.read(calendarListProvider).setCalendarEnabled("22", true);

    final expectedReceivedCalendars = [
      SelectableCalendar("Calendar A", "22", true),
      SelectableCalendar("Calendar B", "34", true),
      SelectableCalendar("Calendar C", "18", true)
    ];

    final receivedCalendars = (await container
        .readUntilFirstSuccessOrError(calendarListProvider.state))
        .data
        ?.value;

    expect(receivedCalendars, expectedReceivedCalendars);
  });
}
