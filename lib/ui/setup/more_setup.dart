import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/setup/boot/rebble_setup.dart';
import 'package:flutter/material.dart';

import '../common/icons/fonts/rebble_icons.dart';

class MoreSetup extends StatefulWidget implements CobbleScreen {
  @override
  State<StatefulWidget> createState() => _MoreSetupState();
}

class _MoreSetupState extends State<MoreSetup> {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold.page(
      title: "More setup",
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.pushReplacement(RebbleSetup()),
        label: Row(
          children: <Widget>[
            Text("LET'S GET STARTED"),
            Icon(RebbleIcons.caret_right)
          ],
          mainAxisAlignment: MainAxisAlignment.center,
        ),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerFloat,
      child: SingleChildScrollView(
        child: Column(
          children: <Widget>[Text("Setup language / health / privacy")],
        ),
      ),
    );
  }
}
