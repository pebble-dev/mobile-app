// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'app.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

App _$AppFromJson(Map<String, dynamic> json) {
  return App(
    uuid: const NonNullUuidConverter().fromJson(json['uuid'] as String),
    shortName: json['shortName'] as String,
    longName: json['longName'] as String,
    company: json['company'] as String,
    appstoreId: json['appstoreId'] as String?,
    version: json['version'] as String,
    isWatchface:
        const BooleanNumberConverter().fromJson(json['isWatchface'] as int),
    supportedHardware: const CommaSeparatedListConverter()
        .fromJson(json['supportedHardware'] as String),
    nextSyncAction:
        _$enumDecode(_$NextSyncActionEnumMap, json['nextSyncAction']),
    appOrder: json['appOrder'] as int,
  );
}

Map<String, dynamic> _$AppToJson(App instance) => <String, dynamic>{
      'uuid': const NonNullUuidConverter().toJson(instance.uuid),
      'shortName': instance.shortName,
      'longName': instance.longName,
      'company': instance.company,
      'appstoreId': instance.appstoreId,
      'version': instance.version,
      'isWatchface':
          const BooleanNumberConverter().toJson(instance.isWatchface),
      'supportedHardware': const CommaSeparatedListConverter()
          .toJson(instance.supportedHardware),
      'nextSyncAction': _$NextSyncActionEnumMap[instance.nextSyncAction],
      'appOrder': instance.appOrder,
    };

K _$enumDecode<K, V>(
  Map<K, V> enumValues,
  Object? source, {
  K? unknownValue,
}) {
  if (source == null) {
    throw ArgumentError(
      'A value must be provided. Supported values: '
      '${enumValues.values.join(', ')}',
    );
  }

  return enumValues.entries.singleWhere(
    (e) => e.value == source,
    orElse: () {
      if (unknownValue == null) {
        throw ArgumentError(
          '`$source` is not one of the supported values: '
          '${enumValues.values.join(', ')}',
        );
      }
      return MapEntry(unknownValue, enumValues.values.first);
    },
  ).key;
}

const _$NextSyncActionEnumMap = {
  NextSyncAction.Nothing: 'Nothing',
  NextSyncAction.Upload: 'Upload',
  NextSyncAction.Delete: 'Delete',
  NextSyncAction.Ignore: 'Ignore',
  NextSyncAction.DeleteThenIgnore: 'DeleteThenIgnore',
};
