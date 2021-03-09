import 'dart:async';

import 'package:cobble/domain/db/dao/notification_channel_dao.dart';
import 'package:cobble/domain/db/models/notification_channel.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/ui/common/components/cobble_tile.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/alerting_apps.dart';
import 'package:flutter/material.dart';
import 'package:flutter/src/widgets/framework.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_svg_provider/flutter_svg_provider.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:cobble/ui/theme/with_cobble_theme.dart';

class AlertingAppDetails extends HookWidget implements CobbleScreen {
  AlertingApp app;
  AlertingAppDetails(this.app);

  @override
  Widget build(BuildContext context) {
    final channelDao = useProvider(notifChannelDaoProvider);
    final mutedPackages = useProvider(notificationsMutedPackagesProvider);
    final preferences = useProvider(preferencesProvider);

    final StreamController<List<NotificationChannel>> streamController = StreamController();

    channelDao.getNotifChannelsByPackage(app.packageId).then((value) => streamController.add(value));

    return CobbleScaffold.tab(
        title: 'Alert Settings',
        subtitle: '8 alerted, 5 muted today',
        child: Column(
          children: [
            Container(
              margin: EdgeInsets.symmetric(vertical: 15),
              child: Column(
                children: [
                  Image(image: Svg('images/temp_alerting_app.svg')),
                  Text(app.name)
                ],
              ),
            ),
            Container(
              padding: EdgeInsets.symmetric(vertical: 5),
              margin: EdgeInsets.symmetric(vertical: 15),
              color: context.scheme!.surface,
              child: CobbleTile.setting(
                  title: "All \"${app.name}\" notifications",
                  child: Switch(
                    value: app.enabled,
                    onChanged: (value) async {
                      var mutedPkgList = mutedPackages.data?.value ?? [];
                      if (value) {
                        mutedPkgList.removeWhere((element) => element == app.packageId);
                      }else {
                        print(app.packageId);
                        mutedPkgList.add(app.packageId);
                      }
                      app = AlertingApp(app.name, value, app.packageId);
                      await preferences.data?.value
                          .setNotificationsMutedPackages(mutedPkgList);
                    },
                  ),
              ),
            ),
            Expanded(
                child: StreamBuilder(
                  stream: streamController.stream,
                  builder: (BuildContext context, AsyncSnapshot<List<NotificationChannel>> snapshot) {
                    if (snapshot.hasData) {
                      if (snapshot.data!.isEmpty) {
                        return Center(child: Text("No channels notified, check back after you receive a notification."));
                      }else {
                        return ListView(
                          children: snapshot.data!.map((e) =>
                              CobbleTile.setting
                                (title: e.name ?? e.channelId,
                                  subtitle: e.description,
                                  child: Switch(
                                    value: e.shouldNotify,
                                    onChanged: (value) {
                                      e.shouldNotify = value;
                                      channelDao.insertOrUpdateNotificationChannel(e);
                                      streamController.add(snapshot.data!);
                                    },
                                  )
                              )
                          ).toList(),
                        );
                      }
                    }else {
                      return Center(child: CircularProgressIndicator());
                    }
                  },
                )
            )
          ]
        )
    );
  }

}