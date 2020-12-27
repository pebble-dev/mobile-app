import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:flutter/material.dart';

class TestPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      title: "Cobble",
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: <Widget>[
          RaisedButton(
            child: Text("Connect"),
            onPressed: () {
              //TODO
            },
          )
        ],
      ),
    );
  }
}
