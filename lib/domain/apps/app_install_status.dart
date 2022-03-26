import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class AppInstallStatusStateNotifier extends StateNotifier<AppInstallStatus>
    implements AppInstallStatusCallbacks {
  final appInstallControl = AppInstallControl();

  AppInstallStatusStateNotifier() : super(_getDefault()) {
    AppInstallStatusCallbacks.setup(this);
    appInstallControl.subscribeToAppStatus();
  }

  void close() {
    appInstallControl.unsubscribeFromAppStatus();
  }

  @override
  void onStatusUpdated(AppInstallStatus status) {
    state = status;
  }
}

AppInstallStatus _getDefault() {
  AppInstallStatus status = AppInstallStatus();
  status.isInstalling = false;
  status.progress = 0.0;

  return status;
}

final appInstallStatusProvider =
    AutoDisposeStateNotifierProvider<AppInstallStatusStateNotifier, AppInstallStatus>((ref) {
  final notifier = AppInstallStatusStateNotifier();

  ref.onDispose(() {
    notifier.close();
  });

  return notifier;
});
