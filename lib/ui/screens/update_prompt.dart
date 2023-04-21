import 'dart:async';

import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/firmwares.dart';
import 'package:cobble/infrastructure/datasources/firmwares.dart';
import 'package:cobble/ui/common/components/cobble_step.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class _UpdateStatus {
  final double? progress;
  final String message;

  _UpdateStatus(this.progress, this.message);
}

class UpdatePrompt extends HookWidget implements CobbleScreen {
  UpdatePrompt({Key? key}) : super(key: key);

  String title = "Checking for update...";
  Stream<_UpdateStatus>? updaterStatusStream;

  @override
  Widget build(BuildContext context) {
    var connectionState = useProvider(connectionStateProvider.state);
    var firmwares = useProvider(firmwaresProvider.future);
    double? progress;

    useEffect(() {
      if (connectionState.currentConnectedWatch != null && connectionState.isConnected == true) {
        if (connectionState.currentConnectedWatch?.runningFirmware.isRecovery == true) {
          title = "Restoring firmware...";
          updaterStatusStream ??= () async* {
            final firmwareFile = (await firmwares).getFirmwareFor(connectionState.currentConnectedWatch!.board!, FirmwareType.normal);
            yield _UpdateStatus(0.0, "Restoring firmware...");

          }();
        }
      } else {
        title = "Lost connection to watch";
        //TODO: go to error
      }
    }, [connectionState, firmwares]);

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
                  LinearProgressIndicator(value: progress,),
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
      onWillPop: () async => false,
    );
  }
}