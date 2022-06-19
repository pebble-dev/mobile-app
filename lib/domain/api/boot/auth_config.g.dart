// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'auth_config.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AuthConfig _$AuthConfigFromJson(Map<String, dynamic> json) => AuthConfig(
      base: json['base'],
      authoriseUrl: json['authorise_url'] as String,
      refreshUrl: json['refresh_url'] as String,
      clientId: json['client_id'] as String,
    );

Map<String, dynamic> _$AuthConfigToJson(AuthConfig instance) =>
    <String, dynamic>{
      'base': instance.base,
      'authorise_url': instance.authoriseUrl,
      'refresh_url': instance.refreshUrl,
      'client_id': instance.clientId,
    };
