import 'dart:io';
import 'dart:ui';

import 'package:cobble/background/modules/apps_background.dart';
import 'package:cobble/background/modules/notifications_background.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundReceiver.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/localization/model/model_generator.model.dart';
import 'package:cobble/util/container_extensions.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import 'actions/master_action_handler.dart';
import 'modules/calendar_background.dart';

void main_background() {
  DartPluginRegistrant.ensureInitialized();
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
    final locale = resolveLocale(
        WidgetsBinding.instance?.window.locales, supportedLocales);

    await Localization.load(locale);

    await BackgroundControl().notifyFlutterBackgroundStarted();

    masterActionHandler = container.read(masterActionHandlerProvider);

    connectionSubscription = container.listen<WatchConnectionState>(
      connectionStateProvider, (previous, sub) {
        final currentConnectedWatch = sub.currentConnectedWatch;
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

    startReceivingRpcRequests(onMessageFromUi);
  }

  void onWatchConnected(PebbleDevice watch) async {
    var prefs = await preferences;

    await prefs.reload();

    final lastConnectedWatch = prefs.getLastConnectedWatchAddress();

    bool unfaithful = false;
    if (lastConnectedWatch != watch.address) {
      Log.d("Different watch connected than the last one.");
      unfaithful = true;
    } else if (watch.isUnfaithful!) {
      Log.d("Connected watch has beein unfaithful (tsk, tsk tsk)");
      unfaithful = true;
    }

    if (unfaithful) {
      // Ensure we will stay in unfaithful mode until sync succeeds
      await prefs.setLastConnectedWatchAddress("");
    }

    bool success = true;

    success &= await calendarBackground.onWatchConnected(watch, unfaithful);
    success &= await appsBackground.onWatchConnected(watch, unfaithful);

    Log.d('Watch connected');

    if (success) {
      prefs.setLastConnectedWatchAddress(watch.address!);
    }
  }

  Future<Object> onMessageFromUi(Object message) async {
    Object? result;

    result = appsBackground.onMessageFromUi(message);
    if (result != null) {
      return result;
    }

    result = calendarBackground.onMessageFromUi(message);
    if (result != null) {
      return result;
    }

    throw Exception("Unknown message $message");
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
