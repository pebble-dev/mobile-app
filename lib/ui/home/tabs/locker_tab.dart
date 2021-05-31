import 'package:cobble/domain/apps/app_compatibility.dart';
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/localization/localization.dart';
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

    Widget facesPreview(App face, bool compatible, bool extended) {
      bool circleWatchface = false;
      bool circleOnly = face.supportedHardware.length == 1 &&
          face.supportedHardware[0] == WatchType.chalk;
      circleWatchface = compatible && circleConnected || !compatible && circleOnly;
      return Container(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ClipRRect(
              child: Image(
                image: Svg('images/temp_watch_face.svg'),
                width: 92,
                height: circleWatchface ? 92 : 108,
                alignment: AlignmentDirectional.center,
                fit: BoxFit.cover,
              ),
              borderRadius: BorderRadius.circular(circleWatchface ? 46.0 : 6.0),
            ),
            SizedBox(height: 8),
            Text(
              face.longName + (extended ? " ${face.version}" : ""),
              style: context.textTheme.headline6,
            ),
            SizedBox(height: 4),
            Text(
              face.company,
              style: context.textTheme.bodyText2!.copyWith(
                color: context.textTheme.bodyText2!.color!.withOpacity(
                  context.scheme!.muted.opacity,
                ),
              ),
            ),
          ],
        ),
      );
    }

    void _showFacesSheet(App face, bool compatible) {
      CobbleSheet.showModal(
        context: context,
        builder: (context) => Column(
          children: [
            SizedBox(height: 8),
            facesPreview(face, compatible, false),
            SizedBox(height: 4),
            // TODO: Implement getting metadata from the store (including the preview)
            if (face.appstoreId != null)
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.rebble_store,
                    onPressed: () {},
                  ),
                  SizedBox(width: 16),
                  CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.health_heart,
                    label: "0",
                    onPressed: () {},
                  ),
                  SizedBox(width: 16),
                  CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.share,
                    onPressed: () => Share.share(
                        "https://store-beta.rebble.io/app/${face.appstoreId}"),
                  ),
                ],
              ),
            SizedBox(height: 16),
            CobbleDivider(),
            if (compatible)
              Container(
                child: Column(
                  children: [
                    CobbleTile.action(
                      leading: RebbleIcons.send_to_watch_unchecked,
                      title: tr.lockerPage.apply,
                      onTap: () {},
                    ),
                    CobbleDivider(),
                    // TODO: Implement permissions
                    CobbleTile.action(
                      leading: RebbleIcons.permissions,
                      title: tr.lockerPage.permissions,
                      onTap: () {},
                    ),
                    CobbleTile.action(
                      leading: RebbleIcons.settings,
                      title: tr.lockerPage.faceSettings,
                      onTap: () {},
                    ),
                  ],
                ),
              )
            else
              CobbleTile.action(
                leading: RebbleIcons.unpair_from_watch,
                title: tr.lockerPage.notCompatible(
                  watch: stringFromWatchLine(lineConnected),
                ),
                onTap: () {},
              ),
            CobbleDivider(),
            CobbleTile.action(
              leading: RebbleIcons.delete_trash,
              title: tr.lockerPage.delete,
              onTap: () => appManager.deleteApp(face.uuid),
            ),
          ],
        ),
      );
    }

    Card _facesCard(App face, bool compatible) {
      return Card(
        key: ValueKey(face.uuid),
        child: Container(
          margin: EdgeInsets.all(8),
          child: Row(
            children: <Widget>[
              Expanded(
                child: facesPreview(face, compatible, false),
              ),
              Column(
                children: <Widget>[
                  // TODO: Implement sending to the watch
                  Expanded(
                    child: compatible
                        ? CobbleButton(
                            outlined: false,
                            icon: RebbleIcons.send_to_watch_unchecked,
                            onPressed: () {},
                          )
                        : Container(),
                  ),
                  // TODO: Implement settings and showing and hiding this button depending on whether or not settings are available
                  Expanded(
                    child: CobbleButton(
                      outlined: false,
                      icon: RebbleIcons.settings,
                      onPressed: () {},
                    ),
                  ),
                  Expanded(
                    child: CobbleButton(
                      outlined: false,
                      icon: RebbleIcons.menu_vertical,
                      onPressed: () => _showFacesSheet(face, compatible),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      );
    }

    void _showAppsSheet(App app, bool compatible) {
      CobbleSheet.showModal(
        context: context,
        builder: (context) => Column(
          children: [
            CobbleTile.app(
              leading: Svg('images/temp_watch_app.svg'),
              title: "${app.longName} ${app.version}",
              subtitle: app.company,
            ),
            SizedBox(height: 8),
            if (app.appstoreId != null)
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.rebble_store,
                    onPressed: () {},
                  ),
                  SizedBox(width: 16),
                  CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.health_heart,
                    label: "0",
                    onPressed: () {},
                  ),
                  SizedBox(width: 16),
                  CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.share,
                    onPressed: () => Share.share(
                        "https://store-beta.rebble.io/app/${app.appstoreId}"),
                  ),
                ],
              ),
            SizedBox(height: 16),
            CobbleDivider(),
            if (compatible)
              Container(
                child: Column(
                  children: [
                    CobbleTile.action(
                      leading: RebbleIcons.permissions,
                      title: tr.lockerPage.permissions,
                      onTap: () {},
                    ),
                    CobbleTile.action(
                      leading: RebbleIcons.settings,
                      title: tr.lockerPage.appSettings,
                      onTap: () {},
                    ),
                  ],
                ),
              )
            else
              CobbleTile.action(
                leading: RebbleIcons.unpair_from_watch,
                title: tr.lockerPage.notCompatible(
                  watch: stringFromWatchLine(lineConnected),
                ),
                onTap: () {},
              ),
            CobbleDivider(),
            CobbleTile.action(
              leading: RebbleIcons.delete_trash,
              title: tr.lockerPage.delete,
              onTap: () => appManager.deleteApp(app.uuid),
            ),
          ],
        ),
      );
    }

    Widget _appsItem(App app, bool compatible) {
      return Container(
        key: ValueKey(app.uuid),
        height: 72.0,
        child: Row(
          children: [
            if (compatible)
              ReorderableDragStartListener(
                child: Padding(
                  padding: EdgeInsets.only(left: 16),
                  child: Icon(RebbleIcons.drag_handle,
                      size: 25.0, color: context.scheme!.muted),
                ),
                index: compatibleApps.indexOf(app),
              )
            else
              SizedBox(width: 57),
            Expanded(
              child: CobbleTile.app(
                leading: Svg('images/temp_watch_app.svg'),
                title: app.longName,
                subtitle: app.company,
                onTap: () => _showAppsSheet(app, compatible),
                child: CobbleButton(
                  outlined: false,
                  icon: compatible
                      ? RebbleIcons.settings
                      : RebbleIcons.menu_vertical,
                  onPressed: () {},
                ),
              ),
            ),
          ],
        ),
      );
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
                          .map<Widget>((face) => _facesCard(face, true))
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
                          .map<Widget>((face) => _facesCard(face, false))
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
                    return _appsItem(compatibleApps[index], true);
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
                        .map<Widget>((app) => _appsItem(app, false))
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
