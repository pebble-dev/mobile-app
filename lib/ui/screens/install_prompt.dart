import 'package:cobble/domain/apps/app_compatibility.dart';
import 'package:cobble/domain/apps/app_install_status.dart';
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class InstallPrompt extends HookConsumerWidget implements CobbleScreen {
  final String _appUri;
  final PbwAppInfo _appInfo;

  InstallPrompt(this._appUri, this._appInfo);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final userInitiatedInstall = useState(false);
    final watchUploadHasStarted = useState(false);

    final installStatus = ref.watch(appInstallStatusProvider);
    final appManager = ref.watch(appManagerProvider.notifier);
    final connectionStatus = ref.watch(connectionStateProvider);

    final connectedWatch = connectionStatus.currentConnectedWatch;

    useEffect(() {
      if (watchUploadHasStarted.value && !installStatus.isInstalling) {
        Navigator.of(context).pop();
      } else if (!watchUploadHasStarted.value && installStatus.isInstalling) {
        watchUploadHasStarted.value = true;
      }
    }, [watchUploadHasStarted, installStatus]);

    Widget body;
    if (userInitiatedInstall.value) {
      String statusText;
      if (installStatus.isInstalling) {
        final roundedPercentage =
        (installStatus.progress * 100).round().toInt().toString();
        statusText = "Installing... [" + roundedPercentage + "%]";
      } else {
        statusText = "Installing...";
      }

      body = Text(statusText);
    } else if (_appInfo.isValid != true) {
      body = Column(
        children: [
          Text("Sorry, this is not a valid PBW file"),
          CobbleButton(
              outlined: false,
              label: "Go Back",
              onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      );
    } else if (connectedWatch == null) {
      body = Column(
        children: [
          Text("Watch not connected"),
          CobbleButton(
              outlined: false,
              label: "Go Back",
              onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      );
    } else if (!_isCompatible(_appInfo, connectedWatch)) {
      body = Column(
        children: [
          Text("Watch not compatible"),
          CobbleButton(
              outlined: false,
              label: "Go Back",
              onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      );
    } else {
      body = Column(
        children: [
          Text(
              "Do you want to install ${_appInfo.longName} by ${_appInfo.companyName}?"),
          CobbleButton(
              outlined: false,
              label: "Yes",
              onPressed: () { 
                  appManager.beginAppInstall(_appUri, _appInfo);
                  userInitiatedInstall.value = true;
              },
          ),
          CobbleButton(
              outlined: false,
              label: "No",
              onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      );
    }

    return CobbleScaffold.page(
        title: "App install",
        child: Container(
          padding: EdgeInsets.all(16.0),
          alignment: Alignment.topCenter,
          child: body,
        ));
  }

  bool _isCompatible(PbwAppInfo app, PebbleDevice watch) {
    final watchType = watch.runningFirmware.hardwarePlatform.getWatchType();

    return app.isCompatibleWith(watchType);
  }
}
