import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../common/icons/fonts/rebble_icons.dart';

class AboutTab extends StatefulWidget implements CobbleScreen {
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
    return CobbleScaffold.tab(
      title: tr.aboutPage.title,
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
                      tr.aboutPage.about(
                        name: _appName,
                        version: _appVersion,
                      ),
                      style: Theme.of(context).textTheme.headline5,
                    ),
                    SizedBox(height: 8.0),
                    CobbleButton(
                        outlined: false,
                        label: tr.aboutPage.community,
                        icon: RebbleIcons.share,
                        color: Theme.of(context).accentColor,
                        onPressed: () => {launchURL(communityURL)}),
                    CobbleButton(
                        outlined: false,
                        label: tr.aboutPage.support,
                        icon: RebbleIcons.developer_connection_console,
                        color: Theme.of(context).accentColor,
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
