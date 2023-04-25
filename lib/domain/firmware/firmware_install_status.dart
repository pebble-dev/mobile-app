import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:state_notifier/state_notifier.dart';

class FirmwareInstallStatus {
  final bool isInstalling;
  final double? progress;
  final bool success;

  FirmwareInstallStatus({required this.isInstalling, this.progress, this.success = false});

  @override
  String toString() {
    return 'FirmwareInstallStatus{isInstalling: $isInstalling, progress: $progress}';
  }
}

class FirmwareInstallStatusNotifier extends StateNotifier<FirmwareInstallStatus> implements FirmwareUpdateCallbacks {
  FirmwareInstallStatusNotifier() : super(FirmwareInstallStatus(isInstalling: false)) {
    FirmwareUpdateCallbacks.setup(this);
  }

  @override
  void onFirmwareUpdateFinished() {
    state = FirmwareInstallStatus(isInstalling: false, progress: 100.0, success: true);
  }

  @override
  void onFirmwareUpdateProgress(double progress) {
    state = FirmwareInstallStatus(isInstalling: true, progress: progress == 0.0 ? null : progress);
  }

  @override
  void onFirmwareUpdateStarted() {
    state = FirmwareInstallStatus(isInstalling: true);
  }

  void reset() {
    state = FirmwareInstallStatus(isInstalling: false);
  }
}

final firmwareInstallStatusProvider = StateNotifierProvider<FirmwareInstallStatusNotifier>((ref) => FirmwareInstallStatusNotifier());