import 'package:cobble/infrastructure/datasources/web_services.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class RebbleSetupSuccess extends StatefulWidget implements CobbleScreen {
  @override
  State<StatefulWidget> createState() => _RebbleSetupSuccessState();
}

class _RebbleSetupSuccessState extends State<RebbleSetupSuccess> {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      title: "Activate Rebble services",
      child: Column(
        children: <Widget>[
          Text(
            "All set and ready to Rebble!",
            style: Theme.of(context).textTheme.headline3,
          ),
          FutureBuilder<WSAuthUser>(
            future: WSAuthUser.get(),
            builder:
                (BuildContext context, AsyncSnapshot<WSAuthUser> snapshot) {
              if (snapshot.hasData) {
                return Text("Welcome back, ${snapshot.data.name}!");
              } else {
                return Text(" ");
              }
            },
          )
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
          onPressed: () {
            SharedPreferences.getInstance()
                .then((value) => {
                      value.setBool("firstRun", false),
                      value.setBool("bootSetup", true)
                    })
                .then((value) => context.pushAndRemoveAllBelow(HomePage()));
          },
          label: Text("ON TO REBBLE!")),
    );
  }
}
