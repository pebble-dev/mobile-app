import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/calendar/requests/delete_all_pins_request.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class CalendarBackground implements CalendarCallbacks {
  final ProviderContainer container;

  late CalendarSyncer calendarSyncer;
  late WatchTimelineSyncer watchTimelineSyncer;
  late TimelinePinDao timelinePinDao;

  late ProviderSubscription<WatchConnectionState> connectionSubscription;

  CalendarBackground(this.container);

  void init() async {
    calendarSyncer = container.listen<CalendarSyncer>(calendarSyncerProvider, (previous, value) {}).read();
    watchTimelineSyncer = container.listen<WatchTimelineSyncer>(watchTimelineSyncerProvider, (previous, value) {}).read();
    timelinePinDao = container.listen<TimelinePinDao>(timelinePinDaoProvider, (previous, value) {}).read();

    CalendarCallbacks.setup(this);

    connectionSubscription = container.listen<WatchConnectionState>(
      connectionStateProvider, (previous, value) {},
    );
  }

  Future<bool> onWatchConnected(PebbleDevice watch, bool unfaithful) async {
    if (unfaithful) {
      Log.d("Clearing all pins");
      return watchTimelineSyncer.clearAllPinsFromWatchAndResync();
    } else {
      Log.d('Performing normal calendar sync');
      return syncTimelineToWatch();
    }
  }

  Future<Object>? onMessageFromUi(Object message) {
    if (message is DeleteAllCalendarPinsRequest) {
      return deleteCalendarPinsFromWatch();
    }

    return null;
  }

  Future<bool> deleteCalendarPinsFromWatch() async {
    await timelinePinDao.markAllPinsFromAppForDeletion(calendarWatchappId);
    await syncTimelineToWatch();

    return true;
  }

  @override
  Future<void> doFullCalendarSync() async {
    await calendarSyncer.syncDeviceCalendarsToDb();
    await syncTimelineToWatch();
  }

  Future<bool> syncTimelineToWatch() async {
    if (isConnectedToWatch()!) {
      await watchTimelineSyncer.syncPinDatabaseWithWatch();
    }

    return false;
  }

  bool? isConnectedToWatch() {
    return connectionSubscription.read().isConnected;
  }
}
