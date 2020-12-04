import 'dart:developer';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:cobble/domain/entities/PebbleDevice.dart';
import 'package:cobble/infrastructure/datasources/PairedStorage.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:cobble/ui/common/icons/WatchIcon.dart';
import 'package:cobble/ui/common/icons/fonts/RebbleIconsStroke.dart';
import 'package:shared_preferences/shared_preferences.dart';

class MyWatchesTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _MyWatchesTabState();
}

class _MyWatchesTabState extends State<MyWatchesTab> {
  List<PebbleDevice> _pebbles = [] ;

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("My Watches"),
          leading: BackButton(),
        ),
        body: ListView(children: <Widget>[
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
                            children: [
                            ],
                          ),
                        ],
                        crossAxisAlignment: CrossAxisAlignment.start,
                      ),
                      Expanded(
                          child: Container(width: 0.0, height: 0.0)),
                      Icon(RebbleIconsStroke.caret_right,
                          color:
                          Theme.of(context).colorScheme.secondary),
                    ]),
                    margin: EdgeInsets.all(16)),
                onTap: () {

                },
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
                      textColor: Theme
                          .of(context)
                          .accentColor,
                      onPressed: () => Navigator.pushNamed(context, '/pair'),
                    ),
                  ]))
        ]));
  }

  @override
  void dispose() {
    super.dispose();
  }
}
