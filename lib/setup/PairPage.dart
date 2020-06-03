import 'dart:convert';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/TabsPage.dart';
import 'package:fossil/util/PairedStorage.dart';
import 'MoreSetup.dart';
import 'package:fossil/util/interfacing/PebbleDevice.dart';
import 'package:fossil/icons/rebble_icons_stroke_only_icons.dart';
import 'package:shared_preferences/shared_preferences.dart';

class _PairDevice {
  final int id;
  final PebbleDevice device;
  _PairDevice(this.id, this.device);

  Map<String,dynamic> toJson() => {'id':id, 'device':device};
}

class PairPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _PairPageState();
}

class _PairPageState extends State<PairPage> {
  static final MethodChannel platform = MethodChannel('io.rebble.fossil/protocol');

  static final EventChannel scanEvent = EventChannel('io.rebble.fossil/scanEvent');

  List<_PairDevice> _pebbles = [];
  bool _scanning = true;

  @override
  void initState() {
    super.initState();
    platform.setMethodCallHandler((call) {
      return () async {
        print(call.method);
        switch (call.method) {
          case "updatePairScanResults":
            setState(() {
              List a = jsonDecode(call.arguments);
              print(a[0]);
              //print(int.parse(a[0].value['address'].toString().replaceAll(':', ''), radix: 16));
              if (a.length == 0) {
                _pebbles = [];
              }else {
                _pebbles = a.asMap().entries.map((e) => _PairDevice(e.key, PebbleDevice(e.value['name'], int.parse(e.value['address'].toString().replaceAll(':', ''), radix: 16)))).toList();
              }
            });
            break;
        }
      }();
    });

    scanEvent.receiveBroadcastStream().listen((event) {
      Map<String, dynamic> ev = jsonDecode(event);
      switch (ev['event']) {
        case 'scanFinish':
          setState(() { _scanning = false; });
          break;
      }
    });

    platform.invokeMethod("scanDevices");
  }

  void _refreshDevices() {
    if (!_scanning) {
      setState(() {
        _scanning = true;
        platform.invokeMethod("scanDevices");
      });
    }
  }

  void _targetPebble(int id) {
    platform.invokeMethod("targetPebble", id).then((value) {
      if (value as bool) setState(() {
        PairedStorage.register(_pebbles[id].device).then((_) => PairedStorage.getDefault().then((def) {if (def == null) {PairedStorage.setDefault(_pebbles[id].device.address);}})); // Register + set as default if no default set
        SharedPreferences.getInstance().then((value) {
          if (!value.containsKey("firstRun")) {
            Navigator.push(context, MaterialPageRoute(builder: (context) => MoreSetup()));
          }else {
            Navigator.pushReplacement(context, MaterialPageRoute(builder: (context) => TabsPage()));
          }
        });
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text("Pair"),
          leading: IconButton(icon: Icon(RebbleIconsStrokeOnly.caret_left), onPressed: () => Navigator.maybePop(context),),
          actions: <Widget>[
            _scanning
                ? Container(
                width: 55,
                height: 55,
                padding: EdgeInsets.all(6),
                child: CircularProgressIndicator())
                : IconButton(
              icon: Icon(Icons.refresh),
              onPressed: _refreshDevices,
            ),
          ],
        ),
        body: ListView(
            children: _pebbles
                .map((e) => Card(
              child: InkWell(
                child: Container(
                    child: Row(children: <Widget>[
                      Container(
                        child: Image.asset("images/rebble.png"),
                        color: Color.fromRGBO(0, 0, 0, 0.2),
                        width: 75,
                        height: 75,
                        padding: EdgeInsets.all(8),
                      ),
                      SizedBox(width: 16),
                      Column(
                        children: <Widget>[
                          Text(e.device.name, style: TextStyle(fontSize: 19)),
                          SizedBox(height: 2),
                          Text(e.device.address.toRadixString(16).padLeft(6, '0')),
                        ],
                        crossAxisAlignment: CrossAxisAlignment.start,
                      ),
                    ]),
                    margin: EdgeInsets.all(8)),
                onTap: () {
                  _targetPebble(e.id);
                },
              ),
            ))
                .toList())
    );
  }
}