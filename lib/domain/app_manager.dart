import 'package:cobble/domain/db/dao/app_dao.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';

import 'db/models/app.dart';

class AppManager extends StateNotifier<List<App>> {
  final appInstallControl = AppInstallControl();
  final AppDao appDao;

  AppManager(this.appDao) : super(List.empty()) {
    refresh();
  }

  Future<void> refresh() async {
    state = await appDao.getAllInstalledApps();
  }

  Future<void> deleteApp(Uuid uuid) async {
    final uuidWrapper = StringWrapper();
    uuidWrapper.value = uuid.toString();

    await appInstallControl.beginAppDeletion(uuidWrapper);
    await refresh();
  }

  void beginAppInstall(String uri, PbwAppInfo appInfo) {
    final wrapper = InstallData();
    wrapper.uri = uri;
    wrapper.appInfo = appInfo;
    appInstallControl.beginAppInstall(wrapper);
  }
}

final appManagerProvider = AutoDisposeStateNotifierProvider<AppManager>((ref) {
  final dao = ref.watch(appDaoProvider);
  return AppManager(dao);
});
