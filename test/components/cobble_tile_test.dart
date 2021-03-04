import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
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
          leading: RebbleIcons.health_heart,
          title: 'Track my health data',
          child: Switch(
            value: true,
            onChanged: (bool value) {},
          ),
        ),
        CobbleTile.action(
          leading: RebbleIcons.health_heart,
          trailing: RebbleIcons.about_app,
          title: 'Manage health database',
          onTap: () {},
          intent: Colors.green,
        ),
        CobbleTile.action(
          leading: RebbleIcons.health_heart,
          title: 'Manage health database...',
          subtitle: '...but gently',
          onTap: () {},
        ),
        CobbleTile.navigation(
          leading: RebbleIcons.health_heart,
          trailing: RebbleIcons.caret_right,
          title: 'Manage health database',
          navigateTo: EmptyScreen(),
        ),
        CobbleTile.navigation(
          leading: RebbleIcons.health_heart,
          title: 'Manage health database...',
          subtitle: '...but gently',
          navigateTo: EmptyScreen(),
          intent: Colors.green,
        ),
        Builder(
          builder: (context) => CobbleTile.action(
            leading: RebbleIcons.menu_vertical,
            title: 'Delete all health data',
            intent: context.scheme.destructive,
            onTap: () async {},
          ),
        ),
        CobbleDivider(),
        CobbleTile.info(
          leading: RebbleIcons.dictation_microphone,
          title: 'Voice and weather subscription',
          subtitle: 'Not subscribed',
        ),
      ],
    );

void main() {
  testUi('Cobble tiles', tiles());
}

class EmptyScreen extends StatelessWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    throw Container();
  }
}
