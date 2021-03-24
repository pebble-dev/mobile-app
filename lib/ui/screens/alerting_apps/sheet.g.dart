// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sheet.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SheetOnChanged _$SheetOnChangedFromJson(Map<String, dynamic> json) {
  return SheetOnChanged(
    json['query'] as String?,
    _$enumDecodeNullable(_$AppSourceEnumMap, json['source']),
  );
}

Map<String, dynamic> _$SheetOnChangedToJson(SheetOnChanged instance) =>
    <String, dynamic>{
      'query': instance.query,
      'source': _$AppSourceEnumMap[instance.source],
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

K? _$enumDecodeNullable<K, V>(
  Map<K, V> enumValues,
  dynamic source, {
  K? unknownValue,
}) {
  if (source == null) {
    return null;
  }
  return _$enumDecode<K, V>(enumValues, source, unknownValue: unknownValue);
}

const _$AppSourceEnumMap = {
  AppSource.All: 'All apps',
  AppSource.Phone: 'Phone only',
  AppSource.Watch: 'Watch only',
};
