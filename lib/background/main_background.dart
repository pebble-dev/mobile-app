import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
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

  ProviderSubscription<WatchConnectionState> connectionSubscription;

  BackgroundReceiver() {
    init();
  }

  void init() async {
    await BackgroundControl().notifyFlutterBackgroundStarted();

    calendarSyncer = container.listen(calendarSyncerProvider).read();
    watchTimelineSyncer = container.listen(watchTimelineSyncerProvider).read();

    connectionSubscription = container.listen(
      connectionStateProvider.state,
      mayHaveChanged: (sub) {
        if (isConnectedToWatch()) {
          onWatchConnected();
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

  void onWatchConnected() async {
    await syncTimelineToWatch();
  }

  Future syncTimelineToWatch() async {
    if (isConnectedToWatch()) {
      final status = await watchTimelineSyncer.syncPinDatabaseWithWatch();
      // TODO properly handle errors
      print('Sync status $status');
    }
  }

  bool isConnectedToWatch() {
    return connectionSubscription.read().isConnected;
  }
}
