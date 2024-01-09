import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final localNotificationsPluginProvider =
    FutureProvider<FlutterLocalNotificationsPlugin>(
  (ref) async {
    final plugin = FlutterLocalNotificationsPlugin();

    const AndroidInitializationSettings initializationSettingsAndroid =
        AndroidInitializationSettings('@drawable/ic_notification_warning');
    const IOSInitializationSettings initializationSettingsIOS =
        IOSInitializationSettings(requestBadgePermission: false, defaultPresentBadge: false);

    await plugin.initialize(const InitializationSettings(
      android: initializationSettingsAndroid,
      iOS: initializationSettingsIOS
    ));

    return plugin;
  },
);
