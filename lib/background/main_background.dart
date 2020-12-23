import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:cobble/util/container_extensions.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/widgets.dart';
import 'package:hooks_riverpod/all.dart';

import 'actions/master_action_handler.dart';

void main_background() {
  WidgetsFlutterBinding.ensureInitialized();

  BackgroundReceiver();
}

class BackgroundReceiver implements CalendarCallbacks, TimelineCallbacks {
  final container = ProviderContainer();
  CalendarSyncer calendarSyncer;
  WatchTimelineSyncer watchTimelineSyncer;
  Future<Preferences> preferences;
  TimelinePinDao timelinePinDao;
  MasterActionHandler masterActionHandler;

  ProviderSubscription<WatchConnectionState> connectionSubscription;

  BackgroundReceiver() {
    init();
  }

  void init() async {
    await BackgroundControl().notifyFlutterBackgroundStarted();

    calendarSyncer = container.listen(calendarSyncerProvider).read();
    watchTimelineSyncer = container.listen(watchTimelineSyncerProvider).read();
    timelinePinDao = container.listen(timelinePinDaoProvider).read();
    preferences = Future.microtask(() async {
      final asyncValue =
          await container.readUntilFirstSuccessOrError(preferencesProvider);

      return asyncValue.data.value;
    });
    masterActionHandler = container.read(masterActionHandlerProvider);

    connectionSubscription = container.listen(
      connectionStateProvider.state,
      mayHaveChanged: (sub) {
        final currentConnectedWatch = sub.read().currentConnectedWatch;
        if (isConnectedToWatch() && currentConnectedWatch.name.isNotEmpty) {
          onWatchConnected(currentConnectedWatch);
        }
      },
    );

    CalendarCallbacks.setup(this);
    TimelineCallbacks.setup(this);
  }

  @override
  Future<void> doFullCalendarSync() async {
    await calendarSyncer.syncDeviceCalendarsToDb();
    await syncTimelineToWatch();
  }

  void onWatchConnected(PebbleDevice watch) async {
    final lastConnectedWatch =
    (await preferences).getLastConnectedWatchAddress();
    if (lastConnectedWatch != watch.address) {
      Log.d("Different watch connected than the last one. Resetting DB...");
      await watchTimelineSyncer.clearAllPinsFromWatchAndResync();
    } else if (watch.isUnfaithful) {
      Log.d("Connected watch has beein unfaithful (tsk, tsk tsk). Reset DB...");
      await watchTimelineSyncer.clearAllPinsFromWatchAndResync();
    } else {
      await syncTimelineToWatch();
    }

    (await preferences).setLastConnectedWatchAddress(watch.address);
  }

  Future syncTimelineToWatch() async {
    if (isConnectedToWatch()) {
      await watchTimelineSyncer.syncPinDatabaseWithWatch();
    }
  }

  bool isConnectedToWatch() {
    return connectionSubscription
        .read()
        .isConnected;
  }

  @override
  Future<void> deleteCalendarPinsFromWatch() async {
    await timelinePinDao.markAllPinsFromAppForDeletion(calendarWatchappId);
    await syncTimelineToWatch();
  }

  @override
  Future<ActionResponsePigeon> handleTimelineAction(ActionTrigger arg) async {
    return (await masterActionHandler.handleTimelineAction(arg)).toPigeon();
  }
}
