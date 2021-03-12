import 'package:cobble/localization/localization.dart';
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
    return CobbleScaffold.tab(
      title: tr.notifications.title,
      child: ListView(
        children: [
          CobbleTile.setting(
            leading: RebbleIcons.health_heart,
            title: tr.notifications.enabled,
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.locker,
            title: tr.notifications.chooseApps,
            navigateTo: AlertingApps(),
          ),
          CobbleDivider(),
          CobbleTile.withIconColor(
            color: context.scheme.muted,
            child: CobbleTile.sectionTitle(
              leading: RebbleIcons.settings,
              title: tr.notifications.silence.title,
              body: tr.notifications.silence.description,
            ),
          ),
          CobbleTile.setting(
            leading: CobbleTile.reservedIconSpace,
            title: tr.notifications.silence.notifications,
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.setting(
            leading: CobbleTile.reservedIconSpace,
            title: tr.notifications.silence.calls,
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
