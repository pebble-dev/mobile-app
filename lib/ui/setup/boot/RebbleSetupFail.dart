import 'package:flutter/material.dart';
import 'package:fossil/ui/home/homepage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class RebbleSetupFail extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Activate Rebble services"),
      ),
      body: Column(
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
                .then((value) => Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (context) => HomePage()),
                    (Route<dynamic> route) =>
                        false // it has to be this or a back button shows on the main page
                    ));
          },
          label: Text("OKAY")),
    );
  }
}
