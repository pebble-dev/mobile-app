import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons_stroke.dart';
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
          icon: RebbleIconsStroke.notifications_megaphone,
          label: 'Button',
        ),
        CobbleButton(
          onPressed: () {},
          icon: RebbleIconsStroke.notifications_megaphone,
        ),
        CobbleButton(
          onPressed: null,
          label: 'Button',
        ),
        CobbleButton(
          onPressed: null,
          icon: RebbleIconsStroke.notifications_megaphone,
          label: 'Button',
        ),
        CobbleButton(
          onPressed: null,
          icon: RebbleIconsStroke.notifications_megaphone,
        ),
        CobbleButton(
          outlined: false,
          onPressed: () {},
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: () {},
          icon: RebbleIconsStroke.notifications_megaphone,
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: () {},
          icon: RebbleIconsStroke.notifications_megaphone,
        ),
        CobbleButton(
          outlined: false,
          onPressed: null,
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: null,
          icon: RebbleIconsStroke.notifications_megaphone,
          label: 'Button',
        ),
        CobbleButton(
          outlined: false,
          onPressed: null,
          icon: RebbleIconsStroke.notifications_megaphone,
        ),
      ],
    );

void main() {
  testUi('Cobble button', buttons());
}
