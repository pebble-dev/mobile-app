// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'appstore_app.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AppstoreApp _$AppstoreAppFromJson(Map<String, dynamic> json) {
  return AppstoreApp(
    id: json['id'] as String,
    uuid: const NonNullUuidConverter().fromJson(json['uuid'] as String),
    title: json['title'] as String,
    listImage:
        (json['listImage'] as List<dynamic>?)?.map((e) => e as int).toList(),
    iconImage:
        (json['iconImage'] as List<dynamic>?)?.map((e) => e as int).toList(),
    screenshotImage: (json['screenshotImage'] as List<dynamic>?)
        ?.map((e) => e as int)
        .toList(),
    isWatchface:
        const BooleanNumberConverter().fromJson(json['isWatchface'] as int),
  );
}

Map<String, dynamic> _$AppstoreAppToJson(AppstoreApp instance) =>
    <String, dynamic>{
      'id': instance.id,
      'uuid': const NonNullUuidConverter().toJson(instance.uuid),
      'title': instance.title,
      'listImage': instance.listImage,
      'iconImage': instance.iconImage,
      'screenshotImage': instance.screenshotImage,
      'isWatchface':
          const BooleanNumberConverter().toJson(instance.isWatchface),
    };
