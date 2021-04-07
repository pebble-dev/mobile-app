import 'package:cobble/localization/localization.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/alerting_apps.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class Notifications extends HookWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    final preferences = useProvider(preferencesProvider);
    final notifcationsEnabled = useProvider(notificationToggleProvider);
    final phoneNotificationsMuteEnabled =
        useProvider(phoneNotificationsMuteProvider);
    final phoneCallsMuteEnabled = useProvider(phoneCallsMuteProvider);

    return CobbleScaffold.tab(
      title: tr.notifications.title,
      child: ListView(
        children: [
          CobbleTile.setting(
            leading: RebbleIcons.notification,
            title: tr.notifications.enabled,
            child: Switch(
              value: notifcationsEnabled.data?.value ?? true,
              onChanged: (bool value) async {
                await preferences.data?.value.setNotificationsEnabled(value);
              },
            ),
          ),
          CobbleTile.navigation(
            leading: RebbleIcons.locker,
            title: tr.notifications.chooseApps,
            navigateTo: AlertingApps(),
          ),
          CobbleDivider(),
          CobbleTile.withIconColor(
            color: context.scheme!.muted,
            child: CobbleTile.sectionTitle(
              leading: RebbleIcons.settings,
              title: tr.notifications.silence.title,
              body: tr.notifications.silence.description,
            ),
          ),
          // TODO Separate call and notification mute is only possible on
          //  Android 7 (SDK 24) and newer. On older releases,
          //  we should only display one switch that controls both.
          CobbleTile.setting(
            leading: CobbleTile.reservedIconSpace,
            title: tr.notifications.silence.notifications,
            child: Switch(
              value: phoneNotificationsMuteEnabled.data?.value ?? false,
              onChanged: (bool value) async {
                await preferences.data?.value.setPhoneNotificationMute(value);
              },
            ),
          ),
          CobbleTile.setting(
            leading: CobbleTile.reservedIconSpace,
            title: tr.notifications.silence.calls,
            child: Switch(
              value: phoneCallsMuteEnabled.data?.value ?? false,
              onChanged: (bool value) async {
                await preferences.data?.value.setPhoneCallsMute(value);
              },
            ),
          ),
        ],
      ),
    );
  }
}
