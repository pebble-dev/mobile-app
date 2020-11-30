import 'package:flutter/material.dart';
import 'package:fossil/ui/common/icons/fonts/RebbleIconsStroke.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:package_info/package_info.dart';

class AboutTab extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _AboutTabState();

}

class _AboutTabState extends State<AboutTab> {

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

    return Scaffold(
      appBar: AppBar(
        title: Text("About"),
      ),
      body: SingleChildScrollView(
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
                      "About " + _appName + " v" +_appVersion,
                      style: Theme.of(context).textTheme.headline5,
                    ),
                    SizedBox(height: 8.0),
                    FlatButton.icon(
                        label: Text("Community"),
                        icon: Icon(
                            RebbleIconsStroke.settings, size: 25.0),
                        textColor: Theme.of(context).accentColor,
                        onPressed: () => {launchURL(communityURL)}),
                    FlatButton.icon(
                        label: Text("Support"),
                        icon: Icon(RebbleIconsStroke.developer_connection_console, size: 25.0),
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
