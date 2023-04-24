import 'dart:async';

import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/domain/firmware/firmware_install_status.dart';
import 'package:cobble/domain/firmwares.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/datasources/firmwares.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_step.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class UpdatePrompt extends HookWidget implements CobbleScreen {
  UpdatePrompt({Key? key}) : super(key: key);

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

    useEffect(() {
      if (connectionState.currentConnectedWatch != null && connectionState.isConnected == true) {
        if (connectionState.currentConnectedWatch?.runningFirmware.isRecovery == true) {
          title.value = "Restoring firmware...";
          updater.value ??= () async {
            final String hwRev;
            try {
              hwRev = connectionState.currentConnectedWatch!.runningFirmware.hardwarePlatform.getHardwarePlatformName();
            } catch (e) {
              title.value = "Error";
              error.value = "Unknown hardware platform";
              return;
            }
            final firmwareFile = await (await firmwares).getFirmwareFor(hwRev, FirmwareType.normal);
            if ((await fwUpdateControl.checkFirmwareCompatible(StringWrapper(value: firmwareFile.path))).value!) {
              Log.d("Firmware compatible, starting update");
              if (!(await fwUpdateControl.beginFirmwareUpdate(StringWrapper(value: firmwareFile.path))).value!) {
                Log.d("Failed to start update");
                title.value = "Error";
                error.value = "Failed to start update";
              }
            } else {
              Log.d("Firmware incompatible");
              title.value = "Error";
              error.value = "Firmware incompatible";
            }
          }();
        }
      } else {
        title.value = "Error";
        error.value = "Watch not connected or lost connection";
      }
      return null;
    }, [connectionState, firmwares]);

    useEffect(() {
      progress = installStatus.progress;
      if (installStatus.isInstalling) {
        title.value = "Installing...";
      } else if (installStatus.isInstalling && installStatus.progress == 1.0) {
        title.value = "Done";
      } else if (!installStatus.isInstalling && installStatus.progress != null && installStatus.progress != 1.0) {
        title.value = "Error";
        error.value = "Installation failed";
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
              title: title.value,
              child: Column(
                children: [
                  if (error.value != null)
                    Text(error.value!)
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
      onWillPop: () async => error.value != null,
    );
  }
}