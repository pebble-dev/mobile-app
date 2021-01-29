import 'dart:ui';

import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';

import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/setup/pair_page.dart';

import '../../common/icons/fonts/rebble_icons.dart';

class MyWatchesTab extends HookWidget {
  final Color _disconnectedColor = Color.fromRGBO(255, 255, 255, 0.5);
  final Color _connectedColor = Color.fromARGB(255, 0, 255, 170);
  final Color _connectedBrColor = Color.fromARGB(255, 0, 169, 130);

  void getCurrentWatchStatus() {}
  final UiConnectionControl uiConnectionControl = UiConnectionControl();
  final ConnectionControl connectionControl = ConnectionControl();

  @override
  Widget build(BuildContext context) {
    final connectionState = useProvider(connectionStateProvider.state);
    final defaultWatch = useProvider(defaultWatchProvider);
    final pairedStorage = useProvider(pairedStorageProvider);
    final allWatches = useProvider(pairedStorageProvider.state);

    List<PebbleScanDevice> allWatchesList =
        allWatches.map((e) => e.device).toList();
    List<PebbleScanDevice> allDisconnectedWatches = allWatchesList.toList();
    if (defaultWatch != null && connectionState.isConnected == true) {
      //TODO: Save the data from the connected watch after first connection(i.e, not here)
      defaultWatch.color = connectionState.currentConnectedWatch.model.index;
      defaultWatch.version =
          connectionState.currentConnectedWatch.runningFirmware.version;
      //Hide the default watch if we're connected to it. We don't need to see it twice!
      allDisconnectedWatches.remove(defaultWatch);
    }

    List<PebbleDevice> connectedWatchList;
    if (connectionState.currentConnectedWatch != null) {
      connectedWatchList = [connectionState.currentConnectedWatch];
    } else {
      connectedWatchList = [];
    }

    bool isConnected;

    if (connectionState.isConnecting == true) {
      isConnected = false;
    } else if (connectionState.isConnected == true) {
      isConnected = true;
    } else {
      isConnected = false;
    }

    String _getStatusText(int address) {
      if (connectionState.isConnected &&
          connectionState.currentWatchAddress == address)
        return "Connected";
      else if (connectionState.isConnecting &&
          connectionState.currentWatchAddress == address)
        return "Connecting...";
      else
        return "Disconnected";
    }

    Color _getBrStatusColor(PebbleScanDevice device) {
      if (connectionState.currentWatchAddress == device.address)
        return _connectedBrColor;
      else
        return _disconnectedColor;
    }

    Color _getStatusColor(PebbleScanDevice device) {
      if (connectionState.currentWatchAddress == device.address)
        return _connectedColor;
      else
        return _disconnectedColor;
    }

    void _onDisconnectPressed(bool inSettings) {
      connectionControl.disconnect();
      if (inSettings) Navigator.pop(context);
    }

    void _onConnectPressed(PebbleScanDevice device, inSettings) {
      NumberWrapper addressWrapper = NumberWrapper();
      addressWrapper.value = device.address;

      uiConnectionControl.connectToWatch(addressWrapper);
      if (inSettings) Navigator.pop(context);
    }

    void _onForgetPressed(PebbleScanDevice device) {
      pairedStorage.unregister(device.address);
      Navigator.pop(context);
    }

    void _onUpdatePressed(PebbleScanDevice device) {
      Navigator.pop(context);
      //TODO
    }

    void _onSettingsPressed(bool isConnected, int address) {
      PebbleScanDevice device =
          allWatchesList.firstWhere((e) => e.address == address);

      showModalBottomSheet(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.vertical(top: Radius.circular(15.0)),
          ),
          context: context,
          isScrollControlled: true,
          builder: (context) {
            return Container(
              //Todo:use theme
              color: Color.fromARGB(1, 65, 65, 65),
              child: Wrap(
                children: <Widget>[
                  Container(
                    child: Row(children: <Widget>[
                      Container(
                        child: Center(
                            child: PebbleWatchIcon(
                                PebbleWatchModel.values[device.color])),
                        width: 56,
                        height: 56,
                        decoration: BoxDecoration(
                            color: _getBrStatusColor(device),
                            shape: BoxShape.circle),
                      ),
                      SizedBox(width: 16),
                      Column(
                        children: <Widget>[
                          Text(device.name, style: TextStyle(fontSize: 16)),
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
                    margin: EdgeInsets.fromLTRB(16, 16, 16, 0),
                  ),
                  const Divider(
                    color: Colors.white24,
                    height: 20,
                    thickness: 2,
                    indent: 0,
                    endIndent: 0,
                  ),
                  Offstage(
                    offstage: isConnected,
                    child: ListTile(
                      leading: Icon(RebbleIcons.connect_to_watch),
                      title: Text('Connect to watch'),
                      onTap: () => _onConnectPressed(device, true),
                    ),
                  ),
                  Offstage(
                    offstage: !isConnected,
                    child: ListTile(
                      leading: Icon(RebbleIcons.disconnect_from_watch),
                      title: Text('Disconnect from watch'),
                      onTap: () => _onDisconnectPressed(true),
                    ),
                  ),
                  ListTile(
                    leading: Icon(RebbleIcons.check_for_updates),
                    title: Text('Check for updates'),
                    onTap: () => _onUpdatePressed(device),
                  ),
                  const Divider(
                    color: Colors.white24,
                    height: 20,
                    thickness: 2,
                    indent: 0,
                    endIndent: 0,
                  ),
                  ListTile(
                    leading: Icon(RebbleIcons.x_close, color: Colors.red),
                    title: Text('Forget watch',
                        style: TextStyle(color: Colors.red)),
                    onTap: () => _onForgetPressed(device),
                  ),
                ],
              ),
            );
          });
    }

    return CobbleScaffold(
      title: "My Watches",
      child: ListView(children: <Widget>[
        Offstage(
            offstage: isConnected,
            child: Column(children: <Widget>[
              Container(
                  child: Row(children: <Widget>[
                    Container(
                      child: Center(
                          child: Icon(RebbleIcons.disconnect_from_watch,
                              color: Colors.black)),
                      width: 56,
                      height: 56,
                      decoration: BoxDecoration(
                          color: _disconnectedColor, shape: BoxShape.circle),
                    ),
                    SizedBox(width: 16),
                    Column(
                      children: <Widget>[
                        Text("Nothing connected",
                            style: TextStyle(fontSize: 16)),
                        SizedBox(height: 4),
                        Text("Background service stopped"),
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
            ])),
        Offstage(
          offstage: !isConnected,
          child: Column(
              children: connectedWatchList
                  .map((e) => InkWell(
                        child: Container(
                            child: Row(children: <Widget>[
                              Container(
                                child: Center(child: PebbleWatchIcon(e.model)),
                                width: 56,
                                height: 56,
                                decoration: BoxDecoration(
                                    color: _connectedBrColor,
                                    shape: BoxShape.circle),
                              ),
                              SizedBox(width: 16),
                              Column(
                                children: <Widget>[
                                  Text(e.name, style: TextStyle(fontSize: 16)),
                                  SizedBox(height: 4),
                                  Text(_getStatusText(e.address),
                                      style: TextStyle(color: _connectedColor)),
                                  Wrap(
                                    spacing: 4,
                                    children: [],
                                  ),
                                ],
                                crossAxisAlignment: CrossAxisAlignment.start,
                              ),
                              Expanded(
                                  child: Container(width: 0.0, height: 0.0)),
                              Padding(
                                padding: EdgeInsets.fromLTRB(0, 0, 5, 0),
                                child: IconButton(
                                  icon: Icon(RebbleIcons.disconnect_from_watch,
                                      color: Theme.of(context)
                                          .colorScheme
                                          .secondary),
                                  onPressed: () => _onDisconnectPressed(false),
                                ),
                              ),
                              IconButton(
                                  icon: Icon(RebbleIcons.menu_vertical,
                                      color: Theme.of(context)
                                          .colorScheme
                                          .secondary),
                                  onPressed: () =>
                                      _onSettingsPressed(true, e.address)),
                            ]),
                            margin: EdgeInsets.all(16)),
                        onTap: () {},
                      ))
                  .toList()),
        ),
        Padding(
            padding: EdgeInsets.fromLTRB(15, 25, 15, 5),
            child: Text('All watches', style: TextStyle(fontSize: 18))),
        const Divider(
          color: Colors.white24,
          height: 20,
          thickness: 2,
          indent: 15,
          endIndent: 15,
        ),
        Column(
            children: allDisconnectedWatches
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
                                color: _disconnectedColor,
                                shape: BoxShape.circle),
                          ),
                          SizedBox(width: 16),
                          Column(
                            children: <Widget>[
                              Text(e.name, style: TextStyle(fontSize: 16)),
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
                          Padding(
                            padding: EdgeInsets.fromLTRB(0, 0, 5, 0),
                            child: IconButton(
                              icon: Icon(RebbleIcons.connect_to_watch,
                                  color:
                                      Theme.of(context).colorScheme.secondary),
                              onPressed: () => _onConnectPressed(e, false),
                            ),
                          ),
                          IconButton(
                              icon: Icon(RebbleIcons.menu_vertical,
                                  color:
                                      Theme.of(context).colorScheme.secondary),
                              onPressed: () =>
                                  _onSettingsPressed(false, e.address)),
                        ]),
                        margin: EdgeInsets.fromLTRB(16, 10, 16, 16),
                      ),
                      onTap: () {},
                    ))
                .toList()),
      ]),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push(PairPage()),
        label: Text('PAIR A WATCH'),
        icon: Icon(Icons.add),
      ),
    );
  }
}
