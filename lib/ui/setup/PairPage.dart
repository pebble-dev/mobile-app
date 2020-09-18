import 'dart:convert';
import 'dart:developer';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/domain/entities/PebbleDevice.dart';
import 'package:fossil/infrastructure/datasources/PairedStorage.dart';
import 'package:fossil/ui/common/icons/fonts/RebbleIconsStroke.dart';
import 'package:fossil/ui/common/icons/WatchIcon.dart';
import 'package:shared_preferences/shared_preferences.dart';

class PairPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _PairPageState();
}

final MethodChannel _platform = MethodChannel('io.rebble.fossil/protocol');

class _PairPageState extends State<PairPage> {
  List<PebbleDevice> _pebbles = [];
  bool _scanning = true;

  @override
  void initState() {
    super.initState();
    _platform.setMethodCallHandler((call) {
      return () async {
        print(call.method);
        switch (call.method) {
          case "addPairScanResult":
            setState(() {
              dynamic a = jsonDecode(call.arguments as String);
              log(call.arguments as String);
              _pebbles.add(PebbleDevice(
                  a['name'],
                  int.parse((a['address'] as String).replaceAll(':', ''), radix: 16),
                  a['version'],
                  a['serialNumber'],
                  a['color'],
                  a['runningPRF'],
                  a['firstUse']));
            });
            break;
          case "finishPairScan":
            setState(() {
              _scanning = false;
            });
            break;
        }
      }();
    });

    _platform.invokeMethod("scanDevices");
  }

  void _refreshDevices() {
    if (!_scanning) {
      setState(() {
        _scanning = true;
        _pebbles = [];
        _platform.invokeMethod("scanDevices");
      });
    }
  }

  void _targetPebble(PebbleDevice dev) {
    _platform.invokeMethod("targetPebbleAddr", dev.address).then((value) {
      if (value as bool)
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
                                    child: PebbleWatchIcon(PebbleWatchModel
                                        .time_round_rose_gold_14)),
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
                                      .padLeft(6, '0').toUpperCase()),
                                  Wrap(
                                    spacing: 4,
                                    children: [
                                      Offstage(
                                        offstage: !e.runningPRF || e.firstUse,
                                        child: Chip(
                                          backgroundColor: Colors.deepOrange,
                                          label: Text("Recovery"),
                                        )
                                      ),
                                      Offstage(
                                          offstage: !e.firstUse,
                                          child: Chip(
                                            backgroundColor: Color(0xffd4af37),
                                            label: Text("New!"),
                                          )
                                      ),
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
                        child: FlatButton(
                          child: Text("SEARCH AGAIN"),
                          padding: EdgeInsets.symmetric(horizontal: 32.0),
                          textColor: Theme.of(context).accentColor,
                          onPressed: _refreshDevices,
                        )),
                    FlatButton(
                      child: Text("SKIP"),
                      padding: EdgeInsets.symmetric(horizontal: 32.0),
                      onPressed: () => {},
                    )
                  ]))
        ]));
  }
}
