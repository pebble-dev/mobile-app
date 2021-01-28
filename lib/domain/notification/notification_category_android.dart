import 'package:cobble/domain/timeline/timeline_icon.dart';

class CategoryAndroid {
  final String id;
  final TimelineIcon icon;
  const CategoryAndroid(this.id, this.icon);

  static const categories = const [
    CategoryAndroid('email', TimelineIcon.genericEmail),
    CategoryAndroid('msg', TimelineIcon.genericSms),
    CategoryAndroid('event', TimelineIcon.timelineCalendar),
    CategoryAndroid('promo', TimelineIcon.payBill),
    CategoryAndroid('navigation', TimelineIcon.location),
    CategoryAndroid('alarm', TimelineIcon.alarmClock),
    CategoryAndroid('social', TimelineIcon.newsEvent),
    CategoryAndroid('err', TimelineIcon.genericWarning),
    CategoryAndroid('transport', TimelineIcon.audioCassette),
    CategoryAndroid('sys', TimelineIcon.settings),
    CategoryAndroid('reminder', TimelineIcon.notificationReminder)
  ];

  static CategoryAndroid fromId(String id) => categories.firstWhere(
          (element) => element.id == id,
      orElse: () => null);
}