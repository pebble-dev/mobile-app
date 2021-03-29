// @dart=2.9

import 'package:cobble/ui/common/components/cobble_card.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';
import 'package:golden_toolkit/golden_toolkit.dart';

import '../flutter_test_config.dart';

Widget cards() => Column(
      children: [
        CobbleCard(
          title: 'Rebble account',
          subtitle: 'support@rebble.io',
          leading: Svg('images/app_icon.svg'),
          child: Column(
            children: [
              ListTile(
                leading: Icon(
                  RebbleIcons.dictation_microphone,
                  size: 24,
                ),
                title: Text('Voice and weather subscription'),
                subtitle: Text('Next charge 6/9/20'),
              ),
              ListTile(
                leading: Icon(
                  RebbleIcons.timeline_pin,
                  size: 24,
                ),
                title: Text('Timeline sync'),
                subtitle: Text('Every 30 minutes'),
              ),
            ],
          ),
          actions: [
            CobbleCardAction(
              onPressed: () {},
              label: 'Sign out',
            ),
            CobbleCardAction(
              onPressed: () {},
              label: 'Manage account',
            ),
          ],
        ),
        Builder(
          builder: (context) => CobbleCard(
            title: 'Untrusted boot URL',
            leading: RebbleIcons.notification,
            intent: context.scheme.danger,
            actions: [
              CobbleCardAction(
                onPressed: () {},
                label: 'Reset',
              ),
              CobbleCardAction(
                onPressed: () {},
                label: 'Copy url',
              ),
            ],
          ),
        ),
        CobbleCard.inList(
          leading: AssetImage('images/health_icon.png'),
          title: 'Signed in as',
          subtitle: 'support@rebble.io',
          actions: [
            CobbleCardAction(
              label: 'Sign out',
              onPressed: () {},
            ),
            CobbleCardAction(
              label: 'Switch account',
              onPressed: () {},
            ),
          ],
        )
      ],
    );

void main() {
  testUi('Cobble cards', cards(), [Device.iphone11]);
}
