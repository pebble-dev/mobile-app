import 'package:cobble/localization/localization.dart';
import 'package:cobble/main.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/setup/first_run_page.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:url_launcher/url_launcher.dart';

class SplashPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  void _openHome() {
    SharedPreferences.getInstance().then((prefs) {
      if (!prefs.containsKey("firstRun")) {
        context.pushReplacement(FirstRunPage());
      } else {
        context.pushReplacement(HomePage());
      }
    });
  }

  // ignore: unused_element
  void _askToBoot() {
    showDialog(
        context: context,
        builder: (BuildContext context) {
          return AlertDialog(
            title: Text(tr.splashPage.title),
            content: Text(tr.splashPage.body),
            actions: <Widget>[
              CobbleButton(
                outlined: false,
                label: tr.common.yes,
                onPressed: () {
                  canLaunch(getBootUrl).then((value) => {
                        if (value)
                          launch(getBootUrl).then((value) => _openHome())
                        else
                          _openHome()
                      });
                },
              ),
              CobbleButton(
                outlined: false,
                label: tr.common.no,
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
    return CobbleScaffold.page(
      child: Center(
        // This page shouldn't be visible for more than a split second, but if
        // it ever is, let the user know it's not broken
        child: CircularProgressIndicator(),
      ),
    );
  }
}
