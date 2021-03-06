import 'dart:async';
import 'dart:io';

import 'package:cobble/background/actions/master_action_handler.dart';
import 'package:cobble/domain/calendar/calendar_list.dart';
import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/calendar/device_calendar_plugin_provider.dart';
import 'package:cobble/domain/calendar/selectable_calendar.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/timeline_action_response.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/util/state_provider_extension.dart';
import 'package:cobble/util/stream_extensions.dart';
import 'package:collection/collection.dart' show IterableExtension;
import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';

class CalendarActionHandler implements ActionHandler {
  final TimelinePinDao _dao;
  final CalendarSyncer _calendarSyncer;
  final WatchTimelineSyncer _watchSyncer;
  final CalendarList _calendarList;
  final DeviceCalendarPlugin _deviceCalendarPlugin;

  CalendarActionHandler(
    this._dao,
    this._calendarSyncer,
    this._watchSyncer,
    this._calendarList,
    this._deviceCalendarPlugin,
  );

  @override
  Future<TimelineActionResponse> handleTimelineAction(
    TimelinePin pin,
    ActionTrigger trigger,
  ) async {
    switch (trigger.actionId) {
      case calendarActionRemove:
        return _handleRemoveEventAction(pin);
      case calendarActionMuteCalendar:
        return _handleMuteCalendarAction(pin);
      case calendarActionAccept:
      case calendarActionMaybe:
      case calendarActionDecline:
        return _handleAttendanceAction(pin, trigger.actionId);
      default:
        Log.e("Unknown calendar action: ${trigger.actionId}");
        return TimelineActionResponse(false);
    }
  }

  @override
  Future<TimelineActionResponse> _handleRemoveEventAction(
      TimelinePin pin,) async {
    Future.microtask(() async {
      await _dao.setSyncAction(pin.itemId, NextSyncAction.Delete);
      await _watchSyncer.syncPinDatabaseWithWatch();
    });

    return TimelineActionResponse(true, attributes: [
      TimelineAttribute.subtitle("Removed"),
      TimelineAttribute.largeIcon(TimelineIcon.resultDismissed)
    ]);
  }

  @override
  Future<TimelineActionResponse> _handleMuteCalendarAction(TimelinePin pin,) async {
    final calendarList =
    await (_calendarList.streamWithExistingValue.firstSuccessOrError() as FutureOr<AsyncValue<List<SelectableCalendar>>>);

    final calendars = calendarList.data!.value;
    if (calendars == null) {
      return TimelineActionResponse(false);
    }

    final eventId = CalendarEventId.fromTimelinePin(pin);
    if (eventId == null) {
      return TimelineActionResponse(false);
    }

    Future.microtask(() async {
      await _calendarList.setCalendarEnabled(eventId.calendarId, false);
      await _calendarSyncer.syncDeviceCalendarsToDb();
      await _watchSyncer.syncPinDatabaseWithWatch();
    });

    return TimelineActionResponse(true, attributes: [
      TimelineAttribute.subtitle("Calendar muted"),
      TimelineAttribute.largeIcon(TimelineIcon.resultMute)
    ]);
  }

  @override
  Future<TimelineActionResponse> _handleAttendanceAction(TimelinePin pin,
      int? actionId,) async {
    final eventId = CalendarEventId.fromTimelinePin(pin);
    if (eventId == null) {
      Log.e("Unknown timeline pin backing ID ${pin.backingId}");
      return TimelineActionResponse(false);
    }

    final events = await _deviceCalendarPlugin.retrieveEvents(
      eventId.calendarId,
      RetrieveEventsParams(eventIds: [eventId.eventId]),
    );
    if (!events.isSuccess || events.data.isEmpty) {
      Log.e("Unknown event ${eventId.eventId}");
      return TimelineActionResponse(false);
    }

    final event = events.data.first;
    final selfAttendee = event.attendees.firstWhereOrNull(
          (element) => element.isCurrentUser == true,
    );

    if (selfAttendee == null) {
      Log.e("Event does not have self attendee");
      return TimelineActionResponse(false);
    }

    if (Platform.isAndroid) {
      AndroidAttendanceStatus targetAttendanceStatus;
      switch (actionId) {
        case calendarActionAccept:
          targetAttendanceStatus = AndroidAttendanceStatus.Accepted;
          break;
        case calendarActionMaybe:
          targetAttendanceStatus = AndroidAttendanceStatus.Tentative;
          break;
        case calendarActionDecline:
          targetAttendanceStatus = AndroidAttendanceStatus.Declined;
          break;
        default:
          Log.e("Unknown action $actionId");
          return TimelineActionResponse(false);
      }

      selfAttendee.androidAttendeeDetails = AndroidAttendeeDetails(
        role: selfAttendee.androidAttendeeDetails.role,
        attendanceStatus: targetAttendanceStatus,
      );
    }
    else if (Platform.isIOS) {
      IosAttendanceStatus targetAttendanceStatus;
      switch (actionId) {
        case calendarActionAccept:
          targetAttendanceStatus = IosAttendanceStatus.Accepted;
          break;
        case calendarActionMaybe:
          targetAttendanceStatus = IosAttendanceStatus.Tentative;
          break;
        case calendarActionDecline:
          targetAttendanceStatus = IosAttendanceStatus.Declined;
          break;
        default:
          Log.e("Unknown action $actionId");
          return TimelineActionResponse(false);
      }

      selfAttendee.iosAttendeeDetails = IosAttendeeDetails(
        role: selfAttendee.iosAttendeeDetails.role,
        attendanceStatus: targetAttendanceStatus,
      );
    } else {
      Log.e("Unsupported platform ${Platform.operatingSystem}");
      return TimelineActionResponse(false);
    }

    Future.microtask(() async {
      await _deviceCalendarPlugin.createOrUpdateEvent(event);
      await _calendarSyncer.syncDeviceCalendarsToDb();
      await _watchSyncer.syncPinDatabaseWithWatch();
    });

    List<TimelineAttribute> attributes = [];
    switch (actionId) {
      case calendarActionAccept:
        attributes = [
          TimelineAttribute.subtitle("Accepted"),
          TimelineAttribute.largeIcon(TimelineIcon.resultSent)
        ];
        break;
      case calendarActionMaybe:
        attributes = [
          TimelineAttribute.subtitle("Maybe"),
          TimelineAttribute.largeIcon(TimelineIcon.resultSent)
        ];
        break;
      case calendarActionDecline:
        attributes = [
          TimelineAttribute.subtitle("Declined"),
          TimelineAttribute.largeIcon(TimelineIcon.resultSent)
        ];
        break;
      default:
        Log.e("Unknown action $actionId");
        return TimelineActionResponse(false);
    }


    return TimelineActionResponse(true, attributes: attributes);
  }
}

final calendarActionHandlerProvider = Provider((ref) =>
    CalendarActionHandler(
      ref.read(timelinePinDaoProvider!),
      ref.read(calendarSyncerProvider!),
      ref.read(watchTimelineSyncerProvider!),
      ref.read(calendarListProvider!),
      ref.read(deviceCalendarPluginProvider),
    ));
