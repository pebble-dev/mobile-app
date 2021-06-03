import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';

import '../db/models/app.dart';

class AppManager extends StateNotifier<List<App>> {
  final appInstallControl = AppInstallControl();
  final AppDao appDao;

  AppManager(this.appDao) : super(List.empty()) {
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
    final wrapper = InstallData();
    wrapper.uri = uri;
    wrapper.appInfo = appInfo;
    await appInstallControl.beginAppInstall(wrapper);

    await refresh();
  }

  Future<void> reorderApp(Uuid uuid, int newPosition) async {
    final request = AppReorderRequest();
    request.uuid = uuid.toString();
    request.newPosition = newPosition;

    await appInstallControl.beginAppOrderChange(request);
    await refresh();
  }
}

final appManagerProvider = AutoDisposeStateNotifierProvider<AppManager, List<App>>((ref) {
  final dao = ref.watch(appDaoProvider);
  return AppManager(dao);
});
