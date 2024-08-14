import 'dart:convert';

import 'package:cobble/domain/apps/app_lifecycle_manager.dart';
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/apps/requests/app_reorder_request.dart';
import 'package:cobble/domain/apps/requests/force_refresh_request.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/entities/pbw_app_info_extension.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/watch_apps_syncer.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';

class AppsBackground implements BackgroundAppInstallCallbacks {
  final ProviderContainer container;

  late WatchAppsSyncer watchAppsSyncer;
  late AppDao appDao;
  late AppLifecycleManager appLifecycleManager;
  late Future<Preferences> preferences;
  late AppManager appManager;

  late ProviderSubscription<WatchConnectionState> connectionSubscription;

  AppsBackground(this.container);

  void init() async {
    watchAppsSyncer = container.listen<WatchAppsSyncer>(watchAppSyncerProvider, (previous, value) {}).read();
    appDao = container.listen<AppDao>(appDaoProvider, (previous, value) {}).read();
    appLifecycleManager = container.listen<AppLifecycleManager>(appLifecycleManagerProvider, (previous, value) {}).read();
    preferences = container.listen<Future<Preferences>>(preferencesProvider.future, (previous, value) {}).read();
    appManager = container.listen<AppManager>(appManagerProvider.notifier, (previous, value) {}).read();

    BackgroundAppInstallCallbacks.setup(this);

    connectionSubscription = container.listen<WatchConnectionState>(
      connectionStateProvider, (previous, value) {},
    );
  }

  Future<bool> onWatchConnected(PebbleDevice watch, bool unfaithful) async {
    return forceAppSync(unfaithful);
  }

  Future<bool> forceAppSync(bool clear) async {
    if (clear) {
      Log.d('Clearing all apps and re-syncing');
      return watchAppsSyncer.clearAllAppsFromWatchAndResync();
    } else {
      Log.d('Performing normal app sync');
      return watchAppsSyncer.syncAppDatabaseWithWatch();
    }
  }

  Future<Object>? onMessageFromUi(String type, Object message) {
    if (type == (AppReorderRequest).toString()) {
      if (container.read(connectionStateProvider).currentConnectedWatch?.runningFirmware.isRecovery == true) {
        return Future.value(true);
      }
      final req = AppReorderRequest.fromJson(message as Map<String, dynamic>);
      return beginAppOrderChange(req);
    } else if (type == (ForceRefreshRequest).toString()) {
      if (container.read(connectionStateProvider).currentConnectedWatch?.runningFirmware.isRecovery == true) {
        return Future.value(true);
      }
      final req = ForceRefreshRequest.fromJson(message as Map<String, dynamic>);
      return forceAppSync(req.clear);
    }

    return null;
  }

  @override
  Future<void> beginAppInstall(InstallData installData) async {
    final newAppUuid = Uuid.parse(installData.appInfo.uuid!);

    final existingApp = await appDao.getPackage(newAppUuid);
    if (existingApp != null) {
      final deleteWrapper = StringWrapper();
      deleteWrapper.value = installData.appInfo.uuid;

      await deleteApp(deleteWrapper);
    }

    int newAppOrder;
    if (installData.appInfo.watchapp!.watchface!) {
      newAppOrder = -1;
    } else {
      newAppOrder = await appDao.getNumberOfAllInstalledApps();
    }

    final appInfo = installData.appInfo;

    final newApp = App(
        uuid: newAppUuid,
        shortName: appInfo.shortName ?? "??",
        longName: appInfo.longName ?? "??",
        company: appInfo.companyName ?? "??",
        appstoreId: null,
        version: appInfo.versionLabel!,
        isWatchface: appInfo.watchapp!.watchface!,
        isSystem: false,
        supportedHardware: appInfo.targetPlatformsCast(),
        processInfoFlags: existingApp?.processInfoFlags ?? "{}",
        sdkVersions: existingApp?.sdkVersions ?? "{}",
        nextSyncAction: NextSyncAction.Upload,
        url: existingApp?.url,
        appOrder: newAppOrder);

    await appDao.insertOrUpdatePackage(newApp);

    await (await preferences).setAppReorderPending(true);

    final blobDbSyncSuccess = await watchAppsSyncer.syncAppDatabaseWithWatch();
    Log.d("Blob sync success: $blobDbSyncSuccess");

    if (blobDbSyncSuccess) {
      await appLifecycleManager.openApp(newApp.uuid);
    }
  }

  @override
  Future<void> deleteApp(StringWrapper uuidString) async {
    final uuid = Uuid.parse(uuidString.value!);
    await appDao.setSyncAction(uuid, NextSyncAction.Delete);

    if (connectionSubscription.read().isConnected == true) {
      await watchAppsSyncer.syncAppDatabaseWithWatch();
    }
  }

  @override
  Future<bool> beginAppOrderChange(AppReorderRequest arg) async {
    await appDao.move(arg.uuid, arg.newPosition);

    await (await preferences).setAppReorderPending(true);
    await watchAppsSyncer.syncAppDatabaseWithWatch();

    return true;
  }

  @override
  Future<String?> downloadPbw(String uuid) async {
    final app = await appDao.getPackage(Uuid.parse(uuid));
    if (app?.url == null) {
      return null;
    }
    return appManager.downloadPbw(app!.url!, uuid).then((value) => value.toString());
  }
}
