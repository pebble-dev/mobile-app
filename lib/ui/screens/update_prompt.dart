import 'dart:async';

import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/domain/firmware/firmware_install_status.dart';
import 'package:cobble/domain/firmwares.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/datasources/firmwares.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_fab.dart';
import 'package:cobble/ui/common/components/cobble_step.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';


class _UpdateIcon extends StatelessWidget {
  final FirmwareInstallStatus progress;
  final bool hasError;
  final PebbleWatchModel model;

  const _UpdateIcon ({Key? key, required this.progress, required this.hasError, required this.model}) : super(key: key);
  @override
  Widget build(BuildContext context) {
    if (progress.success) {
      return PebbleWatchIcon(model, size: 80.0,);
    } else if (hasError) {
      return const CompIcon(RebbleIcons.dead_watch_ghost80, RebbleIcons.dead_watch_ghost80_background, size: 80.0);
    } else {
      return const CompIcon(RebbleIcons.check_for_updates, RebbleIcons.check_for_updates_background, size: 80.0);
    }
  }
}

class UpdatePrompt extends HookWidget implements CobbleScreen {
  final bool popOnSuccess;
  UpdatePrompt({Key? key, required this.popOnSuccess}) : super(key: key);

  final fwUpdateControl = FirmwareUpdateControl();


  @override
  Widget build(BuildContext context) {
    var connectionState = useProvider(connectionStateProvider.state);
    var firmwares = useProvider(firmwaresProvider.future);
    var installStatus = useProvider(firmwareInstallStatusProvider.state);
    double? progress;

    final title = useState("Checking for update...");
    final error = useState<String?>(null);
    final updater = useState<Future<void>?>(null);
    final desc = useState<String?>(null);
    final updateRequiredFor = useState<FirmwareType?>(null);
    final awaitingReconnect = useState(false);


    Future<void> _updaterJob(FirmwareType type, bool isRecovery, String hwRev, Firmwares firmwares) async {
      title.value = (isRecovery ? "Restoring" : "Updating") + " firmware...";
      final firmwareFile = await firmwares.getFirmwareFor(hwRev, type);
      try {
        if ((await fwUpdateControl.checkFirmwareCompatible(StringWrapper(value: firmwareFile.path))).value!) {
          Log.d("Firmware compatible, starting update");
          if (!(await fwUpdateControl.beginFirmwareUpdate(StringWrapper(value: firmwareFile.path))).value!) {
            Log.d("Failed to start update");
            error.value = "Failed to start update";
          }
        } else {
          Log.d("Firmware incompatible");
          error.value = "Firmware incompatible";
        }
      } catch (e) {
        Log.d("Failed to start update: $e");
        error.value = "Failed to start update";
      }
    }

    String? _getHWRev() {
      try {
        return connectionState.currentConnectedWatch?.runningFirmware.hardwarePlatform.getHardwarePlatformName();
      } catch (e) {
        return null;
      }
    }

    useEffect(() {
      firmwares.then((firmwares) async {
        if (error.value != null) return;
        final hwRev = _getHWRev();
        if (hwRev == null) return;

        if (connectionState.currentConnectedWatch != null && connectionState.isConnected == true && updater.value == null && !installStatus.success) {
          final isRecovery = connectionState.currentConnectedWatch!.runningFirmware.isRecovery!;
          final recoveryOutOfDate = await firmwares.doesFirmwareNeedUpdate(hwRev, FirmwareType.recovery, DateTime.fromMillisecondsSinceEpoch(connectionState.currentConnectedWatch!.recoveryFirmware.timestamp!));
          final normalOutOfDate = isRecovery ? null : await firmwares.doesFirmwareNeedUpdate(hwRev, FirmwareType.normal, DateTime.fromMillisecondsSinceEpoch(connectionState.currentConnectedWatch!.runningFirmware.timestamp!));

          if (isRecovery || normalOutOfDate == true) {
            if (isRecovery) {
              updater.value ??= _updaterJob(FirmwareType.normal, isRecovery, hwRev, firmwares);
            } else {
              updateRequiredFor.value = FirmwareType.normal;
            }
          } else if (recoveryOutOfDate || true) {
            updateRequiredFor.value = FirmwareType.recovery;
          } else {
            if (installStatus.success) {
              title.value = "Success!";
              desc.value = "Your watch is now up to date.";
              updater.value = null;
            } else {
              title.value = "Up to date";
              desc.value = "Your watch is already up to date.";
            }
          }
        }
      }).catchError((e) {
        error.value = "Failed to check for updates";
      });
      return null;
    }, [connectionState, firmwares]);

    useEffect(() {
      progress = installStatus.progress;
      if (connectionState.currentConnectedWatch == null || connectionState.isConnected == false) {
        if (installStatus.success) {
          awaitingReconnect.value = true;
          error.value = null;
          title.value = "Reconnecting...";
          desc.value = "Installation was successful, waiting for the watch to reboot.";
        } else {
          error.value = "Watch not connected or lost connection";
          updater.value = null;
        }
      } else {
        if (installStatus.isInstalling) {
          title.value = "Installing...";
        } else if (!installStatus.success) {
          if (error.value == null) {
            final rev = _getHWRev();
            if (rev == null) {
              error.value = "Failed to get hardware revision";
            } else {
              title.value = "Checking for update...";
            }
          }
        } else {
          if (awaitingReconnect.value) {
            WidgetsBinding.instance.scheduleFrameCallback((timeStamp) {
              context.read(firmwareInstallStatusProvider).reset();
              Navigator.of(context).pop();
            });
          }
        }
      }
      return null;
    }, [connectionState, installStatus]);

    if (error.value != null) {
      title.value = "Error";
      desc.value = error.value;
    }

    final CobbleFab? fab;
    if (error.value != null) {
      fab = CobbleFab(
        label: "Retry",
        icon: RebbleIcons.check_for_updates,
        onPressed: () {
          error.value = null;
          updater.value = null;
        },
      );
    } else if (installStatus.success) {
      if (!popOnSuccess) {
        fab = CobbleFab(
          label: "OK",
          icon: RebbleIcons.check_done,
          onPressed: () {
            Navigator.of(context).pop();
          },
        );
      } else {
        fab = null;
      }
    } else if (!installStatus.isInstalling && updateRequiredFor.value != null) {
      fab = CobbleFab(
        label: "Install",
        icon: RebbleIcons.apply_update,
        onPressed: () async {
          final hwRev = _getHWRev();
          if (hwRev != null) {
            updater.value ??= _updaterJob(updateRequiredFor.value!, false, hwRev, await firmwares);
          }
        },
      );
    } else {
      fab = null;
    }

    return WillPopScope(
      child: CobbleScaffold.page(
          title: "Update",
          child: Container(
            padding: const EdgeInsets.all(16.0),
            alignment: Alignment.topCenter,
            child: CobbleStep(
              icon: _UpdateIcon(progress: installStatus, hasError: error.value != null, model: connectionState.currentConnectedWatch?.model ?? PebbleWatchModel.rebble_logo),
              title: title.value,
              iconBackgroundColor: error.value != null ? context.scheme!.destructive : installStatus.success ? context.scheme!.positive : null,
              child: Column(
                children: [
                  if (desc.value != null)
                    Text(desc.value!)
                  else
                    LinearProgressIndicator(value: progress),
                  const SizedBox(height: 16.0),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(RebbleIcons.send_to_watch_unchecked),
                      const SizedBox(width: 8.0),
                      Text(connectionState.currentConnectedWatch?.name ?? "Watch"),
                    ],
                  ),
                ],
              ),
            ),
          ),
        floatingActionButton: fab,
      ),
      onWillPop: () async => error.value != null || installStatus.success
    );
  }
}