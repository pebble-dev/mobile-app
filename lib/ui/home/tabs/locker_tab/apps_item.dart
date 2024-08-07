import 'package:cached_network_image/cached_network_image.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/system_app_icon.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/home/tabs/locker_tab/apps_sheet.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class AppsItem extends ConsumerWidget {
  final App app;
  final bool compatible;
  final AppManager appManager;
  final PebbleWatchLine? lineConnected;
  final int? index;
  final String? iconUrl;

  const AppsItem({
    required this.app,
    this.compatible = false,
    required this.appManager,
    this.lineConnected,
    this.index,
    this.iconUrl,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Container(
      key: key,
      child: Row(
        children: [
          if (compatible)
            ReorderableDragStartListener(
              child: Padding(
                padding: EdgeInsets.only(left: 16),
                child: Icon(
                  RebbleIcons.drag_handle,
                  size: 25.0,
                  color: context.scheme!.muted,
                ),
              ),
              index: index ?? 0,
            )
          else
            SizedBox(width: 57),
          Expanded(
            child: CobbleTile.app(
              leading: iconUrl != null ? CachedNetworkImage(imageUrl: iconUrl!, errorListener: (e) {
                Log.e("Error loading app image: $e");
              },
                placeholder: (context, url) => SystemAppIcon(app.uuid),
              ): SystemAppIcon(app.uuid),
              title: app.longName,
              subtitle: app.company,
              onTap: () => AppsSheet.showModal(
                iconUrl: iconUrl,
                context: context,
                app: app,
                compatible: compatible,
                appManager: appManager,
              ),
              child: CobbleButton(
                outlined: false,
                icon: compatible
                ? RebbleIcons.settings
                : RebbleIcons.menu_vertical,
                onPressed: () {},
              ),
            ),
          ),
        ],
      ),
    );
  }
}
