import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class RebbleSetupFail extends StatelessWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      title: "Activate Rebble services",
      child: Column(
        children: <Widget>[
          Text(
            "Oops!",
            style: Theme.of(context).textTheme.headline3,
          ),
          Text(
              "An error occured setting up Rebble, we'll load in offline mode and you can try again from settings later!")
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
          onPressed: () {
            SharedPreferences.getInstance()
                .then((value) => {
                      value.setBool("firstRun", false),
                      value.setBool("bootSetup", false)
                    })
                .then(
                  (value) => context.pushAndRemoveAllBelow(HomePage()),
                );
          },
          label: Text("OKAY")),
    );
  }
}
