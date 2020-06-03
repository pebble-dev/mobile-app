import 'package:flutter/material.dart';

import 'boot/RebbleSetup.dart';
import '../icons/rebble_icons_stroke_only_icons.dart';

class MoreSetup extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _MoreSetupState();
}

class _MoreSetupState extends State<MoreSetup> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("More setup"),
        leading: IconButton(icon: Icon(RebbleIconsStrokeOnly.caret_left), onPressed: () => Navigator.maybePop(context),),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => RebbleSetup())),
        label: Row(children: <Widget>[
          Text("LET'S GET STARTED"),
          Icon(RebbleIconsStrokeOnly.caret_right)],
          mainAxisAlignment: MainAxisAlignment.center,
        ),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      body: SingleChildScrollView(
        child: Column(
          children: <Widget>[
            Text("Setup language / health / privacy")
          ],
        ),
      ),
    );
  }

}