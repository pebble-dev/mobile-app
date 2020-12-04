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
import 'package:cobble/domain/preferences.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:uuid_type/uuid_type.dart';

import '../../fakes/fake_database.dart';
import '../../fakes/fake_device_calendar_plugin.dart';
import '../../fakes/memory_shared_preferences.dart';
import '../../util/test_utils.dart';

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
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A")
      ];

      calendarPlugin.reportedEvents = [
        Event(
          "22",
          eventId: "1337",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            11, //hour
            30, //minute
          ),
        ),
        Event(
          "22",
          eventId: "1338",
          start: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            13, //hour
            30, //minute
          ),
        )
      ];

      final calendarSyncer = container.read(calendarSyncerProvider);
      final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

      final insertedEvents = await pinDao.getAllPins();

      final expectedEvents = [
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1337T1605004200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          duration: 60,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1338T1605094200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          duration: 120,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
      ];

      expectEventsWithoutItemIdAndJsonsIgnoringOrder(
          insertedEvents, expectedEvents);

      expect(anyChanges, true);
    });
  });

  test('Full sync: Do not re-add existing items', () async {
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A")
      ];

      calendarPlugin.reportedEvents = [
        Event(
          "22",
          eventId: "1337",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            11, //hour
            30, //minute
          ),
        ),
        Event(
          "22",
          eventId: "1338",
          start: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            13, //hour
            30, //minute
          ),
        )
      ];

      await pinDao.insertOrUpdateTimelinePin(
        TimelinePin(
            itemId: Uuid("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
            parentId: CALENDAR_WATCHAPP_ID,
            backingId: "1338T1605094200000",
            timestamp: DateTime.utc(
              2020, //year
              11, //month
              11, //day
              11, //hour
              30, //minute
            ),
            duration: 120,
            type: TimelinePinType.PIN,
            isVisible: true,
            isFloating: false,
            isAllDay: false,
            persistQuickView: false,
            layout: TimelinePinLayout.CALENDAR_PIN,
            nextSyncAction: NextSyncAction.Nothing,
            attributesJson:
                """[{"id":4,"uint32":2147483669},{"id":1,"maxLength":64},{"id":25,"listOfString":["Calendar"],"maxLength":128},{"id":26,"listOfString":["Calendar A"],"maxLength":1024}]"""),
      );

      final calendarSyncer = container.read(calendarSyncerProvider);
      await calendarSyncer.syncDeviceCalendarsToDb();

      final eventsInDao = await pinDao.getAllPins();

      final expectedEvents = [
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1338T1605094200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          duration: 120,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Nothing,
        ),
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1337T1605004200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          duration: 60,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
      ];

      expectEventsWithoutItemIdAndJsonsIgnoringOrder(
          eventsInDao, expectedEvents);
    });
  });

  test('Full sync: Return false when there are no changes', () async {
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A")
      ];

      calendarPlugin.reportedEvents = [
        Event(
          "22",
          eventId: "1338",
          start: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            13, //hour
            30, //minute
          ),
        )
      ];

      await pinDao.insertOrUpdateTimelinePin(
        TimelinePin(
            itemId: Uuid("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
            parentId: CALENDAR_WATCHAPP_ID,
            backingId: "1338T1605094200000",
            timestamp: DateTime.utc(
              2020, //year
              11, //month
              11, //day
              11, //hour
              30, //minute
            ),
            duration: 120,
            type: TimelinePinType.PIN,
            isVisible: true,
            isFloating: false,
            isAllDay: false,
            persistQuickView: false,
            layout: TimelinePinLayout.CALENDAR_PIN,
            nextSyncAction: NextSyncAction.Nothing,
            attributesJson:
            """[{"id":4,"uint32":2147483669},{"id":1,"maxLength":64},{"id":25,"listOfString":["Calendar"],"maxLength":128},{"id":26,"listOfString":["Calendar A"],"maxLength":1024}]"""),
      );

      final calendarSyncer = container.read(calendarSyncerProvider);
      final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

      expect(anyChanges, false);
    });
  });

  test('Full sync: Ensure added items have attributes', () async {
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A")
      ];

      calendarPlugin.reportedEvents = [
        Event(
          "22",
          eventId: "1337",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            11, //hour
            30, //minute
          ),
          title: "Test Event",
          description: "Test Description",
        )
      ];

      final calendarSyncer = container.read(calendarSyncerProvider);
      await calendarSyncer.syncDeviceCalendarsToDb();

      final insertedEvents = await pinDao.getAllPins();
      expect(insertedEvents.length, 1);
      expect(insertedEvents.first.attributesJson,
          """[{"id":4,"uint32":2147483669},{"id":1,"string":"Test Event","maxLength":64},{"id":3,"string":"Test Description","maxLength":512},{"id":25,"listOfString":["Calendar"],"maxLength":128},{"id":26,"listOfString":["Calendar A"],"maxLength":1024}]""");
    });
  });

  test('Full sync: Update existing items', () async {
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A")
      ];

      calendarPlugin.reportedEvents = [
        Event(
          "22",
          eventId: "1338",
          start: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            13, //hour
            30, //minute
          ),
          title: "Test Event X",
          description: "Test Description X",
        )
      ];

      await pinDao.insertOrUpdateTimelinePin(
        TimelinePin(
          itemId: Uuid("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1338T1605094200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          duration: 60,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
          attributesJson: "",
        ),
      );

      final calendarSyncer = container.read(calendarSyncerProvider);
      final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

      final eventsInDao = await pinDao.getAllPins();

      final expectedEvents = [
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1338T1605094200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          duration: 120,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
      ];

      expectEventsWithoutItemIdAndJsonsIgnoringOrder(
          eventsInDao, expectedEvents);

      expect(
        eventsInDao.first.attributesJson,
        """[{"id":4,"uint32":2147483669},{"id":1,"string":"Test Event X","maxLength":64},{"id":3,"string":"Test Description X","maxLength":512},{"id":25,"listOfString":["Calendar"],"maxLength":128},{"id":26,"listOfString":["Calendar A"],"maxLength":1024}]""",
      );

      expect(anyChanges, true);
    });
  });

  test('Full sync: Mark removed events as deleted', () async {
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A")
      ];

      calendarPlugin.reportedEvents = [
        Event(
          "22",
          eventId: "1337",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            11, //hour
            30, //minute
          ),
        )
      ];

      await pinDao.insertOrUpdateTimelinePin(
        TimelinePin(
          itemId: Uuid("e440b58d-7f8e-4137-85ae-2210daf9fc51"),
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1338T1607599800000",
          timestamp: DateTime.utc(
            2020, //year
            12, //month
            10, //day
            11, //hour
            30, //minute
          ),
          duration: 120,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Nothing,
          attributesJson: "",
        ),
      );

      final calendarSyncer = container.read(calendarSyncerProvider);
      final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

      final eventsInDao = await pinDao.getAllPins();

      final expectedEvents = [
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1338T1607599800000",
          timestamp: DateTime.utc(
            2020, //year
            12, //month
            10, //day
            11, //hour
            30, //minute
          ),
          duration: 120,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Delete,
        ),
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1337T1605004200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          duration: 60,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
      ];

      expectEventsWithoutItemIdAndJsonsIgnoringOrder(
          eventsInDao, expectedEvents);

      expect(anyChanges, true);
    });
  });

  test('Full sync: Only add events between now and 3 days in the future',
      () async {
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A")
      ];

      calendarPlugin.reportedEvents = [
        // Event that starts and end in the past
        // Should not be added
        Event(
          "22",
          eventId: "1337",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            00, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            10, //minute
          ),
        ),

        // Event that starts in the past but ends in the present
        // Should be added
        Event(
          "22",
          eventId: "1338",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            11, //hour
            45, //minute
          ),
        ),

        // Event that starts and ends today
        // Should be added
        Event(
          "22",
          eventId: "1339",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            12, //hour
            00, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            15, //hour
            00, //minute
          ),
        ),

        // Event that starts and ends in 3 days
        // Should be added
        Event(
          "22",
          eventId: "1340",
          start: DateTime.utc(
            2020, //year
            11, //month
            13, //day
            12, //hour
            00, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            13, //day
            15, //hour
            00, //minute
          ),
        ),

        // Event that starts on day 3 but ends on day 4
        // Should be added
        Event(
          "22",
          eventId: "1341",
          start: DateTime.utc(
            2020, //year
            11, //month
            13, //day
            23, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            14, //day
            00, //hour
            30, //minute
          ),
        ),

        // Event that starts on day 4
        // Should not be added
        Event(
          "22",
          eventId: "1342",
          start: DateTime.utc(
            2020, //year
            11, //month
            14, //day
            10, //hour
            00, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            14, //day
            12, //hour
            00, //minute
          ),
        ),
      ];

      final calendarSyncer = container.read(calendarSyncerProvider);
      final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

      final insertedEvents = await pinDao.getAllPins();

      final expectedEvents = [
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1338T1605004200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          duration: 60 + 15,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1339T1605009600000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            12, //hour
            00, //minute
          ),
          duration: 60 * 3,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1340T1605268800000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            13, //day
            12, //hour
            00, //minute
          ),
          duration: 60 * 3,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1341T1605310200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            13, //day
            23, //hour
            30, //minute
          ),
          duration: 60,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
      ];

      expectEventsWithoutItemIdAndJsonsIgnoringOrder(
          insertedEvents, expectedEvents);

      expect(anyChanges, true);
    });
  });

  test('Full sync: Only add items from selected calendars', () async {
    await runBlocking(() async {
      final db = await createTestCobbleDatabase();
      final calendarPlugin = FakeDeviceCalendarPlugin();

      final container = ProviderContainer(overrides: [
        deviceCalendarPluginProvider.overrideWithValue(calendarPlugin),
        sharedPreferencesProvider
            .overrideWithValue(Future.value(MemorySharedPreferences())),
        currentDateTimeProvider.overrideWithValue(nowProvider),
        databaseProvider.overrideWithValue(AsyncValue.data(db)),
        currentDateTimeProvider.overrideWithValue(() => now)
      ]);

      final pinDao = container.read(timelinePinDaoProvider);
      final calendarList = container.read(calendarListProvider);

      calendarPlugin.reportedCalendars = [
        Calendar(id: "22", name: "Calendar A"),
        Calendar(id: "23", name: "Calendar B"),
      ];

      calendarPlugin.reportedEvents = [
        Event(
          "22",
          eventId: "1337",
          start: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            11, //hour
            30, //minute
          ),
        ),
        Event(
          "23",
          eventId: "1338",
          start: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            11, //hour
            30, //minute
          ),
          end: DateTime.utc(
            2020, //year
            11, //month
            11, //day
            13, //hour
            30, //minute
          ),
        )
      ];

      calendarList.setCalendarEnabled("23", false);

      final calendarSyncer = container.read(calendarSyncerProvider);
      final anyChanges = await calendarSyncer.syncDeviceCalendarsToDb();

      final insertedEvents = await pinDao.getAllPins();

      final expectedEvents = [
        TimelinePin(
          itemId: null,
          parentId: CALENDAR_WATCHAPP_ID,
          backingId: "1337T1605004200000",
          timestamp: DateTime.utc(
            2020, //year
            11, //month
            10, //day
            10, //hour
            30, //minute
          ),
          duration: 60,
          type: TimelinePinType.PIN,
          isVisible: true,
          isFloating: false,
          isAllDay: false,
          persistQuickView: false,
          layout: TimelinePinLayout.CALENDAR_PIN,
          nextSyncAction: NextSyncAction.Upload,
        ),
      ];

      expectEventsWithoutItemIdAndJsonsIgnoringOrder(
          insertedEvents, expectedEvents);

      expect(anyChanges, true);
    });
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
        itemId: Uuid("00000000-0000-0000-0000-000000000000"),
        actionsJson: "[IGNORED]",
        attributesJson: "[IGNORED]",
      ));

  final actualWithoutJsons = actual.map((e) => e.copyWith(
        itemId: Uuid("00000000-0000-0000-0000-000000000000"),
        actionsJson: "[IGNORED]",
        attributesJson: "[IGNORED]",
      ));

  expect(actualWithoutJsons.toSet(), expectedWithoutJsons.toSet());
}
