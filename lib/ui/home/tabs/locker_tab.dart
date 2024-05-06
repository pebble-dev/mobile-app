import 'package:cobble/domain/apps/app_compatibility.dart';
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/dao/locker_cache_dao.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/db/models/locker_app.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/home/tabs/locker_tab/apps_item.dart';
import 'package:cobble/ui/home/tabs/locker_tab/faces_card.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_fab.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class LockerTab extends HookConsumerWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final connectionState = ref.watch(connectionStateProvider);

    final currentWatch = connectionState.currentConnectedWatch;

    final appManager = ref.watch(appManagerProvider.notifier);
    List allPackages = ref.watch(appManagerProvider);
    List incompatibleApps =
        allPackages.where((element) => !element.isWatchface).toList();
    List incompatibleFaces =
        allPackages.where((element) => element.isWatchface).toList();
    List compatibleApps = [];
    List compatibleFaces = [];
    WatchType watchType;
    bool circleConnected = false;
    PebbleWatchLine lineConnected = PebbleWatchLine.unknown;
    var lockerCache = ref.watch(lockerCacheDaoProvider).getAll().then((value) => { for (var v in value) v.id : v });

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
          label: indexTab.value == 0
              ? tr.lockerPage.getFaces
              : tr.lockerPage.getApps,
          icon: RebbleIcons.plus_add,
          onPressed: () {},
        ),
        child: FutureBuilder<Map<String, LockerApp>>(
          future: lockerCache,
          builder: (context, snap) {
            if (snap.hasData) {
              return TabBarView(
                children: <Widget>[
                  Padding(
                    padding: const EdgeInsets.all(16),
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
                              .map<Widget>(
                                (face) => FacesCard(
                                  listUrl: snap.data![face.appstoreId]?.getPlatformListImage(currentWatch?.runningFirmware.hardwarePlatform.getWatchType().name ?? ""),
                                  face: face,
                                  compatible: true,
                                  appManager: appManager,
                                  circleConnected: circleConnected,
                                  key: ValueKey(face.uuid),
                              ),
                            ).toList(),
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
                              .map<Widget>(
                                (face) => FacesCard(
                                  listUrl: snap.data![face.appstoreId]?.getPlatformListImage(currentWatch?.runningFirmware.hardwarePlatform.getWatchType().name ?? ""),
                                  face: face,
                                  appManager: appManager,
                                  lineConnected: lineConnected,
                                  key: ValueKey(face.uuid),
                                ),
                              ).toList(),
                          ),
                        ),
                      ],
                    ),
                  ),
                  CustomScrollView(
                    slivers: [
                      SliverReorderableList(
                        itemBuilder: (BuildContext context, int index) {
                          return AppsItem(
                            app: compatibleApps[index],
                            compatible: true,
                            appManager: appManager,
                            index: index,
                            iconUrl: snap.data![compatibleApps[index].appstoreId]?.getPlatformIconImage(currentWatch?.runningFirmware.hardwarePlatform.getWatchType().name ?? ""),
                            key: ValueKey(compatibleApps[index].uuid),
                          );
                        },
                        itemCount: compatibleApps.length,
                        onReorder: (int fromIndex, int toIndex) {
                          if (toIndex > fromIndex) {
                            toIndex -= 1;
                          }
                          App app = compatibleApps[fromIndex];
                          int newOrder = compatibleApps[toIndex].appOrder;
                          appManager.reorderApp(app.uuid, newOrder);

                          /// This would be refreshed anyway, but we will do it manually here so the user doesn't have to see the items jump around
                          /// It may actually cause issues if the user moves this before appManager catches up, so it would probably be worth the effort to add a timeout for reordering with some user feedback
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
                              .map<Widget>(
                                (app) => AppsItem(
                              app: app,
                              appManager: appManager,
                              lineConnected: lineConnected,
                              iconUrl: snap.data![app.appstoreId]?.getPlatformIconImage(currentWatch?.runningFirmware.hardwarePlatform.getWatchType().name ?? ""),
                              key: ValueKey(app.uuid),
                            ),
                          )
                              .toList(),
                        ),
                      ),
                    ],
                  ),
                ],
              );
            } else if (snap.hasError) {
              return const Center(
                child: CompIcon(RebbleIcons.dead_watch_ghost80, RebbleIcons.dead_watch_ghost80_background, size: 80.0, strokeColor: Color.fromARGB(255, 190, 190, 190),),
              );
            } else {
              return const CircularProgressIndicator();
            }
          },
        ),
      ),
    );
  }
}
