// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sheet.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SheetOnChanged _$SheetOnChangedFromJson(Map<String, dynamic> json) {
  return SheetOnChanged(
    json['query'] as String,
    _$enumDecodeNullable(_$AppSourceEnumMap, json['source']),
  );
}

Map<String, dynamic> _$SheetOnChangedToJson(SheetOnChanged instance) =>
    <String, dynamic>{
      'query': instance.query,
      'source': _$AppSourceEnumMap[instance.source],
    };

T _$enumDecode<T>(
  Map<T, dynamic> enumValues,
  dynamic source, {
  T unknownValue,
}) {
  if (source == null) {
    throw ArgumentError('A value must be provided. Supported values: '
        '${enumValues.values.join(', ')}');
  }

  final value = enumValues.entries
      .singleWhere((e) => e.value == source, orElse: () => null)
      ?.key;

  if (value == null && unknownValue == null) {
    throw ArgumentError('`$source` is not one of the supported values: '
        '${enumValues.values.join(', ')}');
  }
  return value ?? unknownValue;
}

T _$enumDecodeNullable<T>(
  Map<T, dynamic> enumValues,
  dynamic source, {
  T unknownValue,
}) {
  if (source == null) {
    return null;
  }
  return _$enumDecode<T>(enumValues, source, unknownValue: unknownValue);
}

const _$AppSourceEnumMap = {
  AppSource.All: 'All apps',
  AppSource.Phone: 'Phone only',
  AppSource.Watch: 'Watch only',
};
