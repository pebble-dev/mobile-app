import 'dart:math';

import 'package:cobble/domain/package_details.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_sheet.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/alerting_app_details.dart';
import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import './alerting_apps/sheet.dart';

class AlertingApp {
  final String name;
  final bool enabled;
  final String packageId;

  AlertingApp(this.name, this.enabled, this.packageId);
}

class AlertingApps extends HookConsumerWidget implements CobbleScreen {
  final notificationsControl = NotificationsControl();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    var packageDetailsP = ref.watch(packageDetailsProvider);
    final notifyingPackagesF = useMemoized(() => notificationsControl.getNotificationPackages());
    final packageDetailsF = useMemoized(() => packageDetailsP.getPackageList());
    final notifyingPackages = useFuture(notifyingPackagesF);
    final packageDetails = useFuture(packageDetailsF);

    final random = Random();
    final filter = useState(SheetOnChanged.initial);

    final sheet = CobbleSheet.useInline();
    final mutedPackages = ref.watch(notificationsMutedPackagesProvider);

    List<AlertingApp> apps = [];
    if (packageDetails.hasData && packageDetails.data != null) {
      for (int i = 0; i < packageDetails.data!.packageId!.length; i++) {
        final enabled = (mutedPackages.value ?? []).firstWhereOrNull(
              (element) => element == packageDetails.data!.packageId![i],
        ) == null;
        if (notifyingPackages.hasData &&
            notifyingPackages.data!.firstWhereOrNull((element) => element!
                .packageId == packageDetails.data!.packageId![i]) != null) {
          apps.add(AlertingApp(packageDetails.data!.appName![i] as String, enabled,
              packageDetails.data!.packageId![i] as String));
        }
      }
    }

    List filteredApps = apps.where(
          (app) =>
          app.name.toLowerCase().contains(
            filter.value.query?.toLowerCase() ?? '',
          ),
    ).toList();

    return CobbleScaffold.tab(
        title: tr.alertingApps.title,
        subtitle: tr.alertingApps.subtitle(
          alerted: 5.toString(),
          muted: 3.toString(),
        ),
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
        child: packageDetails.hasData && notifyingPackages.hasData ? ListView.builder(
          itemCount: filteredApps.length,
          itemBuilder: (BuildContext context, int index) {
            AlertingApp app = filteredApps[index];
            return CobbleTile.appNavigation(
              leading: Svg('images/temp_alerting_app.svg'),
              title: app.name,
              subtitle: app.enabled
                  ? tr.alertingApps.alertedToday(
                alerted: random.nextInt(8).toString(),
              )
                  : tr.alertingApps.mutedToday(
                muted: random.nextInt(8).toString(),
              ),
              navigateTo: AlertingAppDetails(app),
            );
          },
        ) : const Center(
          child: CircularProgressIndicator(),
        )
    );
  }
}
