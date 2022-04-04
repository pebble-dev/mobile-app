import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/domain/permissions.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/workarounds.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/devoptions/dev_options_page.dart';
import 'package:cobble/ui/devoptions/test_logs_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:share/share.dart';

import '../../common/icons/fonts/rebble_icons.dart';

class TestTab extends HookConsumerWidget implements CobbleScreen {
  final NotificationsControl notifications = NotificationsControl();

  final ConnectionControl connectionControl = ConnectionControl();
  final DebugControl debug = DebugControl();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final connectionState = ref.watch(connectionStateProvider);
    final defaultWatch = ref.watch(defaultWatchProvider);

    final permissionControl = ref.watch(permissionControlProvider);
    final permissionCheck = ref.watch(permissionCheckProvider);

    final preferences = ref.watch(preferencesProvider);
    final neededWorkarounds = ref.watch(neededWorkaroundsProvider).when(
      data: (data) => data,
      loading: () => List<Workaround>.empty(),
      error: (e, s) => List<Workaround>.empty(),
    );

    useEffect(() {
      Future.microtask(() async {
        if (!(await permissionCheck.hasLocationPermission()).value!) {
          await permissionControl.requestLocationPermission();
        }

        if (defaultWatch != null) {
          if (!(await permissionCheck.hasNotificationAccess()).value!) {
            permissionControl.requestNotificationAccess();
          }

          if (!(await permissionCheck.hasBatteryExclusionEnabled()).value!) {
            permissionControl.requestBatteryExclusion();
          }
        }
      });
      return null;
    }, ["one-time"]);

    String statusText;
    if (connectionState.isConnecting == true) {
      statusText = "Connecting to ${connectionState.currentWatchAddress}";
    } else if (connectionState.isConnected == true) {
      PebbleWatchModel model = PebbleWatchModel.rebble_logo;
      String? fwVersion = "unknown";

      if (connectionState.currentConnectedWatch != null) {
        model = connectionState.currentConnectedWatch!.model;
        fwVersion =
            connectionState.currentConnectedWatch!.runningFirmware.version;
      }

      statusText = "Connected to ${connectionState.currentWatchAddress}" +
          " ($model, firmware $fwVersion)";
    } else {
      statusText = "Disconnected";
    }

    return CobbleScaffold.tab(
      title: "Testing",
      subtitle: 'Testing subtitle',
      child: SingleChildScrollView(
        child: SingleChildScrollView(
          child: Column(
            children: <Widget>[
              ElevatedButton(
                onPressed: () {
                  notifications.sendTestNotification();
                },
                child: Text("Test Notification"),
              ),
              ElevatedButton(
                onPressed: () {
                  ListWrapper l = ListWrapper();
                  l.value = [
                    0x00,
                    0x05,
                    0x07,
                    0xD1,
                    0x00,
                    0x00,
                    0x00,
                    0x05,
                    0x39
                  ];
                  connectionControl.sendRawPacket(l);
                },
                child: Text("Ping"),
              ),
              ElevatedButton(
                onPressed: () {
                  debug.collectLogs();
                },
                child: Text("Send logs"),
              ),
              ElevatedButton(
                  onPressed: () async {
                    // Proper UI should display progress bar here
                    // (Downloading color screenshots can take several seconds)
                    // and display proper error message if operation fails

                    final result =
                        await ScreenshotsControl().takeWatchScreenshot();

                    if (result.success) {
                      Share.shareFiles([result.imagePath],
                          mimeTypes: ["image/png"]);
                    }
                  },
                  child: Text("Take a watch screenshot")),
              ElevatedButton(
                  onPressed: () {
                    context.push(TestLogsPage());
                  },
                  child: Text("Logs")),
              Text(statusText),
              Card(
                margin: EdgeInsets.all(16.0),
                child: Padding(
                  padding:
                      EdgeInsets.symmetric(horizontal: 16.0, vertical: 24.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Row(),
                      Text(
                        "Some debug options",
                        style: Theme.of(context).textTheme.headline5,
                      ),
                      SizedBox(height: 8.0),
                      CobbleButton(
                        outlined: false,
                        label: "Open developer options",
                        icon: RebbleIcons.developer_connection_console,
                        color: Theme.of(context).accentColor,
                        onPressed: () => context.push(DevOptionsPage()),
                      ),
                      CobbleButton(
                          outlined: false,
                          label: "Here's another button",
                          icon: RebbleIcons.settings,
                          color: Theme.of(context).accentColor,
                          onPressed: () => {}),
                    ],
                  ),
                ),
              ),
              Text("Disable BLE Workarounds: "),
              ...neededWorkarounds.map(
                (workaround) => Row(children: [
                  Switch(
                    value: workaround.disabled,
                    onChanged: (value) async {
                      await preferences.data?.value
                          .setWorkaroundDisabled(workaround.name, value);
                    },
                  ),
                  Text(workaround.name)
                ]),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void showSyncError(BuildContext context, String errorMsg) {
    showDialog<void>(
        context: context,
        builder: (context) => AlertDialog(
              title: Text("Sync error"),
              content: Text(errorMsg),
              actions: <Widget>[
                TextButton(
                  child: Text('OK'),
                  onPressed: () {
                    Navigator.of(context).pop();
                  },
                ),
              ],
            ));
  }
}
