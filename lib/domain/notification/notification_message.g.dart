// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_message.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationMessage _$NotificationMessageFromJson(Map<String, dynamic> json) =>
    NotificationMessage()
      ..sender = json['sender'] as String?
      ..text = json['text'] as String?
      ..timestamp = json['timestamp'] as int?;

Map<String, dynamic> _$NotificationMessageToJson(NotificationMessage instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('sender', instance.sender);
  writeNotNull('text', instance.text);
  writeNotNull('timestamp', instance.timestamp);
  return val;
}
