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
  );
}

Map<String, dynamic> _$NotificationChannelToJson(
        NotificationChannel instance) =>
    <String, dynamic>{
      'packageId': instance.packageId,
      'channelId': instance.channelId,
      'shouldNotify':
          const BooleanNumberConverter().toJson(instance.shouldNotify),
    };
