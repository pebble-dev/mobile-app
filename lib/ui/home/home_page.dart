import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/home/tabs/locker_tab.dart';
import 'package:cobble/ui/home/tabs/store_tab.dart';
import 'package:cobble/ui/home/tabs/test_tab.dart';
import 'package:cobble/ui/home/tabs/watches_tab.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/router/uri_navigator.dart';
import 'package:cobble/ui/screens/placeholder_screen.dart';
import 'package:cobble/ui/screens/settings.dart';
import 'package:cobble/ui/screens/update_prompt.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import '../common/icons/fonts/rebble_icons.dart';

class _TabConfig {
  final CobbleScreen? child;
  final Function? onSelect;
  final String label;
  final IconData icon;
  final GlobalKey<NavigatorState> key;

  _TabConfig(this.label, this.icon, {this.onSelect, this.child})
      : key = GlobalKey<NavigatorState>();
}

class HomePage extends HookConsumerWidget implements CobbleScreen {
  final _config = [
    // Only visible when in debug mode
    ...kDebugMode
        ? [
            _TabConfig(
              tr.homePage.testing,
              RebbleIcons.send_to_watch_checked,
              child: TestTab(),
            )
          ]
        : [],
    // // TODO: Health not yet implemented
    // _TabConfig(
    //   HealthTab(),
    //   tr.homePage.health,
    //   RebbleIcons.health_journal,
    // ),
    _TabConfig(
      tr.homePage.locker,
      RebbleIcons.locker,
      onSelect: () => KMPApi().openLockerView(),
      child: PlaceholderScreen(),
    ),
    _TabConfig(tr.homePage.store, RebbleIcons.rebble_store, child: StoreTab()),
    _TabConfig(tr.homePage.watches, RebbleIcons.devices, child: MyWatchesTab()),
    // Use the comment below to access the KMP UI version of the Watch Tab
    // by clicking the watch tab in flutter UI, first comment the line above
    // _TabConfig(tr.homePage.watches, RebbleIcons.devices,
    //     onSelect: () => KMPApi().openWatchesView(), child: PlaceholderScreen()),
    _TabConfig(tr.homePage.settings, RebbleIcons.settings, child: Settings()),
  ];

  HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    useUriNavigator(context);

    final index = useState(0);

    final connectionState = ref.watch(connectionStateProvider);
    useEffect(() {
      WidgetsBinding.instance.addPostFrameCallback((Duration duration) {
        if (connectionState.currentConnectedWatch?.runningFirmware.isRecovery ==
            true) {
          context.push(UpdatePrompt(
            confirmOnSuccess: true,
            onSuccess: (screenContext) {
              Navigator.pop(screenContext);
            },
          ));
        }
      });
      return null;
    }, [connectionState]);

    useEffect(() {
      if (_config[index.value].onSelect != null) {
        _config[index.value].onSelect();
      }
      return null;
    });

    return WillPopScope(
      onWillPop: () async {
        /// Ask currently active child Navigator to pop. If child Navigator has
        /// nothing to pop it will return `false`, allowing root navigator to
        /// pop itself, closing the app.
        final popped =
            (await _config[index.value].key.currentState?.maybePop())!;
        return popped == false;
      },
      child: CobbleScaffold.page(
        bottomNavigationBar: BottomNavigationBar(
          type: BottomNavigationBarType.fixed,
          onTap: (i) => index.value = i,
          currentIndex: index.value,
          items: _config
              .map(
                (tab) => BottomNavigationBarItem(
                  icon: Icon(tab.icon),
                  label: tab.label,
                  backgroundColor: Theme.of(context).colorScheme.surface,
                ),
              )
              .toList(),
        ),
        child: IndexedStack(
          index: index.value,
          children: _config
              .map(
                (tab) => Navigator(
                  key: tab.key,
                  onGenerateInitialRoutes: (navigator, initialRoute) => [
                    CupertinoPageRoute(builder: (_) => tab.child),
                  ],
                ),
              )
              .toList(),
        ),
      ),
    );
  }
}
