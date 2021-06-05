import 'package:cobble/domain/apps/app_compatibility.dart';
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/home/tabs/locker_tab/apps_item.dart';
import 'package:cobble/ui/home/tabs/locker_tab/faces_card.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_fab.dart';
import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:share/share.dart';

class LockerTab extends HookWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    final connectionState = useProvider(connectionStateProvider.state);

    final currentWatch = connectionState.currentConnectedWatch;

    final appManager = useProvider(appManagerProvider);
    List allPackages = useProvider(appManagerProvider.state);
    List incompatibleApps =
        allPackages.where((element) => !element.isWatchface).toList();
    List incompatibleFaces =
        allPackages.where((element) => element.isWatchface).toList();
    List compatibleApps = [];
    List compatibleFaces = [];
    WatchType watchType;
    bool circleConnected = false;
    PebbleWatchLine lineConnected = PebbleWatchLine.unknown;

    if (currentWatch != null) {
      watchType = currentWatch.runningFirmware.hardwarePlatform.getWatchType();
      circleConnected = watchType == WatchType.chalk;
      lineConnected = currentWatch.line;
      compatibleApps = incompatibleApps
          .where((element) =>
              AppCompatibility(element).isCompatibleWith(watchType))
          .toList();
      compatibleFaces = incompatibleFaces
          .where((element) =>
              AppCompatibility(element).isCompatibleWith(watchType))
          .toList();
      incompatibleApps = incompatibleApps
          .where((element) =>
              !AppCompatibility(element).isCompatibleWith(watchType))
          .toList();
      incompatibleFaces = incompatibleFaces
          .where((element) =>
              !AppCompatibility(element).isCompatibleWith(watchType))
          .toList();
    }

    final indexTab = useState<int>(0);

    return DefaultTabController(
      length: 2,
      child: CobbleScaffold.tab(
        title: " ",
        bottomAppBar: TabBar(
          onTap: (index) {
            indexTab.value = index;
          },
          tabs: [
            Tab(text: tr.lockerPage.myFaces),
            Tab(text: tr.lockerPage.myApps),
          ],
        ),
        floatingActionButton: CobbleFab(
          label: indexTab.value == 0 ? tr.lockerPage.getFaces : tr.lockerPage.getApps,
          icon: RebbleIcons.plus_add,
          onPressed: () {},
        ),
        child: TabBarView(
          children: <Widget>[
            Padding(
              padding: EdgeInsets.all(16),
              child: CustomScrollView(
                slivers: [
                  SliverGrid(
                    gridDelegate: SliverGridDelegateWithMaxCrossAxisExtent(
                      maxCrossAxisExtent: 320.0,
                      mainAxisSpacing: 16.0,
                      crossAxisSpacing: 16.0,
                      mainAxisExtent: 204.0,
                    ),
                    delegate: SliverChildListDelegate(
                      compatibleFaces
                          .map<Widget>((face) => FacesCard(face, true, appManager, circleConnected: circleConnected, key: ValueKey(face.uuid)))
                          .toList(),
                    ),
                  ),
                  if (incompatibleFaces.length > 0)
                    SliverToBoxAdapter(
                      child: Container(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Padding(
                              padding: EdgeInsets.all(16),
                              child: Text(tr.lockerPage.incompatibleFaces),
                            ),
                            CobbleDivider(),
                          ],
                        ),
                      ),
                    ),
                  SliverGrid(
                    gridDelegate: SliverGridDelegateWithMaxCrossAxisExtent(
                      maxCrossAxisExtent: 320.0,
                      mainAxisSpacing: 16.0,
                      crossAxisSpacing: 16.0,
                      mainAxisExtent: 204.0,
                    ),
                    delegate: SliverChildListDelegate(
                      incompatibleFaces
                          .map<Widget>((face) => FacesCard(face, false, appManager, lineConnected: lineConnected, key: ValueKey(face.uuid)))
                          .toList(),
                    ),
                  ),
                ],
              ),
            ),
            CustomScrollView(
              slivers: [
                SliverReorderableList(
                  itemBuilder: (BuildContext context, int index) {
                    return AppsItem(compatibleApps[index], true, appManager, index: index, key: ValueKey(compatibleApps[index].uuid));
                  },
                  itemCount: compatibleApps.length,
                  onReorder: (int fromIndex, int toIndex) {
                    if (toIndex > fromIndex) {
                      toIndex -= 1;
                    }
                    App app = compatibleApps[fromIndex];
                    int newOrder = compatibleApps[toIndex].appOrder;
                    appManager.reorderApp(app.uuid, newOrder);
                    // This would be refreshed anyway, but we will do it manually here so the user doesn't have to see the items jump around
                    // It may actually cause issues if the user moves this before appManager catches up, so it would probably be worth the effort to add a timeout for reordering with some user feedback
                    compatibleApps.insert(
                        toIndex, compatibleApps.removeAt(fromIndex));
                  },
                ),
                if (incompatibleApps.length > 0)
                  SliverToBoxAdapter(
                    child: Container(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: EdgeInsets.all(16),
                            child: Text(tr.lockerPage.incompatibleApps),
                          ),
                          CobbleDivider(),
                        ],
                      ),
                    ),
                  ),
                SliverList(
                  delegate: SliverChildListDelegate(
                    incompatibleApps
                        .map<Widget>((app) => AppsItem(app, false, appManager, lineConnected: lineConnected, key: ValueKey(app.uuid)))
                        .toList(),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
