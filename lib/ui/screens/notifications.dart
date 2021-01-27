import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/alerting_apps.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

class Notifications extends StatelessWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold(
      title: 'Notifications and muting',
      child: ListView(
        children: [
          CobbleTile.setting(
            leading: RebbleIcons.health_heart,
            title: 'Send notifications to my watch',
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.locker,
            title: 'Choose which apps can alert your watch',
            navigateTo: AlertingApps(),
          ),
          CobbleDivider(),
          CobbleTile.withIconColor(
            color: context.scheme.muted,
            child: CobbleTile.sectionTitle(
              leading: RebbleIcons.settings,
              title: 'Silence notifications',
              body: 'While your watch is connected, it can silence incoming '
                  'calls and notifications on your phone, so that they only '
                  'vibrate on your wrist.',
            ),
          ),
          CobbleTile.setting(
            leading: CobbleTile.reservedIconSpace,
            title: 'Silence notifications',
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.setting(
            leading: CobbleTile.reservedIconSpace,
            title: 'Silence incoming calls',
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
        ],
      ),
    );
  }
}
