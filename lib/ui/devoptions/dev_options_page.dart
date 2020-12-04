import 'package:flutter/material.dart';
import 'package:cobble/infrastructure/datasources/dev_connection.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
import 'package:shared_preferences/shared_preferences.dart';

class DevOptionsPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => new _DevOptionsPageState();
}

class _DevOptionsPageState extends State<DevOptionsPage> {
  TextEditingController _bootUrlC = TextEditingController();
  bool _overrideS2Config = false;
  bool _devConnection = false;

  bool _isDevConnected = false;
  bool _isDevRunning = false;
  TextEditingController _bootOverrideC = TextEditingController();
  final DevConnection _devConControl = new DevConnection();

  @override
  void initState() {
    super.initState();
    SharedPreferences.getInstance().then((prefs) => {
          if (prefs.containsKey("boot"))
            {_bootUrlC.text = prefs.getString("boot")},
          if (prefs.containsKey("bootOverride"))
            {_bootOverrideC.text = prefs.getString("bootOverride")}
        });
    _devConControl.setCB((bool isConnected) {
      setState(() {
        _isDevConnected = isConnected;
      });
    }, (bool isRunning) {
      setState(() {
        _isDevRunning = true;
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_devConnection) {
      _devConControl.start();
    } else {
      if (_devConControl.isConnected) {
        _devConControl.close();
      }
    }
    return Scaffold(
      appBar: AppBar(
        title: Text("Developer Options"),
        leading: IconButton(
          icon: Icon(RebbleIconsStroke.caret_left),
          onPressed: () {
            Navigator.maybePop(context);
          },
        ),
      ),
      body: ListView(
          children: ListTile.divideTiles(
        context: context,
        tiles: <Widget>[
          ListTile(
              contentPadding:
                  EdgeInsets.symmetric(vertical: 10, horizontal: 15),
              title: Text(
                "Apps",
                style: TextStyle(fontSize: 25),
              )),
          SwitchListTile(
            value: _devConnection,
            title: Text("Developer Connection"),
            subtitle: Text("Extremely insecure, resets outside of page" +
                (_isDevRunning
                    ? "\nRunning..." + (_isDevConnected ? " **CONNECTED**" : "")
                    : "")),
            isThreeLine: _isDevConnected,
            onChanged: (checked) {
              setState(() => _devConnection = checked);
            },
          ),
          ListTile(
              contentPadding:
                  EdgeInsets.symmetric(vertical: 10, horizontal: 15),
              title: Text(
                "Boot",
                style: TextStyle(fontSize: 25),
              )),
          ListTile(
              contentPadding:
                  EdgeInsets.symmetric(vertical: 10, horizontal: 15),
              title: Text("URL"),
              subtitle: TextField(
                  controller: _bootUrlC,
                  onChanged: (value) => setState(() {
                        SharedPreferences.getInstance()
                            .then((_) => _.setString("boot", value));
                      }))),
          SwitchListTile(
              value: _overrideS2Config,
              title: Text("Override stage2 config"),
              subtitle: Text("If enabled, will ignore boot URL"),
              onChanged: (checked) {
                setState(() => _overrideS2Config = checked);
                SharedPreferences.getInstance()
                    .then((_) => _.setBool("overrideBoot", checked));
              }),
          ListTile(
            contentPadding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
            title: Text("Stage2 Override"),
            subtitle: Column(
              children: <Widget>[
                TextField(
                  controller: _bootOverrideC,
                  maxLines: 8,
                  minLines: 4,
                ),
                Container(
                    alignment: Alignment.centerRight,
                    child: RaisedButton(
                        child: Text("Save"),
                        onPressed: () => SharedPreferences.getInstance().then(
                            (_) => _.setString(
                                "overrideBootValue", _bootOverrideC.text))))
              ],
            ),
          )
        ],
      ).toList()),
    );
  }
}
