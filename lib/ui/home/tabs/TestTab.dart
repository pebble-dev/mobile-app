import 'package:flutter/material.dart';
import 'package:fossil/ui/common/icons/RebbleIconsStroke.dart';

class TestTab extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        RaisedButton(
          onPressed: () {},
          child: Text("Button"),
        ),
        Text("This is some text."),
        Card(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Row(
                children: <Widget>[
                  Text("This is a card, with an icon button:"),
                ],
              ),
              IconButton(
                icon: Icon(
                  RebbleIconsStroke.notifications_megaphone,
                  size: 25.0,
                ),
                onPressed: () {},
              )
            ],
          ),
        )
      ],
    );
  }
}
