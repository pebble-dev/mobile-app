import 'package:cobble/localization/localization.dart';
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
      title: tr.health.title,
      child: ListView(
        children: [
          CobbleTile.title(
            title: tr.health.subtitle,
            body: tr.health.description,
          ),
          CobbleTile.setting(
            leading: RebbleIcons.health_heart,
            title: tr.health.trackMe,
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleDivider(),
          CobbleTile.setting(
            leading: RebbleIcons.health_steps,
            title: tr.health.activity.title,
            subtitle: tr.health.activity.subtitle,
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.setting(
            leading: RebbleIcons.health_sleep,
            title: tr.health.sleep.title,
            subtitle: tr.health.sleep.subtitle,
            child: Switch(
              value: true,
              onChanged: (bool value) {},
            ),
          ),
          CobbleTile.title(title: tr.health.sync.title),
          CobbleCard.inList(
            leading: AssetImage('images/health_icon.png'),
            title: tr.health.sync.subtitle,
            subtitle: 'support@rebble.io',
            actions: [
              CobbleCardAction(
                label: tr.health.sync.signOut,
                onPressed: () {},
              ),
              CobbleCardAction(
                label: tr.health.sync.switchAccount,
                onPressed: () {},
              ),
            ],
          ),
          CobbleTile.accordion(
            title: tr.health.database.title,
            children: [
              CobbleTile.action(
                leading: RebbleIcons.floppy_disk_health_database,
                trailing: RebbleIcons.caret_right,
                title: tr.health.database.manage,
                onTap: manageHealthDatabase(context),
              ),
              CobbleTile.action(
                leading: RebbleIcons.delete_trash,
                title: tr.health.database.delete,
                intent: context.scheme.destructive,
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
          title: tr.health.database.permanentlyDelete.title,
          content: tr.health.database.permanentlyDelete.description,
          positive: tr.health.database.permanentlyDelete.positive,
          negative: tr.health.database.permanentlyDelete.negative,
          intent: context.scheme.destructive,
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
                title: tr.health.database.backup,
                onTap: () {},
              ),
              CobbleTile.action(
                leading: RebbleIcons.floppy_disk,
                trailing: RebbleIcons.caret_right,
                title: tr.health.database.restore,
                onTap: () {},
              ),
              CobbleDivider(),
              CobbleTile.action(
                leading: RebbleIcons.delete_trash,
                trailing: RebbleIcons.caret_right,
                title: tr.health.database.permDelete,
                intent: context.scheme.destructive,
                onTap: deleteHealthDatabase(context),
              ),
            ],
          ),
        );
      };
}
