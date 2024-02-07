// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sheet.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SheetOnChanged _$SheetOnChangedFromJson(Map<String, dynamic> json) =>
    SheetOnChanged(
      json['query'] as String?,
      $enumDecodeNullable(_$AppSourceEnumMap, json['source']),
    );

Map<String, dynamic> _$SheetOnChangedToJson(SheetOnChanged instance) =>
    <String, dynamic>{
      'query': instance.query,
      'source': _$AppSourceEnumMap[instance.source],
    };

const _$AppSourceEnumMap = {
  AppSource.All: 'All apps',
  AppSource.Phone: 'Phone only',
  AppSource.Watch: 'Watch only',
};
