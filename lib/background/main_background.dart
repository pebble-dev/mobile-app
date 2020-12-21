import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:flutter/widgets.dart';
import 'package:hooks_riverpod/all.dart';

void main_background() {
  WidgetsFlutterBinding.ensureInitialized();

  BackgroundReceiver();
}

class BackgroundReceiver implements CalendarCallbacks {
  final container = ProviderContainer();
  CalendarSyncer calendarSyncer;
  WatchTimelineSyncer watchTimelineSyncer;
  Preferences preferences;

  ProviderSubscription<WatchConnectionState> connectionSubscription;

  BackgroundReceiver() {
    init();
  }

  void init() async {
    await BackgroundControl().notifyFlutterBackgroundStarted();

    calendarSyncer = container.listen(calendarSyncerProvider).read();
    watchTimelineSyncer = container.listen(watchTimelineSyncerProvider).read();
    preferences = container.listen(preferencesProvider).read();

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
  }

  @override
  Future<void> doFullCalendarSync() async {
    await calendarSyncer.syncDeviceCalendarsToDb();
    await syncTimelineToWatch();
  }

  void onWatchConnected(PebbleDevice watch) async {
    final lastConnectedWatch = await preferences.getLastConnectedWatchAddress();
    if (lastConnectedWatch != watch.address) {
      Log.d("Different watch connected than the last one. Resetting DB...");
      await watchTimelineSyncer.clearAllPinsFromWatchAndResync();
    } else if (watch.isUnfaithful) {
      Log.d("Connected watch has beein unfaithful (tsk, tsk tsk). Reset DB...");
      await watchTimelineSyncer.clearAllPinsFromWatchAndResync();
    } else {
      await syncTimelineToWatch();
    }

    await preferences.setLastConnectedWatchAddress(watch.address);
  }

  Future syncTimelineToWatch() async {
    if (isConnectedToWatch()) {
      await watchTimelineSyncer.syncPinDatabaseWithWatch();
    }
  }

  bool isConnectedToWatch() {
    return connectionSubscription.read().isConnected;
  }
}
