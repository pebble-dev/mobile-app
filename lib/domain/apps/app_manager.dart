import 'dart:convert';
import 'dart:io';

import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/api/appstore/locker_sync.dart';
import 'package:cobble/domain/api/status_exception.dart';
import 'package:cobble/domain/apps/requests/force_refresh_request.dart';
import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/domain/db/dao/locker_cache_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/entities/pbw_app_info_extension.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundRpc.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/util/async_value_extensions.dart';
import 'package:collection/collection.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:uuid_type/uuid_type.dart';
import 'package:logging/logging.dart';

import '../db/models/app.dart';
import 'requests/app_reorder_request.dart';

class AppManager extends StateNotifier<List<App>> {
  final appInstallControl = AppInstallControl();
  final AppDao appDao;
  final BackgroundRpc backgroundRpc;
  final LockerSync lockerSync;
  final _logger = Logger("AppManager");

  AppManager(this.appDao, this.backgroundRpc, this.lockerSync) : super(List.empty()) {
    lockerSync.addListener(_onLockerUpdate, fireImmediately: false);
    refresh();
  }

  void _onLockerUpdate(List<LockerEntry>? locker) async {
    if (locker == null) {
      return;
    }
    final apps = state;
    //remove sideloaded entries
    locker = locker.where((lockerApp) => apps.indexWhere((localApp) => Uuid.parse(lockerApp.uuid) == localApp.uuid && localApp.appstoreId == null) == -1).toList();

    //TODO: updated apps
    final updatedApps = locker.where((lockerApp) => apps.indexWhere((localApp) => lockerApp.id == localApp.appstoreId && lockerApp.version != localApp.version) != -1);
    final newApps = locker.where((lockerApp) => apps.indexWhere((localApp) => lockerApp.id == localApp.appstoreId) == -1);
    final goneApps = apps.where((localApp) => localApp.appstoreId != null && locker!.indexWhere((lockerApp) => lockerApp.id == localApp.appstoreId) == -1);
    final precacheApps = newApps.length < 20;
    for (var app in newApps) {
      if (app.pbw?.file != null) {
        if (newApps.length < 40) {
          _logger.fine("New app ${app.title}");
        }
        try {
          final uri = precacheApps ? await downloadPbw(app.pbw!.file, app.uuid.toLowerCase()) : null;
          await addOrUpdateLockerAppOffloaded(app, uri, app.pbw!.file);
        } on StatusException catch(e) {
          if (e.statusCode == 404) {
            _logger.warning("Failed to download ${app.title}, skipping", e);
          } else {
            rethrow;
          }
        }
      }
    }

    if (newApps.length >= 40) {
      _logger.fine("New apps: ${newApps.length}");
    }

    for (var app in goneApps) {
      await deleteApp(app.uuid);
    }
    final request = ForceRefreshRequest(false);

    final result = await backgroundRpc.triggerMethod(request);
    result.resultOrThrow();

    await refresh();
  }

  Future<File> makePbwFile(String uuid) async {
    final docsDir = (await getApplicationDocumentsDirectory()).parent; // .parent escapes from 'flutter specific' dir
    final appDir = Directory(docsDir.path + "/files/apps");
    if (!await appDir.exists()) {
      await appDir.create(recursive: true);
    }
    return File("${appDir.path}/${uuid.toLowerCase()}.pbw");
  }

  Future<Uri> downloadPbw(String url, String uuid) async {


    final uri = Uri.parse(url);
    HttpClient httpClient = HttpClient();
    final file = await makePbwFile(uuid);
    final fd = file.openWrite();
    try {
      var request = await httpClient.getUrl(uri);
      var response = await request.close();
      if(response.statusCode == 200) {
        await response.pipe(fd);
      } else {
        throw StatusException(response.statusCode, response.reasonPhrase, uri);
      }
      await fd.flush();
    } finally {
      await fd.close();
    }
    return file.uri;
  }

  Future<void> addOrUpdateLockerAppOffloaded(LockerEntry app, Uri? uri, String? url) async {
    final appInfoRequestWrapper = StringWrapper();
    appInfoRequestWrapper.value = uri.toString();
    final appInfo = uri != null ? await appInstallControl.getAppInfo(appInfoRequestWrapper) : null;

    if (uri != null) { // if uri provided, we have the pbw file already
      final wrapper = InstallData(uri: uri.toString(), appInfo: appInfo!, stayOffloaded: true);
      await appInstallControl.beginAppInstall(wrapper);
    }

    final compat = <String>[];
    final sdkVersions = <String, String>{};

    if (app.compatibility.aplite.supported) {
      compat.add("aplite");
      final ver = app.hardwarePlatforms.firstWhereOrNull((e) => e.name == "aplite")?.sdkVersion;
      sdkVersions["aplite"] = ver ?? "5.86";
    }
    if (app.compatibility.basalt.supported) {
      compat.add("basalt");
      final ver = app.hardwarePlatforms.firstWhereOrNull((e) => e.name == "basalt")?.sdkVersion;
      sdkVersions["basalt"] = ver ?? "5.86";
    }
    if (app.compatibility.chalk.supported) {
      compat.add("chalk");
      final ver = app.hardwarePlatforms.firstWhereOrNull((e) => e.name == "chalk")?.sdkVersion;
      sdkVersions["chalk"] = ver ?? "5.86";
    }
    if (app.compatibility.diorite.supported) {
      compat.add("diorite");
      final ver = app.hardwarePlatforms.firstWhereOrNull((e) => e.name == "diorite")?.sdkVersion;
      sdkVersions["diorite"] = ver ?? "5.86";
    }
    if (app.compatibility.emery.supported) {
      compat.add("emery");
      final ver = app.hardwarePlatforms.firstWhereOrNull((e) => e.name == "emery")?.sdkVersion;
      sdkVersions["emery"] = ver ?? "5.86";
    }

    final processInfo = <String, int>{};
    for (var e in app.hardwarePlatforms) {
      processInfo[e.name] = e.pebbleProcessInfoFlags;
    }

    final newApp = App(
        uuid: Uuid.parse(app.uuid),
        shortName: appInfo?.shortName ?? app.title,
        longName: appInfo?.longName ?? app.title,
        company: appInfo?.companyName ?? app.developer.name,
        appstoreId: app.id.toString(),
        version: app.version ?? "??",
        isWatchface: app.type == "watchface",
        isSystem: false,
        supportedHardware: compat,
        processInfoFlags: jsonEncode(processInfo),
        sdkVersions: jsonEncode(sdkVersions),
        nextSyncAction: NextSyncAction.Upload,
        url: url,
        appOrder: app.type == "watchface" ? -1 : await appDao.getNumberOfAllInstalledApps());

    await appDao.insertOrUpdatePackage(newApp);
  }

  Future<void> refresh() async {
    state = await appDao.getAllInstalledPackages();
  }

  Future<void> deleteApp(Uuid uuid) async {
    if ((await appDao.getPackage(uuid))?.appstoreId != null) {
      await lockerSync.removeFromLocker(uuid);
      _logger.fine("Removed from locker");
    }
    final file = await makePbwFile(uuid.toString());
    final uuidWrapper = StringWrapper();
    uuidWrapper.value = uuid.toString();

    await appInstallControl.beginAppDeletion(uuidWrapper);
    await refresh();
    try {
      await file.delete();
    } on FileSystemException catch(_){}
  }

  void beginAppInstall(String uri, PbwAppInfo appInfo) async {
    final wrapper = InstallData(uri: uri, appInfo: appInfo, stayOffloaded: false);
    await appInstallControl.beginAppInstall(wrapper);

    await refresh();
  }

  Future<bool> getAppInfoAndBeginAppInstall(String uri) async {
    final appInfoRequestWrapper = StringWrapper();
    appInfoRequestWrapper.value = uri;
    final appInfo = await appInstallControl.getAppInfo(appInfoRequestWrapper);

    final wrapper = InstallData(appInfo: appInfo, uri: uri, stayOffloaded: false);

    final success = await appInstallControl.beginAppInstall(wrapper);

    await refresh();

    return success.value ?? false;
  }

  Future<void> reorderApp(Uuid uuid, int newPosition) async {
    final request = AppReorderRequest(uuid, newPosition);

    final result = await backgroundRpc.triggerMethod(request);
    result.resultOrThrow();

    await refresh();
  }

  Future<void> resetLocker() async {
    await appDao.deleteAll();
    await appInstallControl.removeAllApps();
    await refresh();
  }
}

final appManagerProvider = AutoDisposeStateNotifierProvider<AppManager, List<App>>((ref) {
  final dao = ref.watch(appDaoProvider);
  final rpc = ref.read(backgroundRpcProvider);
  final lockerSync = ref.watch(lockerSyncProvider.notifier);
  return AppManager(dao, rpc, lockerSync);
});
