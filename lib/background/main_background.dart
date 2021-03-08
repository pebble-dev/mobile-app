import 'package:cobble/background/notification/notification_manager.dart';
import 'package:cobble/domain/calendar/calendar_pin_convert.dart';
import 'package:cobble/domain/calendar/calendar_syncer.db.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/domain/db/dao/timeline_pin_dao.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/entities/pbw_app_info_extension.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/watch_timeline_syncer.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/util/container_extensions.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:hooks_riverpod/all.dart';
import 'package:uuid_type/uuid_type.dart';

import 'actions/master_action_handler.dart';

void main_background() {
  WidgetsFlutterBinding.ensureInitialized();

  BackgroundReceiver();
}

class BackgroundReceiver implements CalendarCallbacks, TimelineCallbacks, NotificationListening, BackgroundAppInstallCallbacks {
  final container = ProviderContainer();
  late CalendarSyncer calendarSyncer;
  late WatchTimelineSyncer watchTimelineSyncer;
  Future<Preferences>? preferences;
  late TimelinePinDao timelinePinDao;
  late MasterActionHandler masterActionHandler;
  late NotificationManager notificationManager;
  late AppDao appDao;

  late ProviderSubscription<WatchConnectionState> connectionSubscription;

  BackgroundReceiver() {
    init();
  }

  void init() async {
    await BackgroundControl().notifyFlutterBackgroundStarted();

    calendarSyncer = container.listen(calendarSyncerProvider!).read();
    notificationManager = container.listen(notificationManagerProvider).read();
    watchTimelineSyncer = container.listen(watchTimelineSyncerProvider!).read();
    timelinePinDao = container.listen(timelinePinDaoProvider!).read();
    appDao = container.listen(appDaoProvider).read();
    preferences = Future.microtask(() async {
      final asyncValue =
          await container.readUntilFirstSuccessOrError(preferencesProvider);

      return asyncValue.data!.value;
    });
    masterActionHandler = container.read(masterActionHandlerProvider);

    connectionSubscription = container.listen(
      connectionStateProvider!.state,
      mayHaveChanged: (sub) {
        final currentConnectedWatch = sub.read().currentConnectedWatch;
        if (isConnectedToWatch()! && currentConnectedWatch!.name!.isNotEmpty) {
          onWatchConnected(currentConnectedWatch);
        }
      },
    );

    CalendarCallbacks.setup(this);
    TimelineCallbacks.setup(this);
    NotificationListening.setup(this);
    BackgroundAppInstallCallbacks.setup(this);
  }

  @override
  Future<void> doFullCalendarSync() async {
    await calendarSyncer.syncDeviceCalendarsToDb();
    await syncTimelineToWatch();
  }

  void onWatchConnected(PebbleDevice watch) async {
    final lastConnectedWatch =
        (await preferences)!.getLastConnectedWatchAddress();
    if (lastConnectedWatch != watch.address) {
      Log.d("Different watch connected than the last one. Resetting DB...");
      await watchTimelineSyncer.clearAllPinsFromWatchAndResync();
    } else if (watch.isUnfaithful!) {
      Log.d("Connected watch has beein unfaithful (tsk, tsk tsk). Reset DB...");
      await watchTimelineSyncer.clearAllPinsFromWatchAndResync();
    } else {
      await syncTimelineToWatch();
    }

    (await preferences)!.setLastConnectedWatchAddress(watch.address!);
  }

  Future syncTimelineToWatch() async {
    if (isConnectedToWatch()!) {
      await watchTimelineSyncer.syncPinDatabaseWithWatch();
    }
  }

  bool? isConnectedToWatch() {
    return connectionSubscription.read().isConnected;
  }

  @override
  Future<void> deleteCalendarPinsFromWatch() async {
    await timelinePinDao.markAllPinsFromAppForDeletion(calendarWatchappId);
    await syncTimelineToWatch();
  }

  @override
  Future<ActionResponsePigeon> handleTimelineAction(ActionTrigger arg) async {
    return (await masterActionHandler.handleTimelineAction(arg))!.toPigeon();
  }

  @override
  Future<TimelinePinPigeon> handleNotification(NotificationPigeon arg) async {
    return (await notificationManager.handleNotification(arg)).toPigeon();
  }

  @override
  void dismissNotification(StringWrapper arg) {
    notificationManager.dismissNotification(Uuid(arg.value!));
  }

  @override
  void beginAppInstall(InstallData installData) async {
    final allApps = await appDao.getAllInstalledApps();

    final appInfo = installData.appInfo;

    final newApp = App(
        uuid: Uuid.parse(installData.appInfo.uuid),
        shortName: appInfo.shortName,
        longName: appInfo.longName,
        company: appInfo.companyName,
        appstoreId: null,
        version: appInfo.versionLabel,
        isWatchface: appInfo.watchapp.watchface,
        supportedHardware: appInfo.targetPlatformsCast(),
        nextSyncAction: NextSyncAction.Upload,
        appOrder: allApps.length);

    await appDao.insertOrUpdateApp(newApp);
  }
}
