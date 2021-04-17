import 'package:cobble/background/modules/apps_background.dart';
import 'package:cobble/background/modules/notifications_background.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/util/container_extensions.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import 'actions/master_action_handler.dart';
import 'modules/calendar_background.dart';

void main_background() {
  WidgetsFlutterBinding.ensureInitialized();

  BackgroundReceiver();
}

class BackgroundReceiver implements TimelineCallbacks {
  final container = ProviderContainer();

  late CalendarBackground calendarBackground;
  late NotificationsBackground notificationsBackground;
  late AppsBackground appsBackground;

  late Future<Preferences> preferences;
  late MasterActionHandler masterActionHandler;

  late ProviderSubscription<WatchConnectionState> connectionSubscription;

  BackgroundReceiver() {
    init();
  }

  void init() async {
    await BackgroundControl().notifyFlutterBackgroundStarted();

    masterActionHandler = container.read(masterActionHandlerProvider);

    connectionSubscription = container.listen(
      connectionStateProvider.state,
      mayHaveChanged: (sub) {
        final currentConnectedWatch = sub.read().currentConnectedWatch;
        if (isConnectedToWatch()! && currentConnectedWatch!.name!.isNotEmpty) {
          onWatchConnected(currentConnectedWatch);
        }
      },
    );

    preferences = Future.microtask(() async {
      final asyncValue =
          await container.readUntilFirstSuccessOrError(preferencesProvider);

      return asyncValue.data!.value;
    });

    TimelineCallbacks.setup(this);

    calendarBackground = CalendarBackground(this.container);
    calendarBackground.init();
    notificationsBackground = NotificationsBackground(this.container);
    notificationsBackground.init();
    appsBackground = AppsBackground(this.container);
    appsBackground.init();
  }

  void onWatchConnected(PebbleDevice watch) async {
    final lastConnectedWatch =
    (await preferences).getLastConnectedWatchAddress();

    bool unfaithful = false;
    if (lastConnectedWatch != watch.address) {
      Log.d("Different watch connected than the last one.");
      unfaithful = true;
    } else if (watch.isUnfaithful!) {
      Log.d("Connected watch has beein unfaithful (tsk, tsk tsk)");
      unfaithful = true;
    }

    await calendarBackground.onWatchConnected(watch, unfaithful);
    await appsBackground.onWatchConnected(watch, unfaithful);

    Log.d('Watch connected');

    (await preferences).setLastConnectedWatchAddress(watch.address!);
  }

  Future syncTimelineToWatch() async {
    await calendarBackground.syncTimelineToWatch();
  }

  bool? isConnectedToWatch() {
    return connectionSubscription.read().isConnected;
  }

  @override
  Future<ActionResponsePigeon> handleTimelineAction(ActionTrigger arg) async {
    return (await masterActionHandler.handleTimelineAction(arg))!.toPigeon();
  }
}
