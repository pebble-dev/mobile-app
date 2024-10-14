// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

App _$AppFromJson(Map<String, dynamic> json) => App(
      uuid: const NonNullUuidConverter().fromJson(json['uuid'] as String),
      shortName: json['shortName'] as String,
      longName: json['longName'] as String,
      company: json['company'] as String,
      appstoreId: json['appstoreId'] as String?,
      version: json['version'] as String,
      isWatchface: const BooleanNumberConverter()
          .fromJson((json['isWatchface'] as num).toInt()),
      isSystem: const BooleanNumberConverter()
          .fromJson((json['isSystem'] as num).toInt()),
      supportedHardware: const CommaSeparatedListConverter()
          .fromJson(json['supportedHardware'] as String),
      nextSyncAction:
          $enumDecode(_$NextSyncActionEnumMap, json['nextSyncAction']),
      appOrder: (json['appOrder'] as num).toInt(),
      processInfoFlags: json['processInfoFlags'] as String,
      sdkVersions: json['sdkVersions'] as String,
      url: json['url'] as String?,
    );

Map<String, dynamic> _$AppToJson(App instance) => <String, dynamic>{
      'uuid': const NonNullUuidConverter().toJson(instance.uuid),
      'shortName': instance.shortName,
      'longName': instance.longName,
      'company': instance.company,
      'appstoreId': instance.appstoreId,
      'version': instance.version,
      'isWatchface':
          const BooleanNumberConverter().toJson(instance.isWatchface),
      'isSystem': const BooleanNumberConverter().toJson(instance.isSystem),
      'supportedHardware': const CommaSeparatedListConverter()
          .toJson(instance.supportedHardware),
      'nextSyncAction': _$NextSyncActionEnumMap[instance.nextSyncAction]!,
      'appOrder': instance.appOrder,
      'url': instance.url,
      'processInfoFlags': instance.processInfoFlags,
      'sdkVersions': instance.sdkVersions,
    };

const _$NextSyncActionEnumMap = {
  NextSyncAction.Nothing: 'Nothing',
  NextSyncAction.Upload: 'Upload',
  NextSyncAction.Delete: 'Delete',
  NextSyncAction.Ignore: 'Ignore',
  NextSyncAction.DeleteThenIgnore: 'DeleteThenIgnore',
};
