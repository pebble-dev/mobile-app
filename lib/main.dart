import 'dart:async';
import 'dart:io';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/DevOptionsPage.dart';
import 'file:///D:/AndroidStudioProjects/fossil/fossil/lib/setup/FirstRunPage.dart';
import 'package:fossil/TabsPage.dart';
import 'package:fossil/theme.dart';
import 'package:fossil/util/PairedStorage.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';

import 'setup/PairPage.dart';

String getBootUrl = "https://boot.rebble.io/";

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Fossil',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        brightness: Brightness.dark,
        colorScheme: CTheme.colorScheme,
        accentColor: CTheme.colorScheme.primary,
        buttonTheme: ButtonThemeData(
          highlightColor: CTheme.colorScheme.primary
        ),

        // This makes the visual density adapt to the platform that you run
        // the app on. For desktop platforms, the controls will be smaller and
        // closer together (more dense) than on mobile platforms.
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: SplashPage(),
    );
  }
}

class SplashPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  Timer _timer;
  static const protocolC = MethodChannel('io.rebble.fossil/protocol');

  void _openHome() {
    SharedPreferences.getInstance().then((prefs) => {
      if (!prefs.containsKey("firstRun")) {
        Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => FirstRunPage()))
      }else {
        PairedStorage.getDefault().then((value) {
          if (value != null) protocolC.invokeMethod("targetPebbleAddr", value.address);
        }),
        Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => TabsPage()))
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
    _timer = new Timer(const Duration(milliseconds: 3000), () {
      /*SharedPreferences.getInstance().then((prefs) => {
            if (!prefs.containsKey("boot")) {_askToBoot()} else {_openHome()}
          });*/
      _openHome();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Color.fromRGBO(50, 50, 50, 1),
      body: Center(
        // Center is a layout widget. It takes a single child and positions it
        // in the middle of the parent.
        child: Image(
          image: AssetImage('images/rebble.png'),
          width: 128,
        ),
      ),
    );
  }
}

class TestPage extends StatelessWidget {
  static final MethodChannel platform =
      MethodChannel('io.rebble.fossil/protocol');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Fossil"),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          RaisedButton(
            child: Text("Connect"),
            onPressed: () {
              //TODO
            },
          )
        ],
      ),
    );
  }
}
