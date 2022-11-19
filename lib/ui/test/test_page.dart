import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:flutter/material.dart';

class TestPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold.page(
      title: "Cobble",
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          CobbleButton(
            label: "Connect",
            onPressed: () {
              //TODO
            },
          )
        ],
      ),
    );
  }
}
