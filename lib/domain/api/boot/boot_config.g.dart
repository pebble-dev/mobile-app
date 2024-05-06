// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'boot_config.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BootConfig _$BootConfigFromJson(Map<String, dynamic> json) => BootConfig(
      auth: AuthConfig.fromJson(json['auth'] as Map<String, dynamic>),
      appstore: BaseURLEntry.fromJson(json['appstore'] as Map<String, dynamic>),
      webviews:
          WebviewConfig.fromJson(json['webviews'] as Map<String, dynamic>),
    );

Map<String, dynamic> _$BootConfigToJson(BootConfig instance) =>
    <String, dynamic>{
      'auth': instance.auth,
      'appstore': instance.appstore,
      'webviews': instance.webviews,
    };
