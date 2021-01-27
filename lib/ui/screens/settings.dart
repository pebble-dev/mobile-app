import 'package:cobble/ui/common/components/cobble_card.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/health.dart';
import 'package:cobble/ui/screens/notifications.dart';
import 'package:cobble/ui/screens/placeholder_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';

class Settings extends StatelessWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      title: 'Settings',
      child: ListView(
        children: [
          CobbleCard.inList(
            leading: Svg('images/app_icon.svg'),
            title: 'Rebble account',
            subtitle: 'support@rebble.io',
            child: Column(
              children: [
                CobbleTile.info(
                  leading: RebbleIcons.dictation_microphone,
                  title: 'Voice and weather subscription',
                  subtitle: 'Not subscribed',
                ),
                CobbleTile.info(
                  leading: RebbleIcons.timeline_pin,
                  title: 'Timeline sync',
                  subtitle: 'Every 2 hours',
                ),
              ],
            ),
            actions: [
              CobbleCardAction(
                label: 'Sign out',
                onPressed: () {},
              ),
              CobbleCardAction(
                label: 'Manage account',
                onPressed: () {},
              ),
            ],
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.notification,
            title: 'Notifications and muting',
            navigateTo: Notifications(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.health_heart,
            title: 'Health',
            navigateTo: Health(),
          ),
          CobbleTile.withIconColor(
            color: context.scheme.danger,
            child: CobbleTile.navigation(
              leading: RebbleIcons.unknown_app,
              title: 'Calendar',
              navigateTo: PlaceholderScreen(),
            ),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.sms_messages,
            title: 'Messages and canned replies',
            navigateTo: PlaceholderScreen(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.system_language,
            title: 'Language and voice',
            navigateTo: PlaceholderScreen(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.analytics,
            title: 'Analytics',
            navigateTo: PlaceholderScreen(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.about_app,
            title: 'About app',
            navigateTo: PlaceholderScreen(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.share,
            title: 'Community and support',
            navigateTo: PlaceholderScreen(),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.developer_settings,
            title: 'Developer options',
            navigateTo: PlaceholderScreen(),
          ),
        ],
      ),
    );
  }
}
