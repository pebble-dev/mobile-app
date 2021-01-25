import 'package:json_annotation/json_annotation.dart';

part 'notification_message.g.dart';

@JsonSerializable(includeIfNull: false)
class NotificationMessage {
  String sender;
  String text;
  int timestamp;

  Map<String, dynamic> toJson() {
    return _$NotificationMessageToJson(this);
  }

  static NotificationMessage fromJson(Map<String, dynamic> json) {
    return _$NotificationMessageFromJson(json);
  }
}