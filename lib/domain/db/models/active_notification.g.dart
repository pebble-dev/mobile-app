// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'active_notification.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ActiveNotification _$ActiveNotificationFromJson(Map<String, dynamic> json) =>
    ActiveNotification(
      pinId: const UuidConverter().fromJson(json['pinId'] as String?),
      notifId: json['notifId'] as int?,
      packageId: json['packageId'] as String?,
      tagId: json['tagId'] as String?,
    );

Map<String, dynamic> _$ActiveNotificationToJson(ActiveNotification instance) =>
    <String, dynamic>{
      'pinId': const UuidConverter().toJson(instance.pinId),
      'notifId': instance.notifId,
      'packageId': instance.packageId,
      'tagId': instance.tagId,
    };
