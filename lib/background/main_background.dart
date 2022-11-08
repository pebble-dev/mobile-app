import 'dart:io';

import 'package:cobble/background/modules/apps_background.dart';
import 'package:cobble/background/modules/notifications_background.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/notification_channel_dao.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/notification_channel.dart';
import 'package:cobble/domain/db/models/timeline_pin.dart';
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
import 'package:shared_preferences_android/shared_preferences_android.dart';
import 'package:shared_preferences_ios/shared_preferences_ios.dart';

import 'actions/master_action_handler.dart';
import 'modules/calendar_background.dart';

void main_background() {
  // https://github.com/flutter/flutter/issues/98473#issuecomment-1041895729
  if (Platform.isAndroid) SharedPreferencesAndroid.registerWith();
  if (Platform.isIOS) SharedPreferencesIOS.registerWith();
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
  //TODO: check where this goes post merge
  late NotificationManager notificationManager;
  late NotificationChannelDao _notificationChannelDao;

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
    _notificationChannelDao = container.listen(notifChannelDaoProvider).read();

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

  //TODO: check where this goes post-merge
  @override
  Future<TimelinePinPigeon?> handleNotification(NotificationPigeon arg) async {
    TimelinePin notif = await notificationManager.handleNotification(arg);

    return notif.toPigeon();
  }

  @override
  void dismissNotification(StringWrapper arg) {
    notificationManager.dismissNotification(Uuid(arg.value!));
  }

  @override
  Future<BooleanWrapper> shouldNotify(NotifChannelPigeon arg) async {
    NotificationChannel? channel = await _notificationChannelDao.getNotifChannelByIds(arg.channelId, arg.packageId);
    return BooleanWrapper()..value=channel?.shouldNotify ?? true;
  }

  @override
  void updateChannel(NotifChannelPigeon arg) {
    if (arg.delete) {
      _notificationChannelDao.deleteNotifChannelByIds(arg.channelId, arg.packageId);
    }else {
      _notificationChannelDao.getNotifChannelByIds(arg.channelId, arg.packageId).then((existing) {
        final shouldNotify = existing?.shouldNotify ?? true;
        final channel = NotificationChannel(arg.packageId, arg.channelId, shouldNotify, name: arg.channelName, description: arg.channelDesc);
        _notificationChannelDao.insertOrUpdateNotificationChannel(channel);
      });
    }
  }
}
