import 'package:json_annotation/json_annotation.dart';

part 'notification_action.g.dart';

@JsonSerializable(includeIfNull: false)
class NotificationAction {
  String title;
  bool isResponse;

  Map<String, dynamic> toJson() {
    return _$NotificationActionToJson(this);
  }

  static NotificationAction fromJson(Map<String, dynamic> json) {
    return _$NotificationActionFromJson(json);
  }
}