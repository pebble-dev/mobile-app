import 'package:cobble/domain/apps/app_manager.dart';
import 'package:cobble/domain/db/models/app.dart';
import 'package:cobble/domain/entities/hardware_platform.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/home/tabs/locker_tab/faces_sheet.dart';
import 'package:flutter/material.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class FacesCard extends ConsumerWidget {
  final App face;
  final bool compatible;
  final AppManager appManager;
  final PebbleWatchLine? lineConnected;
  final bool? circleConnected;

  const FacesCard({
    required this.face,
    this.compatible = false,
    required this.appManager,
    this.lineConnected,
    this.circleConnected,
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Card(
      key: key,
      child: Container(
        margin: EdgeInsets.all(8),
        child: Row(
          children: <Widget>[
            Expanded(
              child: FacesPreview(
                  face: face,
                  compatible: compatible,
                  circleConnected: circleConnected),
            ),
            Column(
              children: <Widget>[
                // TODO: Implement sending to the watch
                Expanded(
                  child: compatible
                      ? CobbleButton(
                          outlined: false,
                          icon: RebbleIcons.send_to_watch_unchecked,
                          onPressed: () {},
                        )
                      : Container(),
                ),
                // TODO: Implement settings and showing and hiding this button depending on whether or not settings are available
                Expanded(
                  child: CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.settings,
                    onPressed: () {},
                  ),
                ),
                Expanded(
                  child: CobbleButton(
                    outlined: false,
                    icon: RebbleIcons.menu_vertical,
                    onPressed: () => FacesSheet.showModal(
                      ref: ref, context: context,
                      face: face,
                      compatible: compatible,
                      appManager: appManager,
                      lineConnected: lineConnected,
                      circleConnected: circleConnected,
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
