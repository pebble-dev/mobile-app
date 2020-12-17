import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/domain/calendar/selectable_calendar.dart';
import 'package:cobble/domain/date/date_providers.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/timeline/attribute_serializer.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:uuid_type/uuid_type.dart';

import 'calendar_list.dart';
import 'calendar_pin_convert.dart';

/// Sync controller that handles synchronization between on-device calendar and
/// internal database.
class CalendarSyncer {
  final CalendarList _calendarList;
  final DeviceCalendarPlugin _deviceCalendarPlugin;
  final DateTimeProvider _dateTimeProvider;
  final TimelinePinDao _timelinePinDao;

  final _uuidGenerator = RandomBasedUuidGenerator();

  /// Sync all calendar changes from device calendar to DB.
  ///
  /// Returns true if there were any changes or false if here were none
  Future<bool> syncDeviceCalendarsToDb() async {
    final allCalendarsResult = await _calendarList.load();
    if (!(allCalendarsResult is AsyncData)) {
      return false;
    }

    final allCalendars = allCalendarsResult.data.value;

    final now = _dateTimeProvider();
    // 1 day is added since we need to get the start of the next day
    // 1 day is added for the 1-day sync buffer
    final syncEndDate =
        _getStartOfDay(now.add(Duration(days: _SYNC_RANGE_DAYS + 2)));

    final retrieveEventParams =
        RetrieveEventsParams(startDate: now, endDate: syncEndDate);

    final List<_EventInCalendar> allCalendarEvents = [];
    for (final calendar in allCalendars) {
      if (!calendar.enabled) {
        continue;
      }

      final result = await _deviceCalendarPlugin.retrieveEvents(
          calendar.id, retrieveEventParams);

      if (!result.isSuccess) {
        //TODO log calendar error
        return false;
      }

      for (final event in result.data) {
        allCalendarEvents.add(_EventInCalendar(calendar, event));
      }
    }

    bool anyChanges = false;

    final newPins = allCalendarEvents.map((e) => e.event.generateBasicEventData(
          serializeAttributesToJson(e.event.getAttributes(e.calendar)),
          null,
        ));

    final existingPins =
        (await _timelinePinDao.getPinsFromParent(CALENDAR_WATCHAPP_ID))
            .toList();

    for (TimelinePin newPin in newPins) {
      final existingPin = existingPins.firstWhere(
        (element) => element.backingId == newPin.backingId,
        orElse: () => null,
      );

      if (existingPin != null &&
          existingPin.duration == newPin.duration &&
          existingPin.isAllDay == newPin.isAllDay &&
          existingPin.attributesJson == newPin.attributesJson &&
          existingPin.actionsJson == newPin.actionsJson) {
        continue;
      }

      Uuid newItemId =
          existingPin != null ? existingPin.itemId : _uuidGenerator.generate();

      newPin = newPin.copyWith(itemId: newItemId);

      _timelinePinDao.insertOrUpdateTimelinePin(newPin);
      anyChanges = true;
    }

    for (final pin in existingPins) {
      if (pin.timestamp.add(Duration(minutes: pin.duration)).isBefore(now)) {
        await _timelinePinDao.delete(pin.itemId);
        anyChanges = true;
      } else if (!newPins.any((newPin) => newPin.backingId == pin.backingId)) {
        await _timelinePinDao.setSyncAction(pin.itemId, NextSyncAction.Delete);
        anyChanges = true;
      }
    }

    return anyChanges;
  }

  DateTime _getStartOfDay(DateTime date) {
    return date.subtract(Duration(
      hours: date.hour,
      minutes: date.minute,
      seconds: date.second,
      milliseconds: date.millisecond,
      microseconds: date.microsecond,
    ));
  }

  CalendarSyncer(this._calendarList,
      this._deviceCalendarPlugin,
      this._dateTimeProvider,
      this._timelinePinDao,);
}

class _EventInCalendar {
  final SelectableCalendar calendar;
  final Event event;

  _EventInCalendar(this.calendar, this.event);
}

final calendarSyncerProvider = Provider.autoDispose<CalendarSyncer>((ref) {
  final calendarList = ref.watch(calendarListProvider);
  final deviceCalendar = ref.watch(deviceCalendarPluginProvider);
  final dateTimeProvider = ref.watch(currentDateTimeProvider);
  final timelinePinDao = ref.watch(timelinePinDaoProvider);

  return CalendarSyncer(
      calendarList, deviceCalendar, dateTimeProvider, timelinePinDao);
});

/// Only sync events between now and following days in the future
const _SYNC_RANGE_DAYS = 3;
