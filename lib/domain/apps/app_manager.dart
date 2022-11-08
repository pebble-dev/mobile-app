import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundRpc.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/util/async_value_extensions.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';

import '../db/models/app.dart';
import 'requests/app_reorder_request.dart';

class AppManager extends StateNotifier<List<App>> {
  final appInstallControl = AppInstallControl();
  final AppDao appDao;
  final BackgroundRpc backgroundRpc;

  AppManager(this.appDao, this.backgroundRpc) : super(List.empty()) {
    refresh();
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
    final wrapper = InstallData(uri: uri, appInfo: appInfo);
    await appInstallControl.beginAppInstall(wrapper);

    await refresh();
  }

  Future<bool> getAppInfoAndBeginAppInstall(String uri) async {
    final appInfoRequestWrapper = StringWrapper();
    appInfoRequestWrapper.value = uri;
    final appInfo = await appInstallControl.getAppInfo(appInfoRequestWrapper);

    final wrapper = InstallData(appInfo: appInfo, uri: uri);

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
  return AppManager(dao, rpc);
});
