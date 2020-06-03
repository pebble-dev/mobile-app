import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:fossil/DevOptionsPage.dart';
import 'package:fossil/icons/rebble_icons_stroke_only_icons.dart';
import 'package:fossil/setup/PairPage.dart';

import 'maintabs/StoreTab.dart';

class TabsPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _TabsPageState();
}

class _TabsPageState extends State<TabsPage> {
  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
        length: 5,
        child: Scaffold(
            appBar: AppBar(
              title: Text("Fossil"),
              actions: <Widget>[
                IconButton(icon: Icon(RebbleIconsStrokeOnly.devices), onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (context) => PairPage())),),
                IconButton(icon: Icon(Icons.code), onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (context) => DevOptionsPage())),)
              ],
            ),
            bottomNavigationBar: BottomAppBar(
                child: Container(
                    height: 60,
                    child: TabBar(
                      labelColor: Colors.white,
                      unselectedLabelColor: Color(0x80FFFFFF),
                      labelPadding: EdgeInsets.all(0.5),
                      tabs: <Widget>[
                        Tab(
                            iconMargin: EdgeInsets.only(bottom: 5.0),
                            icon: Icon(Icons.crop_square),
                            text: "Health"),
                        Tab(
                            iconMargin: EdgeInsets.only(bottom: 5.0),
                            icon: Icon(Icons.crop_square),
                            text: "Locker"),
                        Tab(
                            iconMargin: EdgeInsets.only(bottom: 5.0),
                            icon: Icon(Icons.crop_square),
                            text: "Store"),
                        Tab(
                            iconMargin: EdgeInsets.only(bottom: 5.0),
                            icon: Icon(Icons.crop_square),
                            text: "Notifications"),
                        Tab(
                            iconMargin: EdgeInsets.only(bottom: 5.0),
                            icon: Icon(Icons.crop_square),
                            text: "More"),
                      ],
                    ))),
            body: TabBarView(
              children: <Widget>[
                Scaffold(
                  body: Column(
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
                                Icons.brightness_medium,
                                color: Color(0xFFF9A285),
                              ),
                              onPressed: () {},
                            )
                          ],
                        ),
                      )
                    ],
                  ),
                ), //TODO
                Scaffold(), //TODO
                StoreTab(),
                Scaffold(), //TODO
                Scaffold(), //TODO
              ],
            )));
  }
}
