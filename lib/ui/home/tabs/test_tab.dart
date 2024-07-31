import 'package:cobble/domain/permissions.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/workarounds.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/devoptions/dev_options_page.dart';
import 'package:cobble/ui/devoptions/debug_options_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../../common/icons/fonts/rebble_icons.dart';

class TestTab extends HookConsumerWidget implements CobbleScreen {
  final NotificationsControl notifications = NotificationsControl();

  final ConnectionControl connectionControl = ConnectionControl();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
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

    return CobbleScaffold.tab(
      title: "Testing",
      subtitle: 'Testing subtitle',
      child: SingleChildScrollView(
        child: SingleChildScrollView(
          child: Column(
            children: <Widget>[
              CobbleButton(
                onPressed: () => notifications.sendTestNotification(),
                label: "Test Notification",
              ),
              CobbleButton(
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
                label: "Ping",
              ),
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
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                      SizedBox(height: 8.0),
                      CobbleButton(
                        outlined: false,
                        label: "Developer options",
                        icon: RebbleIcons.developer_connection_console,
                        color: Theme.of(context).colorScheme.primary,
                        onPressed: () => context.push(DevOptionsPage()),
                      ),
                      CobbleButton(
                        outlined: false,
                        label: "App debug options",
                        icon: RebbleIcons.warning,
                        color: Theme.of(context).colorScheme.primary,
                        onPressed: () => context.push(DebugOptionsPage()),
                      ),
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
                      await preferences.value
                          ?.setWorkaroundDisabled(workaround.name, value);
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
