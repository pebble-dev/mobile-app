import 'dart:convert';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fossil/TabsPage.dart';
import 'package:fossil/icons/CompIcon.dart';
import 'package:fossil/icons/WatchIcon.dart';
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
          backgroundColor: CTheme.colorScheme.surface,
          title: Text("Pair a watch"),
          leading: BackButton(),
        ),
        body: ListView(
            children: <Widget>[
              Offstage(
                  offstage: !_scanning,
                  child: Padding(
                      padding: EdgeInsets.all(16.0),
                      child: Center(child: CircularProgressIndicator())
                  )
              ),
              Column(children: _pebbles
                  .map((e) => InkWell(
                child: Container(
                    child: Row(children: <Widget>[
                      Container(
                        child: Center(child: PebbleWatchIcon.Two(Colors.teal, Colors.lightGreenAccent)),
                        width: 56,
                        height: 56,
                        decoration: BoxDecoration(
                            color: Theme.of(context).dividerColor,
                            shape: BoxShape.circle
                        ),
                      ),
                      SizedBox(width: 16),
                      Column(
                        children: <Widget>[
                          Text(e.name, style: TextStyle(fontSize: 16)),
                          SizedBox(height: 4),
                          Text(e.address.toRadixString(16).padLeft(6, '0')),
                        ],
                        crossAxisAlignment: CrossAxisAlignment.start,
                      ),
                      Expanded(child: Container(width: 0.0, height: 0.0)),
                      Icon(RebbleIconsStroke.caret_right, color: CTheme.colorScheme.secondary),
                    ]),
                    margin: EdgeInsets.all(16)),
                onTap: () {
                  _targetPebble(e);
                },
              ))
                  .toList()
              ),
              Offstage(
                  offstage: _scanning,
                  child: Padding(
                      padding: EdgeInsets.all(16.0),
                      child: FlatButton(
                        child: Text("SEARCH AGAIN"),
                        textColor: CTheme.colorScheme.secondary,
                        onPressed: _refreshDevices,
                      )
                  )
              ),
              FlatButton(child: Text("SKIP"), onPressed: ()=>{} ,)
            ])
    );
  }
}