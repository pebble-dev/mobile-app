import 'package:cobble/domain/apps/app_logs.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/devoptions/test_logs_page.dart';
import 'package:cobble/ui/devoptions/debug_options_page.dart';
import 'package:cobble/infrastructure/datasources/dev_connection.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:share/share.dart';

enum ActionItem { debugOptions }

class DevOptionsPage extends HookWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    final devConControl = useProvider(devConnectionProvider);
    final devConnState = useProvider(devConnectionProvider.state);

    final connectionState = useProvider(connectionStateProvider.state);
    final ConnectionControl connectionControl = ConnectionControl();
    final pairedStorage = useProvider(pairedStorageProvider);

    void _onDisconnectPressed(bool inSettings) {
      connectionControl.disconnect();
      pairedStorage.clearDefault();
      if (inSettings) Navigator.pop(context);
    }

    return CobbleScaffold.page(
      title: "Developer Options",
      actions: <Widget>[
        PopupMenuButton<ActionItem>(
          // Callback that sets the selected popup menu item.
          onSelected: (ActionItem item) {
              if (item == ActionItem.debugOptions) {
                context.push(DebugOptionsPage());
              }
          },
          itemBuilder: (BuildContext context) => <PopupMenuEntry<ActionItem>>[
            const PopupMenuItem<ActionItem>(
              value: ActionItem.debugOptions,
              child: ListTile(
                leading: Icon(RebbleIcons.warning),
                title: Text('Rebble app debug options'),
              ),
            ),
          ],
        ),
      ],
      child: SingleChildScrollView(
        child: Column(
          children: <Widget>[
            SizedBox(height: 8.0),
            Card(
              margin: EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
              child: Padding(
                padding: EdgeInsets.symmetric(vertical: 16.0),
                child: SwitchListTile(
                  value: devConnState.running,
                  title: Text("Developer Connection"),
                  subtitle: Text("Extremely insecure, resets outside of page" +
                      (devConnState.running
                          ? "\nRunning... ${devConnState.localIp}" +
                              (devConnState.connected ? " **CONNECTED**" : "")
                          : "")),
                  isThreeLine: devConnState.connected,
                  onChanged: (checked) {
                    if (checked) {
                      devConControl.start();
                    } else {
                      devConControl.close();
                    }
                  },
                ),
              ),
            ),
            Card(
              margin: EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
              child: Padding(
                padding: EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Text(
                      "Devices",
                      style: TextStyle(fontSize: 16),
                    ),
                    // TODO: A limited copy of the widget from watches_tab.dart, we need to make this a shared component
                    if (connectionState.currentConnectedWatch != null) ... [
                      Container(
                        child: Row(children: <Widget>[
                          Container(
                            child: Center(
                                child: PebbleWatchIcon(connectionState.currentConnectedWatch!.model)),
                          ),
                          SizedBox(width: 16),
                          Column(
                            children: <Widget>[
                              Text(connectionState.currentConnectedWatch!.name!, style: TextStyle(fontSize: 16)),
                              SizedBox(height: 4),
                              Text("Connected",
                                  style: TextStyle(
                                      color: context.scheme!.muted)),
                              Wrap(
                                spacing: 4,
                                children: [],
                              ),
                            ],
                            crossAxisAlignment: CrossAxisAlignment.start,
                          ),
                        ]),
                        margin: EdgeInsets.symmetric(vertical: 16.0),
                      ),
                      Container(
                        child: Row(children: <Widget>[
                          Expanded(
                            child: SizedBox(
                            height: 40.0,
                            child: CobbleButton(
                                icon: RebbleIcons.disconnect_from_watch,
                                label: "Disconnect",
                                onPressed: () => _onDisconnectPressed(false),
                              ),
                            ),
                          ),
                          SizedBox(width: 16),
                          Expanded(
                          child: SizedBox(
                            height: 40.0,
                            child: CobbleButton(
                                  onPressed: () async {
                                    // Proper UI should display progress bar here
                                    // (Downloading color screenshots can take several seconds)
                                    // and display proper error message if operation fails

                                    final result =
                                        await ScreenshotsControl().takeWatchScreenshot();

                                    if (result.success) {
                                      Share.shareFiles([result.imagePath!],
                                          mimeTypes: ["image/png"]);
                                    }
                                  },
                                  icon: RebbleIcons.screenshot_camera,
                                  label: "Screenshot"
                                ),
                              ),
                            ),
                        ]),
                      ),
                    ],
                    // TODO: Add device selector
                  ],
                ),
              ),
            ),
            // TODO: Recent apps
            Container(
              width: double.infinity,
              child: Card(
                margin: EdgeInsets.symmetric(vertical: 8.0, horizontal: 16.0),
                child: Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        "Logs",
                        style: TextStyle(fontSize: 16),
                      ),
                      // TODO: List the logs separetely instead of one long list
                      // This is why this is just a button instead of a list
                      ElevatedButton(
                        onPressed: () => context.push(TestLogsPage()),
                        child: Text("Logs"),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
