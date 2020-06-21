import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/infrastructure/datasources/PairedStorage.dart';
import 'package:fossil/main.dart';
import 'package:fossil/ui/home/HomePage.dart';
import 'package:fossil/ui/setup/FirstRunPage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';

class SplashPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  static const protocolC = MethodChannel('io.rebble.fossil/protocol');

  void _openHome() {
    SharedPreferences.getInstance().then((prefs) => {
          if (!prefs.containsKey("firstRun"))
            Navigator.pushReplacementNamed(context, '/firstrun')
          else
            {
              PairedStorage.getDefault().then((value) {
                if (value != null)
                  protocolC.invokeMethod("targetPebbleAddr", value.address);
              }),
              Navigator.pushReplacementNamed(context, '/home')
            }
        });
  }

  void _askToBoot() {
    showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text("Log in to Rebble"),
            content: Text(
                "Do you want to configure this app for Rebble Web Services?\n(Selecting no runs the app with offline-only features)"),
            actions: <Widget>[
              FlatButton(
                child: Text("Yes"),
                onPressed: () {
                  canLaunch(getBootUrl).then((value) => {
                        if (value)
                          launch(getBootUrl).then((value) => _openHome())
                        else
                          _openHome()
                      });
                },
              ),
              FlatButton(
                child: Text("No"),
                onPressed: () {
                  _openHome();
                },
              ),
            ],
          );
        });
  }

  @override
  void initState() {
    super.initState();
    _openHome(); // Let's not do a timed splash screen here, it's a waste of
    // the user's time and there are better platform ways to do it
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color.fromRGBO(50, 50, 50, 1),
      body: Center(
        // This page shouldnt be visible for more than a split second, but if
        // it ever is, let the user know it's not broken
        child: CircularProgressIndicator(),
      ),
    );
  }
}
