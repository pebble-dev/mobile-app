import 'dart:developer';
import 'dart:ui';

import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

class PairPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _PairPageState();
}

final ConnectionControl connectionControl = ConnectionControl();
final ScanControl scanControl = ScanControl();

class _PairPageState extends State<PairPage>
    implements ScanCallbacks, PairCallbacks {
  List<PebbleScanDevice> _pebbles = [];
  bool _scanning = false;

  @override
  void initState() {
    super.initState();
    ScanCallbacks.setup(this);
    PairCallbacks.setup(this);
    log("Prestart");
    scanControl.startBleScan();
  }

  void _refreshDevicesBle() {
    if (!_scanning) {
      setState(() {
        _scanning = true;
        _pebbles = [];
        scanControl.startBleScan();
      });
    }
  }

  void _refreshDevicesClassic() {
    if (!_scanning) {
      setState(() {
        _scanning = true;
        _pebbles = [];
        scanControl.startClassicScan();
      });
    }
  }

  void _targetPebble(PebbleScanDevice dev) {
    NumberWrapper addressWrapper = NumberWrapper();
    addressWrapper.value = dev.address;
    connectionControl.connectToWatch(addressWrapper);
  }

  @override
  void onScanStarted() {
    log("Scan started");
    setState(() {
      _scanning = true;
    });
  }

  @override
  void onScanStopped() {
    setState(() {
      _scanning = false;
    });
  }

  @override
  void onScanUpdate(ListWrapper arg) {
    setState(() {
      _pebbles = (arg.value.cast<Map>())
          .map((element) => PebbleScanDevice.fromMap(element))
          .toList();
    });
  }

  @override
  void onWatchPairComplete(NumberWrapper address) {
    PebbleScanDevice dev = _pebbles.firstWhere(
        (element) => element.address == address.value,
        orElse: () => null);

    if (dev == null) {
      return;
    }

    setState(() {
      PairedStorage.register(dev)
          .then((_) => PairedStorage.getDefault().then((def) {
                if (def == null) {
                  PairedStorage.setDefault(dev.address);
                }
              })); // Register + set as default if no default set
      SharedPreferences.getInstance().then((value) {
        if (!value.containsKey("firstRun")) {
          Navigator.pushReplacementNamed(context, '/moresetup');
        } else {
          Navigator.pushReplacementNamed(context, '/home');
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("Pair a watch"),
          leading: BackButton(),
        ),
        body: ListView(children: <Widget>[
          Column(
              children: _pebbles
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
                                    color: Theme.of(context).dividerColor,
                                    shape: BoxShape.circle),
                              ),
                              SizedBox(width: 16),
                              Column(
                                children: <Widget>[
                                  Text(e.name, style: TextStyle(fontSize: 16)),
                                  SizedBox(height: 4),
                                  Text(e.address
                                      .toRadixString(16)
                                      .padLeft(6, '0')
                                      .toUpperCase()),
                                  Wrap(
                                    spacing: 4,
                                    children: [
                                      Offstage(
                                          offstage: !e.runningPRF || e.firstUse,
                                          child: Chip(
                                            backgroundColor: Colors.deepOrange,
                                            label: Text("Recovery"),
                                          )),
                                      Offstage(
                                          offstage: !e.firstUse,
                                          child: Chip(
                                            backgroundColor: Color(0xffd4af37),
                                            label: Text("New!"),
                                          )),
                                    ],
                                  ),
                                ],
                                crossAxisAlignment: CrossAxisAlignment.start,
                              ),
                              Expanded(
                                  child: Container(width: 0.0, height: 0.0)),
                              Icon(RebbleIconsStroke.caret_right,
                                  color:
                                      Theme.of(context).colorScheme.secondary),
                            ]),
                            margin: EdgeInsets.all(16)),
                        onTap: () {
                          _targetPebble(e);
                        },
                      ))
                  .toList()),
          Offstage(
              offstage: !_scanning,
              child: Padding(
                  padding: EdgeInsets.all(16.0),
                  child: Center(child: CircularProgressIndicator()))),
          Padding(
              padding: EdgeInsets.symmetric(horizontal: 0.0),
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Offstage(
                      offstage: _scanning,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          FlatButton(
                            child: Text("SEARCH AGAIN WITH BLE"),
                            padding: EdgeInsets.symmetric(horizontal: 32.0),
                            textColor: Theme
                                .of(context)
                                .accentColor,
                            onPressed: _refreshDevicesBle,
                          ),
                          FlatButton(
                            child: Text("SEARCH AGAIN WITH BT CLASSIC"),
                            padding: EdgeInsets.symmetric(horizontal: 32.0),
                            textColor: Theme
                                .of(context)
                                .accentColor,
                            onPressed: _refreshDevicesClassic,
                          )
                        ],
                      ),
                    ),
                    //TODO: Hide Skip when we're on the home screen and the tabs are visible
                    FlatButton(
                      child: Text("SKIP"),
                      padding: EdgeInsets.symmetric(horizontal: 32.0),
                      onPressed: () => Navigator.pushNamed(context, '/home'),
                    )
                  ]))
        ]));
  }

  @override
  void dispose() {
    super.dispose();
    ScanCallbacks.setup(null);
    PairCallbacks.setup(null);
  }
}
