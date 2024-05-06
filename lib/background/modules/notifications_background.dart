import 'package:cobble/background/notification/notification_manager.dart';
import 'package:cobble/domain/db/dao/notification_channel_dao.dart';
import 'package:cobble/domain/db/models/notification_channel.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:uuid_type/uuid_type.dart';

class NotificationsBackground implements NotificationListening {
  final ProviderContainer container;

  late NotificationManager notificationManager;
  late NotificationChannelDao _notificationChannelDao;

  NotificationsBackground(this.container);

  void init() async {
    notificationManager = container.listen<NotificationManager>(notificationManagerProvider, (previous, value) {}).read();
    _notificationChannelDao = container.listen<NotificationChannelDao>(notifChannelDaoProvider, (previous, value) {}).read();

    NotificationListening.setup(this);
  }

  @override
  Future<TimelinePinPigeon> handleNotification(NotificationPigeon arg) async {
    final notif = await notificationManager.handleNotification(arg);

    return notif.toPigeon();
  }

  @override
  void dismissNotification(StringWrapper arg) {
    notificationManager.dismissNotification(Uuid.parse(arg.value!));
  }

  @override
  Future<BooleanWrapper> shouldNotify(NotifChannelPigeon arg) async {
    if (arg.channelId != null && arg.packageId != null) {
      NotificationChannel? channel = await _notificationChannelDao.getNotifChannelByIds(arg.channelId!, arg.packageId!);
      return BooleanWrapper(value: channel?.shouldNotify ?? true);
    } else {
      return BooleanWrapper(value: false);
    }
  }

  @override
  void updateChannel(NotifChannelPigeon arg) {
    if (arg.delete == true) {
      _notificationChannelDao.deleteNotifChannelByIds(arg.channelId!, arg.packageId!);
    }else {
      _notificationChannelDao.getNotifChannelByIds(arg.channelId!, arg.packageId!).then((existing) {
        final shouldNotify = existing?.shouldNotify ?? true;
        final channel = NotificationChannel(arg.packageId!, arg.channelId!, shouldNotify, name: arg.channelName, description: arg.channelDesc);
        _notificationChannelDao.insertOrUpdateNotificationChannel(channel);
      });
    }
  }
}
