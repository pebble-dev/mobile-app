import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:fossil/ui/common/icons/fonts/RebbleIconsStroke.dart';
import 'package:fossil/ui/home/tabs/StoreTab.dart';
import 'package:fossil/ui/home/tabs/TestTab.dart';
import 'package:fossil/ui/setup/PairPage.dart';

class HomePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _currentIndex = 0;
  List<Widget> _tabs = <Widget>[ //TODO: replace this
    TestTab(),
    PairPage(), // setup page is not the same as devices tab but it works for now
    StoreTab(),
    Placeholder(), //TODO
    Placeholder(), //TODO
  ];

  Map<String, IconData> _tabBarOptions = {
    "Test": RebbleIconsStroke.send_to_watch_checked,
    "Devices": RebbleIconsStroke.devices,
    "Store": RebbleIconsStroke.rebble_store,
    "Notifications": RebbleIconsStroke.notifications,
    "More": RebbleIconsStroke.menu_horizontal,
  };

  void _onTabTap(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Fossil"),
        actions: <Widget>[
          IconButton(
              icon: Icon(RebbleIconsStroke.developer_connection_console,
                  size: 25.0),
              onPressed: () => Navigator.pushNamed(context, '/devoptions'))
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        onTap: _onTabTap,
        currentIndex: _currentIndex,
        items: _tabBarOptions.entries
            .map(
              (entry) => BottomNavigationBarItem(
                icon: Icon(entry.value),
                title: Text(entry.key),
                backgroundColor: Theme.of(context).colorScheme.surface,
              ),
            )
            .toList(),
      ),
      body: _tabs[_currentIndex],
    );
  }
}
