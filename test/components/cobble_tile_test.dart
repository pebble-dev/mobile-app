import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
import 'package:cobble/ui/screens/health.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

import 'flutter_test_config.dart';

Widget tiles() => Column(
      children: [
        CobbleTile.title(
          title: 'Rebble Health',
          body: 'Supported watches can keep track of your fitness data for '
              'you, including steps, sleep, and heart rate',
        ),
        CobbleTile.setting(
          leading: RebbleIconsStroke.health,
          title: 'Track my health data',
          child: Switch(
            value: true,
            onChanged: (bool value) {},
          ),
        ),
        CobbleTile.action(
          leading: RebbleIconsStroke.health,
          trailing: RebbleIconsStroke.about_app,
          title: 'Manage health database',
          onTap: () {},
          intent: Colors.green,
        ),
        CobbleTile.action(
          leading: RebbleIconsStroke.health,
          title: 'Manage health database...',
          subtitle: '...but gently',
          onTap: () {},
        ),
        CobbleTile.navigation(
          leading: RebbleIconsStroke.health,
          trailing: RebbleIconsStroke.caret_right,
          title: 'Manage health database',
          navigateTo: Health(),
        ),
        CobbleTile.navigation(
          leading: RebbleIconsStroke.health,
          title: 'Manage health database...',
          subtitle: '...but gently',
          navigateTo: Health(),
          intent: Colors.green,
        ),
        Builder(
          builder: (context) => CobbleTile.action(
            leading: RebbleIconsStroke.menu_vertical,
            title: 'Delete all health data',
            intent: context.scheme.destructive,
            onTap: () async {},
          ),
        ),
        CobbleDivider(),
        CobbleTile.info(
          leading: RebbleIconsStroke.dictation_microphone,
          title: 'Voice and weather subscription',
          subtitle: 'Not subscribed',
        ),
      ],
    );

void main() {
  testUi('Cobble tiles', tiles());
}
