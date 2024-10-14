// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'oauth_token.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OAuthToken _$OAuthTokenFromJson(Map<String, dynamic> json) => OAuthToken(
      accessToken: json['access_token'] as String,
      expiresIn: (json['expires_in'] as num).toInt(),
      tokenType: json['token_type'] as String,
      scope: json['scope'] as String,
      refreshToken: json['refresh_token'] as String,
    );

Map<String, dynamic> _$OAuthTokenToJson(OAuthToken instance) =>
    <String, dynamic>{
      'access_token': instance.accessToken,
      'expires_in': instance.expiresIn,
      'token_type': instance.tokenType,
      'scope': instance.scope,
      'refresh_token': instance.refreshToken,
    };
