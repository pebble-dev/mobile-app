import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/timeline/blob_status.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:hooks_riverpod/all.dart';

/// Sync controller that combines both Device -> DB and DB -> watch syncing into aggregate class.
class TimelineSyncController {
  final CalendarSyncer _calendarSyncer;
  final WatchTimelineSyncer _watchTimelineSyncer;

  TimelineSyncController(this._calendarSyncer, this._watchTimelineSyncer);

  /// Do full re-sync of user's calendar to the watch
  ///
  /// Method returns error message or null if everything went fine.
  Future<String> syncCalendarToWatch() async {
    final anyChanges = await _calendarSyncer.syncDeviceCalendarsToDb();
    if (!anyChanges) {
      return null;
    }

    final res = await _watchTimelineSyncer.syncPinDatabaseWithWatch();

    if (res == statusSuccess) {
      return null;
    } else {
      return "Syncing error $res";
    }
  }
}

final timelineSyncControllerProvider =
    Provider.autoDispose<TimelineSyncController>((ref) {
  final calendarSyncer = ref.watch(calendarSyncerProvider);
  final watchTimelineSyncer = ref.watch(watchTimelineSyncerProvider);

  return TimelineSyncController(calendarSyncer, watchTimelineSyncer);
});
