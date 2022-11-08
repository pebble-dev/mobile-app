// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_channel.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationChannel _$NotificationChannelFromJson(Map<String, dynamic> json) {
  return NotificationChannel(
    json['packageId'] as String,
    json['channelId'] as String,
    const BooleanNumberConverter().fromJson(json['shouldNotify'] as int),
    name: json['name'] as String?,
    description: json['description'] as String?,
  );
}

Map<String, dynamic> _$NotificationChannelToJson(
        NotificationChannel instance) =>
    <String, dynamic>{
      'packageId': instance.packageId,
      'channelId': instance.channelId,
      'name': instance.name,
      'description': instance.description,
      'shouldNotify':
          const BooleanNumberConverter().toJson(instance.shouldNotify),
    };
