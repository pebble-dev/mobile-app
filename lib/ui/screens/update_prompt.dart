import 'dart:async';

import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/firmware/firmware_install_status.dart';
import 'package:cobble/domain/firmwares.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/datasources/firmwares.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
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
  final UpdatePromptState state;
  final PebbleWatchModel model;

  const _UpdateIcon ({Key? key, required this.state, required this.model}) : super(key: key);
  @override
  Widget build(BuildContext context) {
    switch (state) {
      case UpdatePromptState.success:
        return PebbleWatchIcon(model, size: 80.0, backgroundColor: Colors.transparent,);
      case UpdatePromptState.error:
        return const CompIcon(RebbleIcons.dead_watch_ghost80, RebbleIcons.dead_watch_ghost80_background, size: 80.0);
      default:
        return const CompIcon(RebbleIcons.check_for_updates, RebbleIcons.check_for_updates_background, size: 80.0);
    }
  }
}

enum UpdatePromptState {
  checking,
  updateAvailable,
  restoreRequired,
  updating,
  reconnecting,
  success,
  error,
  noUpdate,
}

class _RequiredUpdate {
  final FirmwareType type;
  final String hwRev;
  final bool skippable;
  _RequiredUpdate(this.type, this.skippable, this.hwRev);
}

Future<_RequiredUpdate?> _getRequiredUpdate(PebbleDevice device, Firmwares firmwares, String hwRev) async {
  final isRecovery = device.runningFirmware.isRecovery!;
  final recoveryTimestamp = DateTime.fromMillisecondsSinceEpoch(device.recoveryFirmware.timestamp!);
  final normalTimestamp = DateTime.fromMillisecondsSinceEpoch(device.runningFirmware.timestamp!);
  final recoveryOutOfDate = await firmwares.doesFirmwareNeedUpdate(hwRev, FirmwareType.recovery, recoveryTimestamp);
  final normalOutOfDate = isRecovery ? null : await firmwares.doesFirmwareNeedUpdate(hwRev, FirmwareType.normal, normalTimestamp);

  if (isRecovery || normalOutOfDate == true) {
    return _RequiredUpdate(FirmwareType.normal, !isRecovery, hwRev);
  } else if (recoveryOutOfDate == true) {
    return _RequiredUpdate(FirmwareType.recovery, true, hwRev);
  } else {
    return null;
  }
}

class UpdatePrompt extends HookConsumerWidget implements CobbleScreen {
  final Function onSuccess;
  final bool confirmOnSuccess;
  UpdatePrompt({Key? key, required this.onSuccess, required this.confirmOnSuccess}) : super(key: key);

  final fwUpdateControl = FirmwareUpdateControl();

  Future<void> _doUpdate(_RequiredUpdate update, Firmwares firmwares) async {
    final firmwareFile = await firmwares.getFirmwareFor(update.hwRev, update.type);
    if ((await fwUpdateControl.checkFirmwareCompatible(StringWrapper(value: firmwareFile.path))).value!) {
      if (!(await fwUpdateControl.beginFirmwareUpdate(StringWrapper(value: firmwareFile.path))).value!) {
        throw Exception("Failed to start firmware update");
      }
    } else {
      throw Exception("Firmware is not compatible with this watch");
    }
  }

  String _titleForState(UpdatePromptState state) {
    switch (state) {
      case UpdatePromptState.checking:
        return "Checking for updates...";
      case UpdatePromptState.updateAvailable:
        return "Update available!";
      case UpdatePromptState.restoreRequired:
        return "Update required";
      case UpdatePromptState.updating:
        return "Updating...";
      case UpdatePromptState.reconnecting:
        return "Reconnecting...";
      case UpdatePromptState.success:
        return "Success!";
      case UpdatePromptState.error:
        return "Failed to update";
      case UpdatePromptState.noUpdate:
        return "Up to date";
    }
  }

  String? _descForState(UpdatePromptState state) {
    switch (state) {
      case UpdatePromptState.checking:
      case UpdatePromptState.updating:
        return null;
      case UpdatePromptState.updateAvailable:
        return "An update is available for your watch.";
      case UpdatePromptState.restoreRequired:
        return "Your watch firmware needs restoring.";
      case UpdatePromptState.reconnecting:
        return "Installation was successful, waiting for the watch to reboot.";
      case UpdatePromptState.success:
        return "Your watch is now up to date.";
      case UpdatePromptState.error:
        return "Failed to update.";
      case UpdatePromptState.noUpdate:
        return "Your watch is already up to date.";
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    var connectionState = ref.watch(connectionStateProvider);
    var firmwares = ref.watch(firmwaresProvider.future);
    var installStatus = ref.watch(firmwareInstallStatusProvider);
    final error = useState<String?>(null);
    final updater = useState<Future<void>?>(null);
    final state = useState<UpdatePromptState>(UpdatePromptState.checking);
    final showUpdateAnyway = state.value == UpdatePromptState.noUpdate;

    void tryDoUpdate([bool force = false]) {
      if (updater.value != null) {
        Log.w("Update already in progress");
        return;
      }
      updater.value = () async {
        try {
          final hwRev = connectionState.currentConnectedWatch?.runningFirmware.hardwarePlatform.getHardwarePlatformName();
          if (hwRev == null) {
            throw Exception("Failed to get hardware revision");
          }
          var update = await _getRequiredUpdate(connectionState.currentConnectedWatch!, await firmwares, hwRev);
          if (update == null) {
            if (force) {
              update = _RequiredUpdate(FirmwareType.normal, false, hwRev);
            } else {
              state.value = UpdatePromptState.noUpdate;
              return;
            }
          }
          state.value = UpdatePromptState.updating;
          await _doUpdate(update, await firmwares);
        } catch (e) {
          Log.e("Failed to check for updates: $e");
          state.value = UpdatePromptState.error;
          error.value = e.toString();
        }
      }().then((_) {
        updater.value = null;
      });
    }

    Future<void> checkUpdate() async {
      if (state.value == UpdatePromptState.updating || state.value == UpdatePromptState.reconnecting) {
        return;
      }
      state.value = UpdatePromptState.checking;
      error.value = null;
      try {
        final hwRev = connectionState.currentConnectedWatch?.runningFirmware.hardwarePlatform.getHardwarePlatformName();
        if (hwRev == null) {
          throw Exception("Failed to get hardware revision");
        }
        final update = await _getRequiredUpdate(connectionState.currentConnectedWatch!, await firmwares, hwRev);
        if (update == null) {
          state.value = UpdatePromptState.noUpdate;
          return;
        } else {
          if (update.skippable) {
            state.value = UpdatePromptState.updateAvailable;
          } else {
            state.value = UpdatePromptState.restoreRequired;
          }
        }
      } catch (e) {
        Log.e("Failed to check for updates: $e");
        state.value = UpdatePromptState.error;
        error.value = e.toString();
      }
    }

    useEffect(() {
      switch (state.value) {
        case UpdatePromptState.reconnecting:
          if (connectionState.isConnected == true) {
            state.value = UpdatePromptState.success;
          }
          break;
        case UpdatePromptState.updating:
          if (installStatus.success && connectionState.isConnected != true) {
            state.value = UpdatePromptState.reconnecting;
          }
          break;
        default:
          break;
      }
      return null;
    }, [connectionState, installStatus]);

    useEffect(() {
      if (state.value == UpdatePromptState.checking) {
        checkUpdate();
      }
      return null;
    }, []);

    useEffect(() {
      if (!confirmOnSuccess && (state.value == UpdatePromptState.success || state.value == UpdatePromptState.noUpdate)) {
        // Automatically continue if no confirmation is required by queuing for next frame
        WidgetsBinding.instance!.addPostFrameCallback((_) {
          onSuccess(context);
        });
      }
    }, [state.value]);

    final desc = _descForState(state.value);
    final fab = state.value == UpdatePromptState.updateAvailable || state.value == UpdatePromptState.restoreRequired ? CobbleFab(
      icon: RebbleIcons.apply_update,
      onPressed: () {
        tryDoUpdate();
      }, label: 'Update',
    ) : (state.value == UpdatePromptState.success || state.value == UpdatePromptState.noUpdate) && confirmOnSuccess ? CobbleFab(
      icon: RebbleIcons.check_done,
      onPressed: () {
        onSuccess(context);
      }, label: 'Ok',
    ) : null;

    return WillPopScope(
      child: CobbleScaffold.page(
          title: "Update",
          child: Container(
            padding: const EdgeInsets.all(16.0),
            alignment: Alignment.topCenter,
            child: CobbleStep(
              icon: _UpdateIcon(state: state.value, model: connectionState.currentConnectedWatch?.model ?? PebbleWatchModel.rebble_logo),
              iconPadding: installStatus.success ? null : const EdgeInsets.all(20),
              title: _titleForState(state.value),
              iconBackgroundColor: state.value == UpdatePromptState.error ? context.scheme!.destructive : state.value == UpdatePromptState.success ? context.scheme!.positive : null,
              child: Column(
                children: [
                  if (desc != null || error.value != null)
                    Text(error.value ?? desc ?? "")
                  else
                    LinearProgressIndicator(value: installStatus.progress),
                  const SizedBox(height: 16.0),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(RebbleIcons.send_to_watch_unchecked),
                      const SizedBox(width: 8.0),
                      Text(connectionState.currentConnectedWatch?.name ?? "Watch"),
                    ],
                  ),
                  if (showUpdateAnyway)
                    ...[const SizedBox(height: 16.0),
                    CobbleButton(
                      label: "Update Anyway",
                      icon: RebbleIcons.dead_watch_ghost80,
                      onPressed: () {
                        state.value = UpdatePromptState.updateAvailable;
                        tryDoUpdate(true);
                      },
                    )]
                ],
              ),
            ),
          ),
        floatingActionButton: fab,
      ),
      onWillPop: () async => !installStatus.isInstalling && state.value != UpdatePromptState.updating,
    );
  }
}