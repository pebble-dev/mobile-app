import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:fossil/DevOptionsPage.dart';
import 'package:fossil/setup/PairPage.dart';
import 'package:fossil/theme.dart';

import 'icons/rebble_icons_stroke_icons.dart';
import 'maintabs/StoreTab.dart';

class TabsPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _TabsPageState();
}

class _TabsPageState extends State<TabsPage> {
  int _currentIndex = 0;
  List<Widget> _tabs = <Widget>[
    Column(
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
    ), //TODO
    PairPage(),
    StoreTab(),
    Placeholder(), //TODO
    Placeholder(), //TODO
  ];

  void _onTabTap(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: CTheme.colorScheme.surface,
        title: Text("Fossil"),
        actions: <Widget>[
          IconButton(icon: Icon(RebbleIconsStroke.developer_connection_console, size: 25.0), onPressed: () => Navigator.push(context, MaterialPageRoute(builder: (context) => DevOptionsPage())),)
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
          backgroundColor: CTheme.colorScheme.surface,
          selectedItemColor: CTheme.colorScheme.secondary,
          type: BottomNavigationBarType.fixed,
          onTap: _onTabTap,
          currentIndex: _currentIndex,
          items: <BottomNavigationBarItem>[
            BottomNavigationBarItem(
              icon: Icon(RebbleIconsStroke.send_to_watch_checked, size: 25.0),
              title: Text("Test"),
              backgroundColor: CTheme.colorScheme.surface
            ),
            BottomNavigationBarItem(
              icon: Icon(RebbleIconsStroke.devices, size: 25.0),
              title: Text("Devices"),
            ),
            BottomNavigationBarItem(
              icon: Icon(RebbleIconsStroke.rebble_store, size: 25.0),
              title: Text("Store"),
            ),
            BottomNavigationBarItem(
              icon: Icon(RebbleIconsStroke.notifications, size: 25.0),
              title: Text("Notifications"),
            ),
            BottomNavigationBarItem(
              icon: Icon(RebbleIconsStroke.menu_horizontal, size: 25.0),
              title: Text("More"),
            ),
          ]),
      body: _tabs[_currentIndex],
    );
  }
}
