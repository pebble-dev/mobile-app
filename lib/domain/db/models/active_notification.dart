import 'package:cobble/domain/db/converters/sql_json_converters.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:uuid_type/uuid_type.dart';

part 'active_notification.g.dart';

@JsonSerializable()
@UuidConverter()
class ActiveNotification {
  final Uuid? pinId;
  final int? notifId;
  final String? packageId;
  final String? tagId;

  ActiveNotification({
    this.pinId,
    this.notifId,
    this.packageId,
    this.tagId
  });

  Map<String, dynamic> toMap() {
    return _$ActiveNotificationToJson(this);
  }

  factory ActiveNotification.fromMap(Map<String, dynamic> map) {
    return _$ActiveNotificationFromJson(map);
  }
}