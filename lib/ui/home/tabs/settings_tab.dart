import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:flutter/material.dart';
import 'package:package_info/package_info.dart';
import 'package:url_launcher/url_launcher.dart';

class SettingsTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _SettingsTabState();
}

class _SettingsTabState extends State<SettingsTab> {
  static const communityURL = "http://discord.gg/aRUAYFN";
  static const supportURL = "https://rebble.io/faq/";
  String _appVersion = '';
  String _appName = '';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  initPlatformState() async {
    PackageInfo packageInfo = await PackageInfo.fromPlatform();
    String appName = packageInfo.appName;
    String appVersion = packageInfo.version;

    if (!mounted) return;

    setState(() {
      _appName = appName;
      _appVersion = appVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      title: 'Settings',
      child: SingleChildScrollView(
        child: Column(
          children: <Widget>[
            Card(
              margin: EdgeInsets.all(16.0),
              child: Padding(
                padding: EdgeInsets.symmetric(horizontal: 16.0, vertical: 24.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: <Widget>[
                    Row(),
                    Text(
                      "About " + _appName + " v" + _appVersion,
                      style: Theme.of(context).textTheme.headline5,
                    ),
                    SizedBox(height: 8.0),
                    FlatButton.icon(
                        label: Text("Community"),
                        icon: Icon(RebbleIconsStroke.settings, size: 25.0),
                        textColor: Theme.of(context).accentColor,
                        onPressed: () => {launchURL(communityURL)}),
                    FlatButton.icon(
                        label: Text("Support"),
                        icon: Icon(
                          RebbleIconsStroke.developer_connection_console,
                          size: 25.0,
                        ),
                        textColor: Theme.of(context).accentColor,
                        onPressed: () => {launchURL(supportURL)}),
                  ],
                ),
              ),
            )
          ],
        ),
      ),
    );
  }

  launchURL(String url) async {
    if (await canLaunch(url)) {
      await launch(url);
    } else {
      throw 'Could not launch $url';
    }
  }

  @override
  void dispose() {
    super.dispose();
  }
}
