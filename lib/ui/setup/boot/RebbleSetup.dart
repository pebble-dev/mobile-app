import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:cobble/ui/common/icons/fonts/RebbleIconsStroke.dart';
import 'package:url_launcher/url_launcher.dart';

String _getBootUrl = "https://boot.rebble.io/";

class RebbleSetup extends StatelessWidget {
  static final MethodChannel _bootWaiter =
      MethodChannel("io.rebble.cobble/bootWaiter");
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Activate Rebble services"),
        leading: IconButton(
          icon: Icon(RebbleIconsStroke.caret_left),
          onPressed: () => Navigator.maybePop(context),
        ),
      ),
      body: Column(
        children: <Widget>[
          Text(
              "Rebble Web Services provides the app store, timeline integration, timeline weather, and voice dictation"),
          RaisedButton(
            child: Text("SIGN IN TO REBBLE SERVICES"),
            onPressed: () => canLaunch(_getBootUrl).then((value) {
              if (value) {
                launch(_getBootUrl);
                _bootWaiter.invokeMethod("waitForBoot").then((value) {
                  if (value)
                    Navigator.pushReplacementNamed(context, '/setupsuccess');
                  else
                    Navigator.pushReplacementNamed(context, '/setupfail');
                });
              }
            }),
          ),
          FlatButton(
            child: Text("SKIP"),
            onPressed: () =>
                Navigator.pushReplacementNamed(context, '/setupsuccess'),
          )
        ],
      ),
    );
  }
}
