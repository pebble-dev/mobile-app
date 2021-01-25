import 'dart:ui';

import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/setup/pair_page.dart';
import 'package:flutter/material.dart';

import '../../common/icons/fonts/rebble_icons.dart';

class MyWatchesTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _MyWatchesTabState();
}

class _MyWatchesTabState extends State<MyWatchesTab> {
  List<PebbleScanDevice> _pebbles = [];

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
        title: "My Watches",
        child: ListView(children: <Widget>[
          Column(
              children: _pebbles
                  .map((e) => InkWell(
                        child: Container(
                            child: Row(children: <Widget>[
                              Container(
                                child: Center(
                                    child: PebbleWatchIcon(
                                        PebbleWatchModel.values[e.color])),
                                width: 56,
                                height: 56,
                                decoration: BoxDecoration(
                                    color: Theme.of(context).dividerColor,
                                    shape: BoxShape.circle),
                              ),
                              SizedBox(width: 16),
                              Column(
                                children: <Widget>[
                                  Text(e.name, style: TextStyle(fontSize: 16)),
                                  SizedBox(height: 4),
                                  Text(e.address
                                      .toRadixString(16)
                                      .padLeft(6, '0')
                                      .toUpperCase()),
                                  Wrap(
                                    spacing: 4,
                                    children: [],
                                  ),
                                ],
                                crossAxisAlignment: CrossAxisAlignment.start,
                              ),
                              Expanded(
                                  child: Container(width: 0.0, height: 0.0)),
                              Icon(RebbleIcons.caret_right,
                                  color:
                                      Theme.of(context).colorScheme.secondary),
                            ]),
                            margin: EdgeInsets.all(16)),
                        onTap: () {},
                      ))
                  .toList()),
          Padding(
              padding: EdgeInsets.symmetric(horizontal: 0.0),
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    FlatButton(
                      child: Text("Add Device"),
                      padding: EdgeInsets.symmetric(horizontal: 32.0),
                      textColor: Theme.of(context).accentColor,
                      onPressed: () => context.push(PairPage()),
                    ),
                  ]))
        ]));
  }

  @override
  void dispose() {
    super.dispose();
  }
}
