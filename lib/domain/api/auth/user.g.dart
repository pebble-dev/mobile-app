// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

User _$UserFromJson(Map<String, dynamic> json) => User(
      hasTimeline: json['has_timeline'] as bool,
      isSubscribed: json['is_subscribed'] as bool,
      isWizard: json['is_wizard'] as bool,
      name: json['name'] as String,
      scopes:
          (json['scopes'] as List<dynamic>).map((e) => e as String).toList(),
      timelineTtl: json['timeline_ttl'] as int,
      uid: json['uid'] as int,
      bootOverrides: json['boot_overrides'] as Map<String, dynamic>?,
    );

Map<String, dynamic> _$UserToJson(User instance) => <String, dynamic>{
      'boot_overrides': instance.bootOverrides,
      'has_timeline': instance.hasTimeline,
      'is_subscribed': instance.isSubscribed,
      'is_wizard': instance.isWizard,
      'name': instance.name,
      'scopes': instance.scopes,
      'timeline_ttl': instance.timelineTtl,
      'uid': instance.uid,
    };
