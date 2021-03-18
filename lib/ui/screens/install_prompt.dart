import 'package:cobble/domain/app_manager.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class InstallPrompt extends HookWidget implements CobbleScreen {
  final String _appUri;
  final PbwAppInfo _appInfo;

  InstallPrompt(this._appUri, this._appInfo);

  @override
  Widget build(BuildContext context) {
    Widget body;
    if (!_appInfo.isValid) {
      body = Column(
        children: [
          Text("Sorry, this is not a valid APK file"),
          RaisedButton(
              onPressed: () {
                Navigator.of(context).pop();
              },
              child: Text("Go Back")),
        ],
      );
    } else {
      final appManager = useProvider(appManagerProvider);

      body = Column(
        children: [
          Text(
              "Do you want to install ${_appInfo.longName} by ${_appInfo.companyName}?"),
          RaisedButton(
              onPressed: () {
                appManager.beginAppInstall(_appUri, _appInfo);
              },
              child: Text("Yes")),
          RaisedButton(
              onPressed: () {

                Navigator.of(context).pop();
              },
              child: Text("No")),
        ],
      );
    }

    return CobbleScaffold.page(
        title: "App install",
        child: Container(
          padding: EdgeInsets.all(16.0),
          alignment: Alignment.topCenter,
          child: body,
        ));
  }
}
