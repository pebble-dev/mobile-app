import 'package:cobble/background/actions/master_action_handler.dart';
import 'package:cobble/domain/calendar/calendar_list.dart';
import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/timeline_action_response.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:cobble/util/state_provider_extension.dart';
import 'package:cobble/util/stream_extensions.dart';
import 'package:hooks_riverpod/all.dart';

class CalendarActionHandler implements ActionHandler {
  final TimelinePinDao _dao;
  final CalendarSyncer _calendarSyncer;
  final WatchTimelineSyncer _watchSyncer;
  final CalendarList _calendarList;

  CalendarActionHandler(
    this._dao,
    this._calendarSyncer,
    this._watchSyncer,
    this._calendarList,
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
  Future<TimelineActionResponse> _handleMuteCalendarAction(
      TimelinePin pin,) async {
    final calendarList =
    await _calendarList.streamWithExistingValue.firstSuccessOrError();

    final calendars = calendarList.data.value;
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
}

final calendarActionHandlerProvider = Provider((ref) => CalendarActionHandler(
  ref.read(timelinePinDaoProvider),
  ref.read(calendarSyncerProvider),
  ref.read(watchTimelineSyncerProvider),
  ref.read(calendarListProvider),
    ));
