import 'package:flutter/material.dart';
import 'package:fossil/infrastructure/pigeons/pigeons.dart';
import 'package:fossil/ui/common/icons/fonts/RebbleIconsStroke.dart';

class TestTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _TestTabState();
}

class _TestTabState extends State<TestTab> implements ConnectionCallbacks {
  WatchConnectionState connectionState = new WatchConnectionState();
  final NotificationsControl notifications = NotificationsControl();

  final ConnectionControl connectionControl = ConnectionControl();
  final DebugControl debug = DebugControl();

  @override
  void initState() {
    ConnectionCallbacks.setup(this);
  }

  @override
  Widget build(BuildContext context) {
    String statusText;
    if (connectionState.isConnecting == true) {
      statusText = "Connecting to ${connectionState.currentWatchAddress}";
    } else if (connectionState.isConnected == true) {
      statusText = "Connected to ${connectionState.currentWatchAddress}";
    } else {
      statusText = "Disconnected";
    }

    return Scaffold(
      appBar: AppBar(
        title: Text("Testing"),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: <Widget>[
            RaisedButton(
              onPressed: () {
                notifications.sendTestNotification();
              },
              child: Text("Test Notification"),
            ),
            RaisedButton(
              onPressed: () {
                connectionControl.disconnect();
              },
              child: Text("Disconnect"),
            ),
            RaisedButton(
              onPressed: () {
                debug.collectLogs();
              },
              child: Text("Send logs"),
            ),
            Text(statusText),
            Card(
              margin: EdgeInsets.all(16.0),
              child: Padding(
                padding: EdgeInsets.symmetric(horizontal: 16.0, vertical: 24.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Row(),
                    Text(
                      "Some debug options",
                      style: Theme.of(context).textTheme.headline5,
                    ),
                    SizedBox(height: 8.0),
                    FlatButton.icon(
                        label: Text("Open developer options"),
                        icon: Icon(
                            RebbleIconsStroke.developer_connection_console,
                            size: 25.0),
                        textColor: Theme.of(context).accentColor,
                        onPressed: () =>
                            Navigator.pushNamed(context, '/devoptions')),
                    FlatButton.icon(
                        label: Text("Here's another button"),
                        icon: Icon(RebbleIconsStroke.settings, size: 25.0),
                        textColor: Theme.of(context).accentColor,
                        onPressed: () => {}),
                  ],
                ),
              ),
            )
          ],
        ),
      ),
    );
  }

  @override
  void onWatchConnectionStateChanged(WatchConnectionState newState) {
    setState(() {
      connectionState = newState;
    });
  }
}
