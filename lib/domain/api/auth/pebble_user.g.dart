// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pebble_user.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PebbleUser _$PebbleUserFromJson(Map<String, dynamic> json) => PebbleUser(
      id: json['id'] as String,
      name: json['name'] as String,
      email: json['email'] as String?,
    );

Map<String, dynamic> _$PebbleUserToJson(PebbleUser instance) =>
    <String, dynamic>{
      'email': instance.email,
      'id': instance.id,
      'name': instance.name,
    };
