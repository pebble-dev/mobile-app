import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:state_notifier/state_notifier.dart';

class FirmwareInstallStatus {
  final bool isInstalling;
  final double? progress;

  FirmwareInstallStatus({required this.isInstalling, this.progress});
}

class FirmwareInstallStatusNotifier extends StateNotifier<FirmwareInstallStatus> implements FirmwareUpdateCallbacks {
  FirmwareInstallStatusNotifier() : super(FirmwareInstallStatus(isInstalling: false)) {
    FirmwareUpdateCallbacks.setup(this);
  }

  @override
  void onFirmwareUpdateFinished() {
    state = FirmwareInstallStatus(isInstalling: false, progress: 100.0);
  }

  @override
  void onFirmwareUpdateProgress(double progress) {
    state = FirmwareInstallStatus(isInstalling: true, progress: progress);
  }

  @override
  void onFirmwareUpdateStarted() {
    state = FirmwareInstallStatus(isInstalling: true);
  }
}

final firmwareInstallStatusProvider = StateNotifierProvider<FirmwareInstallStatusNotifier>((ref) => FirmwareInstallStatusNotifier());