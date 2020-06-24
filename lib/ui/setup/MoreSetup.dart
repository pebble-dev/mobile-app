import 'package:flutter/material.dart';
import 'package:fossil/ui/common/icons/fonts/RebbleIconsStroke.dart';

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
        leading: IconButton(
          icon: Icon(RebbleIconsStroke.caret_left),
          onPressed: () => Navigator.maybePop(context),
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => Navigator.pushReplacementNamed(context, '/setup'),
        label: Row(
          children: <Widget>[
            Text("LET'S GET STARTED"),
            Icon(RebbleIconsStroke.caret_right)
          ],
          mainAxisAlignment: MainAxisAlignment.center,
        ),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      body: SingleChildScrollView(
        child: Column(
          children: <Widget>[Text("Setup language / health / privacy")],
        ),
      ),
    );
  }
}
