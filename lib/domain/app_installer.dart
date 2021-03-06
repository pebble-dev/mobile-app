import 'package:cobble/domain/entities/pbw_app_info_extension.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/all.dart';

class AppInstaller {
  final appInstallControl = AppInstallControl();

  void beginAppInstall(String uri, PbwAppInfo appInfo) {
    final wrapper = InstallData();
    wrapper.uri = uri;
    wrapper.appInfo = appInfo;
    appInstallControl.beginAppInstall(wrapper);
  }
}

final appInstallerProvider = Provider((ref) => AppInstaller());