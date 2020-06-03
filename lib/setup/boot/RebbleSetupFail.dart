
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'file:///D:/AndroidStudioProjects/fossil/fossil/lib/util/WebServices.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../TabsPage.dart';
import '../../icons/rebble_icons_stroke_only_icons.dart';

class RebbleSetupFail extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Activate Rebble services"),
      ),
      body: Column(
        children: <Widget>[
          Text("Oops!", style: Theme.of(context).textTheme.headline3,),
          Text("An error occured setting up Rebble, we'll load in offline mode and you can try again from settings later!")
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
          onPressed: (){
            SharedPreferences.getInstance().then((value) => {
              value.setBool("firstRun", false),
              value.setBool("bootSetup", false)
            }).then((value) => Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => TabsPage())));
          },
          label: Text("OKAY")
      ),
    );
  }

}