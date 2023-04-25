import 'dart:ui';

import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_fab.dart';
import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/setup/pair_page.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class MyWatchesTab extends HookConsumerWidget implements CobbleScreen {
  final Color _disconnectedColor = Color.fromRGBO(255, 255, 255, 0.5);
  final Color _connectedColor = Color.fromARGB(255, 0, 169, 130);

  void getCurrentWatchStatus() {}
  final UiConnectionControl uiConnectionControl = UiConnectionControl();
  final ConnectionControl connectionControl = ConnectionControl();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final connectionState = ref.watch(connectionStateProvider);
    final defaultWatch = ref.watch(defaultWatchProvider);
    final pairedStorage = ref.watch(pairedStorageProvider.notifier);
    final allWatches = ref.watch(pairedStorageProvider);
    final preferencesFuture = ref.watch(preferencesProvider.future);

    List<PebbleScanDevice> allWatchesList =
        allWatches.map((e) => e.device).toList();
    List<PebbleScanDevice> allDisconnectedWatches = allWatchesList.toList();
    if (defaultWatch != null &&
        (connectionState.isConnected == true ||
            connectionState.isConnecting!)) {
      //TODO: Save the data from the connected watch after first connection(i.e, not here)
      defaultWatch.color = connectionState.currentConnectedWatch!.model.index;
      defaultWatch.version =
          connectionState.currentConnectedWatch!.runningFirmware.version;
      //Hide the default watch if we're connected or connecting to it. We don't need to see it twice!
      allDisconnectedWatches.remove(defaultWatch);
    }

    List<PebbleDevice?> connectedWatchList;
    if (connectionState.currentConnectedWatch != null) {
      connectedWatchList = [connectionState.currentConnectedWatch];
    } else {
      connectedWatchList = [];
    }

    bool isConnected;
    bool isConnecting;

    if (connectionState.isConnecting == true) {
      isConnecting = true;
      isConnected = false;
    } else if (connectionState.isConnected == true) {
      isConnecting = false;
      isConnected = true;
    } else {
      isConnecting = false;
      isConnected = false;
    }

    String _getStatusText(String? address) {
      if (connectionState.isConnected! &&
          connectionState.currentWatchAddress == address)
        return tr.watchesPage.status.connected;
      else if (connectionState.isConnecting! &&
          connectionState.currentWatchAddress == address)
        return tr.watchesPage.status.connecting;
      else
        return tr.watchesPage.status.disconnected;
    }

    Color _getBrStatusColor(dynamic device) {
      if (connectionState.currentWatchAddress == device.address &&
          !isConnecting)
        return _connectedColor;
      else
        return _disconnectedColor;
    }

    Color _getStatusColor(PebbleScanDevice device) {
      // In the future this will return green for firmware update status
      return context.scheme!.muted;
    }

    void _onDisconnectPressed(bool inSettings) {
      connectionControl.disconnect();
      pairedStorage.clearDefault();
      if (inSettings) Navigator.pop(context);
    }

    void _onConnectPressed(PebbleScanDevice device, inSettings) {
      StringWrapper addressWrapper = StringWrapper();
      addressWrapper.value = device.address;

      pairedStorage.setDefault(device.address!);
      uiConnectionControl.connectToWatch(addressWrapper);
      if (inSettings) Navigator.pop(context);
    }

    void _onForgetPressed(PebbleScanDevice device) async {
      if (connectionState.currentWatchAddress == device.address) {
        connectionControl.disconnect();
      }

      final deviceAddressWrapper = StringWrapper();
      deviceAddressWrapper.value = device.address!;
      uiConnectionControl.unpairWatch(deviceAddressWrapper);

      final preferences = await preferencesFuture;
      preferences.reload();
      if (preferences.getLastConnectedWatchAddress() == device.address) {
        preferences.setLastConnectedWatchAddress("");
      }

      pairedStorage.unregister(device.address);
      Navigator.pop(context);
    }

    void _onUpdatePressed(PebbleScanDevice device) {
      Navigator.pop(context);
      //TODO
    }

    void _onSettingsPressed(bool isConnected, String? address) {
      PebbleScanDevice device =
          allWatchesList.firstWhere((e) => e.address == address);

      CobbleSheet.showModal(
          context: context,
          builder: (context) {
            return Container(
              child: Wrap(
                children: <Widget>[
                  Container(
                    child: Row(children: <Widget>[
                      PebbleWatchIcon(PebbleWatchModel.values[device.color!],
                          backgroundColor: _getBrStatusColor(device)),
                      SizedBox(width: 16),
                      Column(
                        children: <Widget>[
                          Text(device.name!, style: TextStyle(fontSize: 16)),
                          SizedBox(height: 4),
                          Text(
                              device.version.toString() +
                                  " - " +
                                  _getStatusText(device.address),
                              style: TextStyle(color: _getStatusColor(device))),
                          Wrap(
                            spacing: 4,
                            children: [],
                          ),
                        ],
                        crossAxisAlignment: CrossAxisAlignment.start,
                      ),
                      Expanded(child: Container(width: 0.0, height: 0.0)),
                    ]),
                    margin: EdgeInsets.all(16.0),
                  ),
                  CobbleDivider(),
                  Offstage(
                    offstage: isConnected,
                    child: CobbleTile.action(
                      leading: RebbleIcons.connect_to_watch,
                      title: tr.watchesPage.action.connect,
                      onTap: () => _onConnectPressed(device, true),
                    ),
                  ),
                  Offstage(
                    offstage: !isConnected,
                    child: CobbleTile.action(
                      leading: RebbleIcons.disconnect_from_watch,
                      title: tr.watchesPage.action.disconnect,
                      onTap: () => _onDisconnectPressed(true),
                    ),
                  ),
                  CobbleTile.action(
                    leading: RebbleIcons.check_for_updates,
                    title: tr.watchesPage.action.checkUpdates,
                    onTap: () => _onUpdatePressed(device),
                  ),
                  CobbleDivider(),
                  CobbleTile.action(
                    leading: RebbleIcons.x_close,
                    title: tr.watchesPage.action.forget,
                    intent: context.scheme!.destructive,
                    onTap: () => _onForgetPressed(device),
                  ),
                ],
              ),
            );
          });
    }

    return CobbleScaffold.tab(
      title: tr.watchesPage.title,
      child: ListView(children: <Widget>[
        if (!isConnecting && !isConnected) ...[
          Column(children: <Widget>[
            Container(
                child: Row(children: <Widget>[
                  Container(
                    child: Center(
                        child: CompIcon(RebbleIcons.disconnect_from_watch,
                            RebbleIcons.disconnect_from_watch_background)),
                    width: 56,
                    height: 56,
                    decoration: BoxDecoration(
                        color: _disconnectedColor, shape: BoxShape.circle),
                  ),
                  SizedBox(width: 16),
                  Column(
                    children: <Widget>[
                      Text(tr.watchesPage.status.nothingConnected,
                          style: TextStyle(fontSize: 16)),
                      SizedBox(height: 4),
                      Text(tr.watchesPage.status.backgroundServiceStopped),
                      Wrap(
                        spacing: 4,
                        children: [],
                      ),
                    ],
                    crossAxisAlignment: CrossAxisAlignment.start,
                  ),
                  Expanded(child: Container(width: 0.0, height: 0.0)),
                ]),
                margin: EdgeInsets.all(16)),
          ])
        ],
        if (isConnecting || isConnected) ...[
          Column(
              children: connectedWatchList
                  .map((e) => InkWell(
                        child: Container(
                            child: Row(children: <Widget>[
                              Container(
                                child: Center(
                                    child: PebbleWatchIcon(e!.model,
                                        backgroundColor: _getBrStatusColor(e))),
                              ),
                              SizedBox(width: 16),
                              Column(
                                children: <Widget>[
                                  Text(e.name!, style: TextStyle(fontSize: 16)),
                                  SizedBox(height: 4),
                                  Text(_getStatusText(e.address),
                                      style: TextStyle(
                                          color: context.scheme!.muted)),
                                  Wrap(
                                    spacing: 4,
                                    children: [],
                                  ),
                                ],
                                crossAxisAlignment: CrossAxisAlignment.start,
                              ),
                              Expanded(
                                  child: Container(width: 0.0, height: 0.0)),
                              CobbleButton(
                                outlined: false,
                                icon: RebbleIcons.disconnect_from_watch,
                                onPressed: () => _onDisconnectPressed(false),
                              ),
                            ]),
                            margin: EdgeInsets.all(16)),
                        onTap: () => _onSettingsPressed(true, e.address),
                      ))
                  .toList()),
        ],
        CobbleTile.title(title: tr.watchesPage.allWatches),
        CobbleDivider(),
        Column(
            children: allDisconnectedWatches
                .map((e) => InkWell(
                      child: Container(
                        child: Row(children: <Widget>[
                          Container(
                            child: Center(
                                child: PebbleWatchIcon(
                                    PebbleWatchModel.values[e.color ?? 0],
                                    backgroundColor: _getBrStatusColor(e))),
                          ),
                          SizedBox(width: 16),
                          Column(
                            children: <Widget>[
                              Text(e.name!, style: TextStyle(fontSize: 16)),
                              SizedBox(height: 4),
                              Text(_getStatusText(e.address)),
                              Wrap(
                                spacing: 4,
                                children: [],
                              ),
                            ],
                            crossAxisAlignment: CrossAxisAlignment.start,
                          ),
                          Expanded(child: Container(width: 0.0, height: 0.0)),
                          CobbleButton(
                            outlined: false,
                            icon: RebbleIcons.connect_to_watch,
                            onPressed: () => _onConnectPressed(e, false),
                          ),
                        ]),
                        margin: EdgeInsets.fromLTRB(16, 10, 16, 16),
                      ),
                      onTap: () => _onSettingsPressed(false, e.address!),
                    ))
                .toList()),
      ]),
      floatingActionButton: CobbleFab(
        onPressed: () => context.push(PairPage.fromTab()),
        label: tr.watchesPage.fab,
        icon: RebbleIcons.plus_add,
      ),
    );
  }
}
