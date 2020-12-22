import 'package:cobble/background/actions/master_action_handler.dart';
import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/timeline_action_response.dart';
import 'package:cobble/domain/timeline/timeline_attribute.dart';
import 'package:cobble/domain/timeline/timeline_icon.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:hooks_riverpod/all.dart';

class CalendarActionHandler implements ActionHandler {
  final TimelinePinDao _dao;
  final WatchTimelineSyncer _syncer;

  CalendarActionHandler(this._dao, this._syncer);

  @override
  Future<TimelineActionResponse> handleTimelineAction(
    TimelinePin pin,
    ActionTrigger trigger,
  ) async {
    switch (trigger.actionId) {
      case calendarActionRemove:
        return _handleRemoveCalendarAction(pin);
      default:
        Log.e("Unknown calendar action: ${trigger.actionId}");
        return TimelineActionResponse(false);
    }
  }

  @override
  Future<TimelineActionResponse> _handleRemoveCalendarAction(
    TimelinePin pin,
  ) async {
    Future.microtask(() async {
      await _dao.setSyncAction(pin.itemId, NextSyncAction.Delete);
      await _syncer.syncPinDatabaseWithWatch();
    });

    return TimelineActionResponse(true, attributes: [
      TimelineAttribute.subtitle("Removed"),
      TimelineAttribute.largeIcon(TimelineIcon.resultDismissed)
    ]);
  }
}

final calendarActionHandlerProvider = Provider((ref) => CalendarActionHandler(
      ref.read(timelinePinDaoProvider),
      ref.read(watchTimelineSyncerProvider),
    ));
