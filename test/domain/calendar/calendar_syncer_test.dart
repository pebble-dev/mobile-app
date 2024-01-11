import 'dart:async';

import 'package:cobble/domain/calendar/calendar_list.dart';
import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/domain/date/date_providers.dart';
import 'package:cobble/domain/db/cobble_database.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/db/models/timeline_pin_layout.dart';
import 'package:cobble/domain/db/models/timeline_pin_type.dart';
import 'package:cobble/domain/permissions.dart';
import 'package:cobble/domain/preferences.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';
import 'package:sqflite/sqflite.dart';

import '../../fakes/fake_database.dart';
import '../../fakes/fake_device_calendar_plugin.dart';
import '../../fakes/fake_permissions_check.dart';
import '../../fakes/memory_shared_preferences.dart';
import 'package:timezone/timezone.dart' as tz;

void main() async {
  // test current time = 2020-11-10 T 11:30 Z
  final now = DateTime.utc(
    2020, //year
    11, //month
    10, //day
    11, //hour
    30, //minute
  );

  final nowProvider = () => now;

  test('Full sync: Add new items to calendar', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1337",
        start: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            10, //Hour
            30, // Minute
          ),
          tz.local
      ),
      end: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            11, //Hour
            30, // Minute
          ),
          tz.local
      ),
      ),
      Event(
        "22",
        eventId: "1338",
        start: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              11, //Hour
              30, // Minute
            ),
            tz.local
        ),
        end: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              13, //Hour
              30, // Minute
            ),
            tz.local
        ),
      )
    ];

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    final insertedEvents = await pinDao.getAllPins();

    final expectedEvents = [
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1337T1605004200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(
        insertedEvents, expectedEvents);

    expect(anyChanges, true);
  });

  test('Full sync: Do not re-add existing items', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1337",
        start: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            10, //Hour
            30, // Minute
          ),
          tz.local
      ),
      end: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            11, //Hour
            30, // Minute
          ),
          tz.local
      ),
      ),
      Event(
        "22",
        eventId: "1338",
        start: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              11, //Hour
              30, // Minute
            ),
            tz.local
        ),
        end: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              13, //Hour
              30, // Minute
            ),
            tz.local
        ),
      )
    ];

    await pinDao.insertOrUpdateTimelinePin(
      TimelinePin(
        itemId: Uuid.parse("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Nothing,
        attributesJson:
            """[{"id":4,"uint32":2147483669},{"id":1,"maxLength":64},{"id":25,"listOfString":["Calendar"],"maxLength":128},{"id":26,"listOfString":["Calendar A"],"maxLength":1024}]""",
        actionsJson:
            """[{"actionId":0,"actionType":2,"attributes":[{"id":1,"string":"Remove","maxLength":64}]},{"actionId":1,"actionType":2,"attributes":[{"id":1,"string":"Mute calendar","maxLength":64}]}]""",
      ),
    );

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    await calendarSyncer.syncDeviceCalendarsToDb();

    final eventsInDao = await pinDao.getAllPins();

    final expectedEvents = [
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Nothing,
      ),
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1337T1605004200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(eventsInDao, expectedEvents);
  });

  test('Full sync: Return false when there are no changes', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1338",
        start: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              11, //Hour
              30, // Minute
            ),
            tz.local
        ),
        end: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              13, //Hour
              30, // Minute
            ),
            tz.local
        ),
      )
    ];

    await pinDao.insertOrUpdateTimelinePin(
      TimelinePin(
        itemId: Uuid.parse("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Nothing,
        attributesJson:
            """[{"id":4,"uint32":2147483669},{"id":1,"maxLength":64},{"id":25,"listOfString":["Calendar"],"maxLength":128},{"id":26,"listOfString":["Calendar A"],"maxLength":1024}]""",
        actionsJson:
            """[{"actionId":0,"actionType":2,"attributes":[{"id":1,"string":"Remove","maxLength":64}]},{"actionId":1,"actionType":2,"attributes":[{"id":1,"string":"Mute calendar","maxLength":64}]}]""",
      ),
    );

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    expect(anyChanges, false);
  });

  test('Full sync: Ensure added items have attributes', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    // ignore: unnecessary_non_null_assertion
    final pinDao = container.read(timelinePinDaoProvider!);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1337",
        start: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            10, //Hour
            30, // Minute
          ),
          tz.local
      ),
      end: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            11, //Hour
            30, // Minute
          ),
          tz.local
      ),
        title: "Test Event",
        description: "Test Description",
      )
    ];

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    await calendarSyncer.syncDeviceCalendarsToDb();

    final insertedEvents = await pinDao.getAllPins();
    expect(insertedEvents.length, 1);
    expect(insertedEvents.first.attributesJson,
        """[{"id":4,"uint32":2147483669},{"id":1,"string":"Test Event","maxLength":64},{"id":25,"listOfString":["","Calendar"],"maxLength":128},{"id":26,"listOfString":["Test Description","Calendar A"],"maxLength":1024}]""");
  });

  test('Full sync: Update existing items', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1338",
        start: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              11, //Hour
              30, // Minute
            ),
            tz.local
        ),
        end: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              13, //Hour
              30, // Minute
            ),
            tz.local
        ),
        title: "Test Event X",
        description: "Test Description X",
      )
    ];

    await pinDao.insertOrUpdateTimelinePin(
      TimelinePin(
        itemId: Uuid.parse("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
        attributesJson: "",
      ),
    );

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    final eventsInDao = await pinDao.getAllPins();

    final expectedEvents = [
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(eventsInDao, expectedEvents);

    expect(
      eventsInDao.first.attributesJson,
      """[{"id":4,"uint32":2147483669},{"id":1,"string":"Test Event X","maxLength":64},{"id":25,"listOfString":["","Calendar"],"maxLength":128},{"id":26,"listOfString":["Test Description X","Calendar A"],"maxLength":1024}]""",
    );

    expect(anyChanges, true);
  });

  test('Full sync: Do not trigger upload on ignored items', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1338",
        start: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              11, //Hour
              30, // Minute
            ),
            tz.local
        ),
        end: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              13, //Hour
              30, // Minute
            ),
            tz.local
        ),
        title: "Test Event X",
        description: "Test Description X",
      )
    ];

    await pinDao.insertOrUpdateTimelinePin(
      TimelinePin(
        itemId: Uuid.parse("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Ignore,
        attributesJson: "",
      ),
    );

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    final eventsInDao = await pinDao.getAllPins();

    final expectedEvents = [
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1338T1605094200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          11, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Ignore,
      ),
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(eventsInDao, expectedEvents);

    expect(
      eventsInDao.first.attributesJson,
      """[{"id":4,"uint32":2147483669},{"id":1,"string":"Test Event X","maxLength":64},{"id":25,"listOfString":["","Calendar"],"maxLength":128},{"id":26,"listOfString":["Test Description X","Calendar A"],"maxLength":1024}]""",
    );

    expect(anyChanges, true);
  });

  test('Full sync: Mark removed events as deleted', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    // ignore: unnecessary_non_null_assertion
    final pinDao = container.read(timelinePinDaoProvider!);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1337",
        start: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            10, //Hour
            30, // Minute
          ),
          tz.local
      ),
      end: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            11, //Hour
            30, // Minute
          ),
          tz.local
      ),
      )
    ];

    await pinDao.insertOrUpdateTimelinePin(
      TimelinePin(
        itemId: Uuid.parse("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
        parentId: calendarWatchappId,
        backingId: "22T1338T1607599800000",
        timestamp: DateTime.utc(
          2020, //year
          12, //month
          10, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Nothing,
        attributesJson: "",
      ),
    );

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    final eventsInDao = await pinDao.getAllPins();

    final expectedEvents = [
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1338T1607599800000",
        timestamp: DateTime.utc(
          2020, //year
          12, //month
          10, //day
          11, //hour
          30, //minute
        ),
        duration: 120,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Delete,
      ),
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1337T1605004200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(eventsInDao, expectedEvents);

    expect(anyChanges, true);
  });

  test('Full sync: Delete past events from db without any watch sync',
      () async {
    // Watch seems to delete the events on their own when they go out of the
    // past timeline. Just delete them from the db and pretend they don't
    // exist anymore

    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1337",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          7, //day
          10, //hour
          30, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          7, //day
          11, //hour
          30, //minute
        ), tz.local),
      ),
      Event(
        "22",
        eventId: "1338",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          11, //hour
          00, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          12, //hour
          00, //minute
        ), tz.local),
      )
    ];

    await pinDao.insertOrUpdateTimelinePin(
      TimelinePin(
        itemId: Uuid.parse("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
        parentId: calendarWatchappId,
        backingId: "22T1337T1604745000000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          7, //day
          10, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Nothing,
        attributesJson: "",
      ),
    );

    await pinDao.insertOrUpdateTimelinePin(
      TimelinePin(
        itemId: Uuid.parse("24b88efe-6b43-41cd-a1f6-06b0e5940f94"),
        parentId: calendarWatchappId,
        backingId: "22T1338T1605006000000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          11, //hour
          00, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Nothing,
        attributesJson: "",
      ),
    );

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    final eventsInDao = await pinDao.getAllPins();

    final List<TimelinePin> expectedEvents = [
      TimelinePin(
        itemId: Uuid.parse("24b88efe-6b43-41cd-a1f6-06b0e5940f94"),
        parentId: calendarWatchappId,
        backingId: "22T1338T1605006000000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          11, //hour
          00, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
        attributesJson: "",
      )
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(eventsInDao, expectedEvents);

    expect(anyChanges, true);
  });

  test('Full sync: Only add events between now and 6 days in the future',
      // Pebble only displays 3 days, but we sync 6 days to allow watch to
      // have a bit of standalone buffer in case it is disconnected from
      // the phone
      () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);

    calendarPlugin.reportedCalendars = [Calendar(id: "22", name: "Calendar A")];

    calendarPlugin.reportedEvents = [
      // Event that starts and end in the past
      // Should not be added
      Event(
        "22",
        eventId: "1337",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          00, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          10, //minute
        ), tz.local),
      ),

      // Event that starts in the past but ends in the present
      // Should be added
      Event(
        "22",
        eventId: "1338",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          30, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          11, //hour
          45, //minute
        ), tz.local),
      ),

      // Event that starts and ends today
      // Should be added
      Event(
        "22",
        eventId: "1339",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          12, //hour
          00, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          10, //day
          15, //hour
          00, //minute
        ), tz.local),
      ),

      // Event that starts and ends in 3 days
      // Should be added
      Event(
        "22",
        eventId: "1340",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          13, //day
          12, //hour
          00, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          13, //day
          15, //hour
          00, //minute
        ), tz.local),
      ),

      // Event that starts on day 6 but ends on day 7
      // Should be added
      Event(
        "22",
        eventId: "1341",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          16, //day
          23, //hour
          30, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          17, //day
          00, //hour
          30, //minute
        ), tz.local),
      ),

      // Event that starts on day 7
      // Should not be added
      Event(
        "22",
        eventId: "1342",
        start: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          17, //day
          10, //hour
          00, //minute
        ), tz.local),
        end: tz.TZDateTime.from(DateTime.utc(
          2020, //year
          11, //month
          17, //day
          12, //hour
          00, //minute
        ), tz.local),
      ),
    ];

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    final insertedEvents = await pinDao.getAllPins();

    final expectedEvents = [
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1338T1605004200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          30, //minute
        ),
        duration: 60 + 15,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1339T1605009600000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          12, //hour
          00, //minute
        ),
        duration: 60 * 3,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1340T1605268800000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          13, //day
          12, //hour
          00, //minute
        ),
        duration: 60 * 3,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1341T1605569400000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          16, //day
          23, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(
        insertedEvents, expectedEvents);

    expect(anyChanges, true);
  });

  test('Full sync: Only add items from selected calendars', () async {
    final db = await createTestCobbleDatabase();
    final calendarPlugin = FakeDeviceCalendarPlugin();

    final container = ProviderContainer(overrides: [
      deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
      sharedPreferencesProvider
          .overrideWithValue(Future.value(MemorySharedPreferences())),
      currentDateTimeProvider.overrideWithValue(nowProvider),
      databaseProvider.overrideWithProvider(AutoDisposeFutureProvider((ref) async* { yield AsyncValue.data(db); } as FutureOr<Database> Function(AutoDisposeFutureProviderRef<Database>))),
      currentDateTimeProvider.overrideWithValue(() => now),
      permissionCheckProvider.overrideWithValue(FakePermissionCheck())
    ]);

    final pinDao = container.read(timelinePinDaoProvider);
    final calendarList = container.read(calendarListProvider.notifier);

    calendarPlugin.reportedCalendars = [
      Calendar(id: "22", name: "Calendar A"),
      Calendar(id: "23", name: "Calendar B"),
    ];

    calendarPlugin.reportedEvents = [
      Event(
        "22",
        eventId: "1337",
        start: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            10, //Hour
            30, // Minute
          ),
          tz.local
      ),
      end: tz.TZDateTime.from(
          DateTime.utc(
            2020, // Year
            11, // Month
            21, // Day
            11, //Hour
            30, // Minute
          ),
          tz.local
      ),
      ),
      Event(
        "23",
        eventId: "1338",
        start: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              11, //Hour
              30, // Minute
            ),
            tz.local
        ),
        end: tz.TZDateTime.from(
            DateTime.utc(
              2020, // Year
              11, // Month
              11, // Day
              13, //Hour
              30, // Minute
            ),
            tz.local
        ),
      )
    ];

    calendarList.setCalendarEnabled("23", false);

    final calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

    final insertedEvents = await pinDao.getAllPins();

    final expectedEvents = [
      TimelinePin(
        itemId: null,
        parentId: calendarWatchappId,
        backingId: "22T1337T1605004200000",
        timestamp: DateTime.utc(
          2020, //year
          11, //month
          10, //day
          10, //hour
          30, //minute
        ),
        duration: 60,
        type: TimelinePinType.pin,
        isVisible: true,
        isFloating: false,
        isAllDay: false,
        persistQuickView: false,
        layout: TimelinePinLayout.calendarPin,
        nextSyncAction: NextSyncAction.Upload,
      ),
    ];

    expectEventsWithoutItemIdAndJsonsIgnoringOrder(
        insertedEvents, expectedEvents);

    expect(anyChanges, true);
  });
}

/// a) Item Id generation is random and thus shouldn't be compared
///
/// b) layout and action JSON generation is out of scope for this test file
/// (apart from basic test to see if generated JSONs make it into database)
///
/// c) Order of insertion of the database is not relevant
///
/// Use this method to ignore all above problems when comparing.
void expectEventsWithoutItemIdAndJsonsIgnoringOrder(
  List<TimelinePin> actual,
  List<TimelinePin> expected,
) {
  final expectedWithoutJsons = expected.map((e) => e.copyWith(
        itemId: Uuid.parse("00000000-0000-0000-0000-000000000000"),
        actionsJson: "[IGNORED]",
        attributesJson: "[IGNORED]",
      ));

  final actualWithoutJsons = actual.map((e) => e.copyWith(
        itemId: Uuid.parse("00000000-0000-0000-0000-000000000000"),
        actionsJson: "[IGNORED]",
        attributesJson: "[IGNORED]",
      ));

  expect(actualWithoutJsons.toSet(), expectedWithoutJsons.toSet());
}
