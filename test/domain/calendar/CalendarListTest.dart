import 'dart:async';

import 'package:device_calendar/device_calendar.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:fossil/domain/Preferences.dart';
import 'package:fossil/domain/calendar/CalendarList.dart';
import 'package:fossil/domain/calendar/DeviceCalendarPluginProvider.dart';
import 'package:fossil/domain/calendar/SelectableCalendar.dart';
import 'package:hooks_riverpod/all.dart';

import '../../fakes/FakeDeviceCalendarPlugin.dart';
import '../../fakes/MemorySharedPreferences.dart';
import '../../util/test_utils.dart';

void main() {
  test('CalendarList should report list of calendars', () {
    runBlocking(() async {
      final calendarPlugin = FakeDeviceCalendarPlugin();
      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
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

      expect(container.read(calendarListProvider.state),
          expectedReceivedCalendars);
    });
  });

  test(
      'CalendarList should not report list of calendars '
      'if there is no calendar permission', () {
    runBlocking(() async {
      final calendarPlugin = FakeDeviceCalendarPlugin();
      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences()))
      ]);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A"),
        Calendar(id: "34", name: "Calendar B"),
        Calendar(id: "18", name: "Calendar C")
      ];

      calendarPlugin.reportedHasPermissionsValue = false;
      final expectedReceivedCalendars = [];

      expect(container.read(calendarListProvider.state),
          expectedReceivedCalendars);
    });
  });

  test('CalendarList should be able to disable calendar', () {
    runBlocking(() async {
      final calendarPlugin = FakeDeviceCalendarPlugin();
      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences()))
      ]);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A"),
        Calendar(id: "34", name: "Calendar B"),
        Calendar(id: "18", name: "Calendar C")
      ];

      await container
          .read(calendarListProvider)
          .setCalendarEnabled("22", false);

      final expectedReceivedCalendars = [
        SelectableCalendar("Calendar A", "22", false),
        SelectableCalendar("Calendar B", "34", true),
        SelectableCalendar("Calendar C", "18", true)
      ];

      expect(container.read(calendarListProvider.state),
          expectedReceivedCalendars);
    });
  });

  test('CalendarList should be able to re-enable calendar', () {
    runBlocking(() async {
      final calendarPlugin = FakeDeviceCalendarPlugin();
      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences()))
      ]);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A"),
        Calendar(id: "34", name: "Calendar B"),
        Calendar(id: "18", name: "Calendar C")
      ];

      await container
          .read(calendarListProvider)
          .setCalendarEnabled("22", false);
      await container.read(calendarListProvider).setCalendarEnabled("22", true);

      final expectedReceivedCalendars = [
        SelectableCalendar("Calendar A", "22", true),
        SelectableCalendar("Calendar B", "34", true),
        SelectableCalendar("Calendar C", "18", true)
      ];

      expect(container.read(calendarListProvider.state),
          expectedReceivedCalendars);
    });
  });
}
