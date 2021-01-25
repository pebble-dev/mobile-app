// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_action.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationAction _$NotificationActionFromJson(Map<String, dynamic> json) {
  return NotificationAction()
    ..title = json['title'] as String
    ..isResponse = json['isResponse'] as bool;
}

Map<String, dynamic> _$NotificationActionToJson(NotificationAction instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('title', instance.title);
  writeNotNull('isResponse', instance.isResponse);
  return val;
}
