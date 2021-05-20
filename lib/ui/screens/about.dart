import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/components/cobble_card.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/common/icons/comp_icon.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:package_info/package_info.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';
import 'dart:io' show Platform;
import 'package:intl/intl.dart';

class About extends StatefulWidget implements CobbleScreen {
  @override
  State<StatefulWidget> createState() => _AboutState();
}

class _AboutState extends State<About> {
  static const discordURL = "https://discord.gg/aRUAYFN";
  static const helpCenterURL = "https://help.rebble.io/";
  static const redditURL = "https://reddit.com/r/pebble";
  static const emailURL = "mailto:support@rebble.io";
  static const twitterURL = "https://twitter.com/pebble_dev";
  static const sourceURL = "https://github.com/pebble-dev/mobile-app";
  String _appVersion = '';
  String _appName = '';
  String os = toBeginningOfSentenceCase(Platform.operatingSystem) ?? '';

  @override
  void initState() {
    super.initState();
    // Apparently there is no pretty print platform name, so we just set the exceptions here
    if (Platform.isIOS)
      os = "iOS";
    else if (Platform.isMacOS)
      os = "macOS";
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
      title: " ", // We want an empty appbar
      bottom: PreferredSize(
        preferredSize: const Size.fromHeight(156.0),
        child: Container(
            height: 156.0,
            child: Padding(
              padding: EdgeInsets.fromLTRB(16.0, 14.0, 16.0, 9.0),
              child: Column(children: <Widget>[
                Row(children: <Widget>[
                  SizedBox(
                    width: 64.0,
                    height: 64.0,
                    child: Image(image: Svg('images/app_icon.svg')),
                  ),
                  SizedBox(width: 20.0),
                  Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Text(
                          _appName,
                          style: Theme.of(context).textTheme.headline5,
                        ),
                        SizedBox(height: 4.0),
                        Text(
                          tr.aboutPage.versionString(
                              version: _appVersion, platform: os),
                          style: Theme.of(context).textTheme.bodyText2,
                        ),
                      ]),
                ]),
                SizedBox(height: 14.0),
                Row(children: <Widget>[
                  Expanded(
                      child: SizedBox(
                          height: 40.0,
                          child: RaisedButton(
                            child: Row(children: <Widget>[
                              Icon(RebbleIcons.rocket, size: 25),
                              SizedBox(width: 8),
                              Text(tr.aboutPage.sourceCode.toUpperCase()),
                            ]),
                            onPressed: () => {launchURL(sourceURL)},
                          ))),
                  SizedBox(width: 16),
                  Expanded(
                      child: SizedBox(
                          height: 40.0,
                          child: CobbleButton(
                            outlined: false,
                            label: tr.aboutPage.licenses,
                            onPressed: () => {showLicensePage(context: context)},
                          ))),
                ])
              ]),
            )),
      ),
      child: SingleChildScrollView(
        child: Column(
          children: <Widget>[
            Card(
              margin: EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Padding(
                    padding: EdgeInsets.fromLTRB(16.0, 16.0, 16.0, 8.0),
                    child: Text(
                      tr.aboutPage.support,
                      style: Theme.of(context)
                          .textTheme
                          .headline6
                          ?.copyWith(fontSize: 18),
                    ),
                  ),
                  CobbleTile.action(
                    leading: Container(
                      child: Center(
                          child: CompIcon(RebbleIcons.unpair_from_watch,
                              RebbleIcons.unpair_from_watch_background)),
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                          color: context.scheme!.primary,
                          shape: BoxShape.circle),
                    ),
                    trailing: RebbleIcons.caret_right,
                    title: tr.aboutPage.helpCenter,
                    subtitle: tr.aboutPage.helpCenterSubtitle,
                    onTap: () => {launchURL(helpCenterURL)},
                  ),
                  CobbleTile.action(
                    leading: Container(
                      child: Center(
                          child: CompIcon(RebbleIcons.notification_email,
                              RebbleIcons.notification_email_background)),
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                          color: Color.fromARGB(255, 33, 156, 136),
                          shape: BoxShape.circle),
                    ),
                    trailing: RebbleIcons.caret_right,
                    title: tr.aboutPage.emailUs,
                    subtitle: tr.aboutPage.emailUsSubtitle,
                    onTap: () => {launchURL(emailURL)},
                  ),
                  CobbleTile.action(
                    leading: Container(
                      child: Center(
                          child: CompIcon(RebbleIcons.discord,
                              RebbleIcons.discord_background)),
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                          color: Color.fromARGB(255, 128, 150, 227),
                          shape: BoxShape.circle),
                    ),
                    trailing: RebbleIcons.caret_right,
                    title: tr.aboutPage.discordServer,
                    subtitle: tr.aboutPage.discordServerSubtitle,
                    onTap: () => {launchURL(discordURL)},
                  ),
                  SizedBox(height: 8.0),
                ],
              ),
            ),
            Card(
              margin: EdgeInsets.only(bottom: 16.0, left: 16.0, right: 16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  Padding(
                    padding: EdgeInsets.fromLTRB(16.0, 14.0, 16.0, 9.0),
                    child: Text(
                      tr.aboutPage.community,
                      style: Theme.of(context)
                          .textTheme
                          .headline6
                          ?.copyWith(fontSize: 18),
                    ),
                  ),
                  CobbleTile.action(
                    leading: Container(
                      child: Center(
                          child: CompIcon(RebbleIcons.reddit,
                              RebbleIcons.reddit_background)),
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                          color: Color.fromARGB(255, 255, 120, 70),
                          shape: BoxShape.circle),
                    ),
                    trailing: RebbleIcons.caret_right,
                    title: tr.aboutPage.reddit,
                    subtitle: tr.aboutPage.redditSubtitle,
                    onTap: () => {launchURL(redditURL)},
                  ),
                  CobbleTile.action(
                    leading: Container(
                      child: Center(
                          child: CompIcon(RebbleIcons.discord,
                              RebbleIcons.discord_background)),
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                          color: Color.fromARGB(255, 128, 150, 227),
                          shape: BoxShape.circle),
                    ),
                    trailing: RebbleIcons.caret_right,
                    title: tr.aboutPage.discord,
                    subtitle: tr.aboutPage.discordSubtitle,
                    onTap: () => {launchURL(discordURL)},
                  ),
                  CobbleTile.action(
                    leading: Container(
                      child: Center(
                          child: CompIcon(RebbleIcons.twitter,
                              RebbleIcons.twitter_background)),
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                          color: Color.fromARGB(255, 56, 175, 248),
                          shape: BoxShape.circle),
                    ),
                    trailing: RebbleIcons.caret_right,
                    title: tr.aboutPage.twitter,
                    subtitle: tr.aboutPage.twitterSubtitle,
                    onTap: () => {launchURL(twitterURL)},
                  ),
                  SizedBox(height: 8.0),
                ],
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
