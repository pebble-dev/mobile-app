import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/setup/boot/rebble_setup_fail.dart';
import 'package:cobble/ui/setup/boot/rebble_setup_success.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';

String _getBootUrl = "https://boot.rebble.io/";

class RebbleSetup extends StatelessWidget implements CobbleScreen {
  static final AppLifecycleControl lifecycleControl = AppLifecycleControl();

  @override
  Widget build(BuildContext context) {
    return CobbleScaffold.page(
      title: "Activate Rebble services",
      child: Column(
        children: <Widget>[
          Text(
              "Rebble Web Services provides the app store, timeline integration, timeline weather, and voice dictation"),
          RaisedButton(
            child: Text("SIGN IN TO REBBLE SERVICES"),
            onPressed: () => canLaunch(_getBootUrl).then((value) {
              if (value) {
                launch(_getBootUrl);
                lifecycleControl.waitForBoot().then((value) {
                  if (value.value!)
                    context.pushReplacement(RebbleSetupSuccess());
                  else
                    context.pushReplacement(RebbleSetupFail());
                });
              }
            }),
          ),
          FlatButton(
            child: Text("SKIP"),
            onPressed: () => context.pushReplacement(RebbleSetupSuccess()),
          )
        ],
      ),
    );
  }
}
