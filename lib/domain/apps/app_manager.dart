import 'dart:io';

import 'package:cobble/domain/api/appstore/locker_entry.dart';
import 'package:cobble/domain/api/appstore/locker_sync.dart';
import 'package:cobble/domain/api/status_exception.dart';
import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/domain/db/models/next_sync_action.dart';
import 'package:cobble/domain/entities/pbw_app_info_extension.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundRpc.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/util/async_value_extensions.dart';
import 'package:flutter/foundation.dart';
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

    for (var app in newApps) {
      if (app.pbw?.file != null) {
        _logger.fine("New app ${app.title}");
        try {
          final uri = await downloadPbw(app.pbw!.file, app.uuid);
          await addOrUpdateLockerAppOffloaded(app, uri);
          await File.fromUri(uri).delete();
        } on StatusException catch(e) {
          if (e.statusCode == 404) {
            _logger.warning("Failed to download ${app.title}, skipping", e);
          } else {
            rethrow;
          }
        }
      }
    }

    for (var app in goneApps) {
      await deleteApp(app.uuid);
    }
    await refresh();
  }

  Future<Uri> downloadPbw(String url, String uuid) async {
    final tempDir = await getTemporaryDirectory();
    final uri = Uri.parse(url);
    HttpClient httpClient = HttpClient();
    final file = File("${tempDir.path}/$uuid.pbw");

    var request = await httpClient.getUrl(uri);
    var response = await request.close();
    if(response.statusCode == 200) {
      var bytes = await consolidateHttpClientResponseBytes(response);
      await file.writeAsBytes(bytes);
    } else {
      throw StatusException(response.statusCode, response.reasonPhrase, uri);
    }
    return file.uri;
  }

  Future<void> addOrUpdateLockerAppOffloaded(LockerEntry app, Uri uri) async {
    final appInfoRequestWrapper = StringWrapper();
    appInfoRequestWrapper.value = uri.toString();
    final appInfo = await appInstallControl.getAppInfo(appInfoRequestWrapper);

    final wrapper = InstallData(uri: uri.toString(), appInfo: appInfo, stayOffloaded: true);
    await appInstallControl.beginAppInstall(wrapper);

    final newApp = App(
        uuid: Uuid.tryParse(appInfo.uuid ?? "") ?? Uuid.parse(app.uuid),
        shortName: appInfo.shortName ?? "??",
        longName: appInfo.longName ?? "??",
        company: appInfo.companyName ?? "??",
        appstoreId: app.id.toString(),
        version: app.version!,
        isWatchface: appInfo.watchapp!.watchface!,
        isSystem: false,
        supportedHardware: appInfo.targetPlatformsCast(),
        nextSyncAction: NextSyncAction.Upload,
        appOrder: appInfo.watchapp!.watchface! ? -1 : await appDao.getNumberOfAllInstalledApps());

    await appDao.insertOrUpdatePackage(newApp);
  }

  Future<void> refresh() async {
    state = await appDao.getAllInstalledPackages();
  }

  Future<void> deleteApp(Uuid uuid) async {
    final uuidWrapper = StringWrapper();
    uuidWrapper.value = uuid.toString();

    await appInstallControl.beginAppDeletion(uuidWrapper);
    await refresh();
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
}

final appManagerProvider = AutoDisposeStateNotifierProvider<AppManager>((ref) {
  final dao = ref.watch(appDaoProvider);
  final rpc = ref.read(backgroundRpcProvider);
  final lockerSync = ref.watch(lockerSyncProvider);
  return AppManager(dao, rpc, lockerSync);
});
