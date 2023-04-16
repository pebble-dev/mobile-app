import 'package:cached_network_image/cached_network_image.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:flutter/material.dart';
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/system_app_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/common/components/cobble_divider.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:share/share.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';

class AppsSheet {
  static void showModal({
    required BuildContext context,
    required App app,
    bool compatible = false,
    required AppManager appManager,
    PebbleWatchLine? lineConnected,
    String? iconUrl,
  }) {
    CobbleSheet.showModal(
      context: context,
      builder: (context) => Column(
        children: [
          CobbleTile.app(
            leading: iconUrl != null ? CachedNetworkImageProvider(iconUrl) : SystemAppIcon(app.uuid),
            title: "${app.longName} ${app.version}",
            subtitle: app.company,
          ),
          SizedBox(height: 8),
          if (app.appstoreId != null)
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                CobbleButton(
                  outlined: false,
                  icon: RebbleIcons.rebble_store,
                  onPressed: () {},
                ),
                SizedBox(width: 16),
                CobbleButton(
                  outlined: false,
                  icon: RebbleIcons.health_heart,
                  label: "0",
                  onPressed: () {},
                ),
                SizedBox(width: 16),
                CobbleButton(
                  outlined: false,
                  icon: RebbleIcons.share,
                  onPressed: () => Share.share(
                    "https://store-beta.rebble.io/app/${app.appstoreId}",
                  ),
                ),
              ],
            ),
          SizedBox(height: 16),
          CobbleDivider(),
          if (compatible) ...[
            CobbleTile.action(
              leading: RebbleIcons.permissions,
              title: tr.lockerPage.permissions,
              onTap: () {},
            ),
            CobbleTile.action(
              leading: RebbleIcons.settings,
              title: tr.lockerPage.appSettings,
              onTap: () {},
            ),
          ] else
            CobbleTile.action(
              leading: RebbleIcons.unpair_from_watch,
              title: tr.lockerPage.notCompatible(
                watch: stringFromWatchLine(
                    lineConnected ?? PebbleWatchLine.unknown),
              ),
              onTap: () {},
            ),
          CobbleDivider(),
          CobbleTile.action(
            leading: RebbleIcons.delete_trash,
            title: tr.lockerPage.delete,
            onTap: () => appManager.deleteApp(app.uuid),
          ),
        ],
      ),
    );
  }
}
