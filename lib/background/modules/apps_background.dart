import 'package:cobble/domain/apps/app_lifecycle_manager.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/domain/db/dao/appstore_app_dao.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/db/models/appstore_app.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/entities/pbw_app_info_extension.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/domain/timeline/watch_apps_syncer.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';
import 'package:http/http.dart' as http;

class AppsBackground implements BackgroundAppInstallCallbacks {
  final ProviderContainer container;

  late WatchAppsSyncer watchAppsSyncer;
  late AppDao appDao;
  late AppstoreAppDao appstoreAppDao;
  late AppLifecycleManager appLifecycleManager;
  late Future<Preferences> preferences;

  late ProviderSubscription<WatchConnectionState> connectionSubscription;

  AppsBackground(this.container);

  void init() async {
    watchAppsSyncer = container.listen(watchAppSyncerProvider).read();
    appDao = container.listen(appDaoProvider).read();
    appstoreAppDao = container.listen(appstoreAppDaoProvider).read();
    appLifecycleManager = container.listen(appLifecycleManagerProvider).read();
    preferences = container.listen(preferencesProvider.future).read();

    BackgroundAppInstallCallbacks.setup(this);

    connectionSubscription = container.listen(
      connectionStateProvider.state,
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

  @override
  Future<void> insertAppstoreApp(AppstoreAppInfo data) async {
    final uuid = Uuid.parse(data.uuid);

    List<String> imageUrls = [data.list_image, data.icon_image, data.screenshot_image];
    List<List<int>?> imageLists = imageUrls.map((url) {
      Uri? uri = Uri.tryParse(url);
      // Apparently an empty string is a fine uri
      if (uri != null && url.isEmpty)
        http.get(uri).then((response) { return response.bodyBytes.toList(); });
      return null;
    }).toList();
    
    final newApp = AppstoreApp(
        id: data.id,
        uuid: uuid,
        title: data.title,
        isWatchface: data.type == "watchface",
        listImage: imageLists[0],
        iconImage: imageLists[1],
        screenshotImage: imageLists[2]);

    await appstoreAppDao.insertOrUpdatePackage(newApp);
  }

  @override
  Future<void> beginAppInstall(InstallData installData) async {
    final newAppUuid = Uuid.parse(installData.appInfo.uuid);
    final appstoreId = installData.appstoreId;

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
        appstoreId: appstoreId,
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
    final uuid = Uuid(uuidString.value);
    await appDao.setSyncAction(uuid, NextSyncAction.Delete);

    if (connectionSubscription.read().isConnected == true) {
      await watchAppsSyncer.syncAppDatabaseWithWatch();
    }
  }

  @override
  Future<void> beginAppOrderChange(AppReorderRequest arg) async {
    final uuid = Uuid(arg.uuid);

    await appDao.move(uuid, arg.newPosition);

    await (await preferences).setAppReorderPending(true);
    await watchAppsSyncer.syncAppDatabaseWithWatch();
  }
}
