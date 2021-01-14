import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:hooks_riverpod/all.dart';

final localNotificationsPluginProvider =
    FutureProvider<FlutterLocalNotificationsPlugin>(
  (ref) async {
    final plugin = FlutterLocalNotificationsPlugin();

    const AndroidInitializationSettings initializationSettingsAndroid =
        AndroidInitializationSettings('@drawable/ic_notification_warning');

    await plugin.initialize(InitializationSettings(
      android: initializationSettingsAndroid,
    ));

    return plugin;
  },
);
