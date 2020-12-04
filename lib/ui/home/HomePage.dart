import 'dart:math';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:cobble/ui/Theme.dart';
import 'package:cobble/ui/common/icons/WatchIcon.dart';
import 'package:cobble/ui/common/icons/fonts/RebbleIconsStroke.dart';
import 'package:cobble/ui/home/tabs/StoreTab.dart';
import 'package:cobble/ui/home/tabs/TestTab.dart';
import 'package:cobble/ui/home/tabs/settings_tab.dart';
import 'package:cobble/ui/home/tabs/watches_tab.dart';
import 'package:cobble/ui/setup/FirstRunPage.dart';
import 'package:cobble/ui/setup/PairPage.dart';
import 'package:cobble/ui/test/WatchCarousel.dart';

class HomePage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _currentIndex = 0;
  List<Widget> _tabs = <Widget>[
    //TODO: replace this
    TestTab(),
    Placeholder(),
    WatchCarousel(),
    StoreTab(),
    MyWatchesTab(),
    SettingsTab(),
  ];

  Map<String, IconData> _tabBarOptions = {
    "Testing": RebbleIconsStroke.send_to_watch_checked,
    "Health": RebbleIconsStroke.health,
    "Locker": RebbleIconsStroke.locker,
    "Store": RebbleIconsStroke.rebble_store,
    "Watches": RebbleIconsStroke.devices,
    "Settings": RebbleIconsStroke.settings,
  };

  void _onTabTap(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        backgroundColor: RebbleTheme.colorScheme.surface,
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
