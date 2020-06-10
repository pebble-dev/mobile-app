import 'dart:convert';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/TabsPage.dart';
import 'package:fossil/icons/CompIcon.dart';
import 'package:fossil/icons/WatchIcon.dart';
import 'package:fossil/icons/pebble_watch_icons_icons.dart';
import 'package:fossil/icons/rebble_icons_stroke_icons.dart';
import 'package:fossil/util/PairedStorage.dart';
import '../theme.dart';
import 'MoreSetup.dart';
import 'package:fossil/util/interfacing/PebbleDevice.dart';
import 'package:shared_preferences/shared_preferences.dart';

class PairPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _PairPageState();
}
final MethodChannel _platform = MethodChannel('io.rebble.fossil/protocol');
final EventChannel _scanEvent = EventChannel('io.rebble.fossil/scanEvent');
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
          case "updatePairScanResults":
            setState(() {
              List a = jsonDecode(call.arguments);
              print(a[0]);
              if (a.length == 0) {
                _pebbles = [];
              }else {
                _pebbles = a.asMap().entries.map((e) => PebbleDevice(e.value['name'], int.parse((e.value['address'] as String).replaceAll(':', ''), radix: 16))).toList();
              }
            });
            break;
        }
      }();
    });

    _scanEvent.receiveBroadcastStream().listen((event) {
      Map<String, dynamic> ev = jsonDecode(event);
      switch (ev['event']) {
        case 'scanFinish':
          setState(() { _scanning = false; });
          break;
      }
    });

    _platform.invokeMethod("scanDevices");
  }

  void _refreshDevices() {
    if (!_scanning) {
      setState(() {
        _scanning = true;
        _platform.invokeMethod("scanDevices");
      });
    }
  }

  void _targetPebble(PebbleDevice dev) {
    _platform.invokeMethod("targetPebbleAddr", dev.address).then((value) {
      if (value as bool) setState(() {
        PairedStorage.register(dev).then((_) => PairedStorage.getDefault().then((def) {if (def == null) {PairedStorage.setDefault(dev.address);}})); // Register + set as default if no default set
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
          leading: IconButton(icon: Icon(RebbleIconsStroke.caret_left), onPressed: () => Navigator.maybePop(context),),
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
                        child: PebbleWatchIcon.Two(Colors.teal, Colors.lightGreenAccent, size: 75),
                        width: 75,
                        height: 75,
                        decoration: BoxDecoration(
                          borderRadius: BorderRadius.all(Radius.circular(5.0)),
                          color: CTheme.colorScheme.primary
                        ),
                      ),
                      SizedBox(width: 16),
                      Column(
                        children: <Widget>[
                          Text(e.name, style: TextStyle(fontSize: 19)),
                          SizedBox(height: 2),
                          Text(e.address.toRadixString(16).padLeft(6, '0')),
                        ],
                        crossAxisAlignment: CrossAxisAlignment.start,
                      ),
                    ]),
                    margin: EdgeInsets.all(8)),
                onTap: () {
                  _targetPebble(e);
                },
              ),
            ))
                .toList())
    );
  }
}