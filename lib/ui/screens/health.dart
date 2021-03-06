import 'package:cobble/ui/common/components/cobble_card.dart';
import 'package:cobble/ui/common/components/cobble_dialog.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';

class Health extends StatelessWidget implements CobbleScreen {
  @override
  Widget build(BuildContext context) {
    return CobbleScaffold.tab(
      title: 'Health',
      child: ListView(
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
          CobbleDivider(),
          CobbleTile.setting(
            leading: RebbleIcons.health_steps,
            title: 'Activity summary',
            subtitle: 'Your watch will notify you at the end of the day '
                'with a summary of how active you were',
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.setting(
            leading: RebbleIcons.health_sleep,
            title: 'Sleep summary',
            subtitle: 'Your watch will notify you when you wake up with a '
                'summary of how well you slept',
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.title(title: 'Sync'),
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
          ),
          CobbleTile.accordion(
            title: 'More options',
            children: [
              CobbleTile.action(
                leading: RebbleIcons.floppy_disk_health_database,
                trailing: RebbleIcons.caret_right,
                title: 'Manage health database',
                onTap: manageHealthDatabase(context),
              ),
              CobbleTile.action(
                leading: RebbleIcons.delete_trash,
                title: 'Delete all health data',
                intent: context.scheme!.destructive,
                onTap: deleteHealthDatabase(context),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> Function() deleteHealthDatabase(BuildContext context) =>
      () async {
        await showCobbleDialog(
          context: context,
          title: 'Permanently delete ALL health data?',
          content: 'This cannot be undone!',
          positive: 'Delete',
          negative: 'Cancel',
          intent: context.scheme!.destructive,
        );
      };

  void Function() manageHealthDatabase(BuildContext context) => () {
        CobbleSheet.showModal(
          context: context,
          builder: (context) => Column(
            children: [
              CobbleTile.action(
                leading: RebbleIcons.floppy_disk,
                trailing: RebbleIcons.caret_right,
                title: 'Create backup of health data',
                onTap: () {},
              ),
              CobbleTile.action(
                leading: RebbleIcons.floppy_disk,
                trailing: RebbleIcons.caret_right,
                title: 'Restore health data from backup',
                onTap: () {},
              ),
              CobbleDivider(),
              CobbleTile.action(
                leading: RebbleIcons.delete_trash,
                trailing: RebbleIcons.caret_right,
                title: 'Permanently delete all health data',
                intent: context.scheme!.destructive,
                onTap: deleteHealthDatabase(context),
              ),
            ],
          ),
        );
      };
}
