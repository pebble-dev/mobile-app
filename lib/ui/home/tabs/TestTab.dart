import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/ui/common/icons/CompIcon.dart';
import 'package:fossil/ui/common/icons/fonts/RebbleIconsFill.dart';
import 'package:fossil/ui/common/icons/fonts/RebbleIconsStroke.dart';
import 'package:fossil/ui/Router.dart';

class TestTab extends StatelessWidget {
  static const notificationTest = MethodChannel('io.rebble.fossil/notificationTest');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Testing"),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: <Widget>[
            RaisedButton(
              onPressed: () {
                notificationTest.invokeMethod('sendTestNotification');
              },
              child: Text("Button"),
            ),
            Text("This is some text."),
            Card(
              margin: EdgeInsets.all(16.0),
              child: Padding(
                padding: EdgeInsets.symmetric(horizontal: 16.0, vertical: 24.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Row(),
                    Text("Some debug options", style: Theme.of(context).textTheme.headline5,),
                    SizedBox(height: 8.0),
                    FlatButton.icon(
                        label: Text("Open developer options"),
                        icon: Icon(RebbleIconsStroke.developer_connection_console, size: 25.0),
                        textColor: Theme.of(context).accentColor,
                        onPressed: () => Navigator.pushNamed(context, '/devoptions')
                    ),
                    FlatButton.icon(
                        label: Text("Here's another button"),
                        icon: Icon(RebbleIconsStroke.settings, size: 25.0),
                        textColor: Theme.of(context).accentColor,
                        onPressed: () => {}
                    ),
                  ],
                ),
              ),
            )
          ],
        ),
      ),
    );
  }
}
