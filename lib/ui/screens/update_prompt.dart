import 'dart:async';
import 'dart:ffi';

import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/firmware/firmware_install_status.dart';
import 'package:cobble/domain/firmwares.dart';
import 'package:cobble/infrastructure/datasources/firmwares.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_step.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class UpdatePrompt extends HookWidget implements CobbleScreen {
  UpdatePrompt({Key? key}) : super(key: key);

  String title = "Checking for update...";
  String? error;
  Future<void>? updater;
  final fwUpdateControl = FirmwareUpdateControl();

  @override
  Widget build(BuildContext context) {
    var connectionState = useProvider(connectionStateProvider.state);
    var firmwares = useProvider(firmwaresProvider.future);
    var installStatus = useProvider(firmwareInstallStatusProvider.state);
    double? progress;

    useEffect(() {
      if (connectionState.currentConnectedWatch != null && connectionState.isConnected == true) {
        if (connectionState.currentConnectedWatch?.runningFirmware.isRecovery == true) {
          title = "Restoring firmware...";
          updater ??= () async {
            final firmwareFile = await (await firmwares).getFirmwareFor(connectionState.currentConnectedWatch!.board!, FirmwareType.normal);
            if ((await fwUpdateControl.checkFirmwareCompatible(StringWrapper(value: firmwareFile.path))).value!) {
              fwUpdateControl.beginFirmwareUpdate(StringWrapper(value: firmwareFile.path));
            } else {
              title = "Error";
              error = "Firmware incompatible";
            }
          }();
        }
      } else {
        title = "Error";
        error = "Watch not connected or lost connection";
      }
      return null;
    }, [connectionState, firmwares]);

    useEffect(() {
      progress = installStatus.progress;
      if (installStatus.isInstalling) {
        title = "Installing...";
      } else if (installStatus.isInstalling && installStatus.progress == 1.0) {
        title = "Done";
      } else if (!installStatus.isInstalling && installStatus.progress != 1.0) {
        title = "Error";
        error = "Installation failed";
      }
      return null;
    }, [installStatus]);

    return WillPopScope(
      child: CobbleScaffold.page(
          title: "Update",
          child: Container(
            padding: const EdgeInsets.all(16.0),
            alignment: Alignment.topCenter,
            child: CobbleStep(
              icon: const CompIcon(RebbleIcons.check_for_updates, RebbleIcons.check_for_updates_background, size: 80.0),
              title: title,
              child: Column(
                children: [
                  if (error != null)
                    Text(error!)
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
          )),
      onWillPop: () async => error != null,
    );
  }
}