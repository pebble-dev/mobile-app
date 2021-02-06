import 'dart:math';

import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';

import './alerting_apps/sheet.dart';

class _App {
  final String name;
  final bool enabled;

  _App(this.name, this.enabled);
}

class AlertingApps extends HookWidget implements CobbleScreen {
  final apps = [
    _App('Twitter', true),
    _App('Youtube', false),
    _App('News', true),
    _App('Facebook', false),
    _App('App name', true),
  ];

  @override
  Widget build(BuildContext context) {
    final random = Random();
    final filter = useState(SheetOnChanged.initial);

    final sheet = CobbleSheet.useInline();

    return CobbleScaffold.tab(
      title: 'Choose which apps can alert',
      subtitle: '8 alerted, 5 muted today',
      actions: [
        Builder(
          builder: (context) => IconButton(
            padding: EdgeInsets.all(16),
            icon: Icon(RebbleIcons.search),
            onPressed: () {
              if (sheet.shown) {
                sheet.close();
              } else {
                sheet.show(
                  context: context,
                  builder: (context) {
                    return Sheet(
                      onClose: () {
                        filter.value = SheetOnChanged.initial;
                        sheet.close();
                      },
                      initialFilter: filter.value,
                      onChanged: (value) {
                        filter.value = value;
                      },
                    );
                  },
                );
              }
            },
          ),
        ),
      ],
      child: ListView(
        children: apps
            .where(
              (app) => app.name.toLowerCase().contains(
                    filter.value.query?.toLowerCase() ?? '',
                  ),
            )
            .map(
              (app) => CobbleTile.app(
                leading: Svg('images/temp_alerting_app.svg'),
                title: app.name,
                subtitle:
                    '${random.nextInt(8)} ${app.enabled ? 'alerted' : 'muted'} today',
                child: Switch(
                  value: app.enabled,
                  onChanged: (value) {},
                ),
              ),
            )
            .toList(),
      ),
    );
  }
}
