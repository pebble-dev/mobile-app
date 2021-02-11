import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:flutter/cupertino.dart';

import 'flutter_test_config.dart';

Widget buttons() => Column(
      children: [
        CobbleButton(
          onPressed: () {},
          label: 'Button',
        ),
        CobbleButton(
          onPressed: () {},
          icon: RebbleIcons.notification,
          label: 'Button',
        ),
        CobbleButton(
          onPressed: () {},
          icon: RebbleIcons.notification,
        ),
        CobbleButton(
          onPressed: null,
          label: 'Button',
        ),
        CobbleButton(
          onPressed: null,
          icon: RebbleIcons.notification,
          label: 'Button',
        ),
        CobbleButton(
          onPressed: null,
          icon: RebbleIcons.notification,
        ),
        CobbleButton(
          outlined: false,
          onPressed: () {},
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: () {},
          icon: RebbleIcons.notification,
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: () {},
          icon: RebbleIcons.notification,
        ),
        CobbleButton(
          outlined: false,
          onPressed: null,
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: null,
          icon: RebbleIcons.notification,
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: null,
          icon: RebbleIcons.notification,
        ),
      ],
    );

void main() {
  testUi('Cobble button', buttons());
}
