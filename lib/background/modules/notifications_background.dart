import 'package:cobble/background/notification/notification_manager.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';

class NotificationsBackground implements NotificationListening {
  final ProviderContainer container;

  late NotificationManager notificationManager;

  NotificationsBackground(this.container);

  void init() async {
    notificationManager = container.listen<NotificationManager>(notificationManagerProvider, (previous, value) {}).read();

    NotificationListening.setup(this);
  }

  @override
  Future<TimelinePinPigeon> handleNotification(NotificationPigeon arg) async {
    return (await notificationManager.handleNotification(arg)).toPigeon();
  }

  @override
  void dismissNotification(StringWrapper arg) {
    notificationManager.dismissNotification(Uuid.parse(arg.value!));
  }
}
