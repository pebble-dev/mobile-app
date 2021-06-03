import 'package:cobble/domain/apps/app_lifecycle_manager.dart';
import 'package:cobble/domain/apps/requests/app_reorder_request.dart';
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

  late ProviderSubscription<WatchConnectionState> connectionSubscription;

  AppsBackground(this.container);

  void init() async {
    watchAppsSyncer = container.listen(watchAppSyncerProvider).read();
    appDao = container.listen(appDaoProvider).read();
    appLifecycleManager = container.listen(appLifecycleManagerProvider).read();
    preferences = container.listen(preferencesProvider.future).read();

    BackgroundAppInstallCallbacks.setup(this);

    connectionSubscription = container.listen(
      connectionStateProvider,
    );
  }

  Future<bool> onWatchConnected(PebbleDevice watch, bool unfaithful) async {
    if (unfaithful) {
      Log.d('Clearing all apps and re-syncing');
      return watchAppsSyncer.clearAllAppsFromWatchAndResync();
    } else {
      Log.d('Performing normal app sync');
      return watchAppsSyncer.syncAppDatabaseWithWatch();
    }
  }

  Future<Object>? onMessageFromUi(Object message) {
    if (message is AppReorderRequest) {
      return beginAppOrderChange(message);
    }

    return null;
  }

  @override
  Future<void> beginAppInstall(InstallData installData) async {
    final newAppUuid = Uuid.parse(installData.appInfo.uuid);

    final existingApp = await appDao.getPackage(newAppUuid);
    if (existingApp != null) {
      final deleteWrapper = StringWrapper();
      deleteWrapper.value = installData.appInfo.uuid;

      await deleteApp(deleteWrapper);
    }

    int newAppOrder;
    if (installData.appInfo.watchapp.watchface) {
      newAppOrder = -1;
    } else {
      newAppOrder = await appDao.getNumberOfAllInstalledApps();
    }

    final appInfo = installData.appInfo;

    final newApp = App(
        uuid: newAppUuid,
        shortName: appInfo.shortName,
        longName: appInfo.longName,
        company: appInfo.companyName,
        appstoreId: null,
        version: appInfo.versionLabel,
        isWatchface: appInfo.watchapp.watchface,
        isSystem: false,
        supportedHardware: appInfo.targetPlatformsCast(),
        nextSyncAction: NextSyncAction.Upload,
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
    final uuid = Uuid.parse(uuidString.value);
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
}
