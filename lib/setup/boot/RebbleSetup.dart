
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/icons/rebble_icons_stroke_icons.dart';
import 'package:url_launcher/url_launcher.dart';

import 'RebbleSetupFail.dart';
import 'RebbleSetupSuccess.dart';

String _getBootUrl = "https://boot.rebble.io/";

class RebbleSetup extends StatelessWidget {
  static final MethodChannel _bootWaiter = MethodChannel("io.rebble.fossil/bootWaiter");
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Activate Rebble services"),
        leading: IconButton(icon: Icon(RebbleIconsStroke.caret_left), onPressed: () => Navigator.maybePop(context),),
      ),
      body: Column(
        children: <Widget>[
          Text("Rebble Web Services provides the app store, timeline integration, timeline weather, and voice dictation"),
          RaisedButton(
            child: Text("SIGN IN TO REBBLE SERVICES"),
            onPressed: () => canLaunch(_getBootUrl).then((value) {
              if (value) {
                launch(_getBootUrl);
                _bootWaiter.invokeMethod("waitForBoot").then((value) {
                  if (value) {
                    Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => RebbleSetupSuccess()));
                  }else {
                    Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => RebbleSetupFail()));
                  }
                });
              }
            }),
          )
        ],
      ),
    );
  }

}