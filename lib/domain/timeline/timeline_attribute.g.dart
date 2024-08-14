// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_attribute.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelineAttribute _$TimelineAttributeFromJson(Map<String, dynamic> json) =>
    TimelineAttribute(
      id: (json['id'] as num?)?.toInt(),
      string: json['string'] as String?,
      listOfString: (json['listOfString'] as List<dynamic>?)
          ?.map((e) => e as String)
          .toList(),
      uint8: (json['uint8'] as num?)?.toInt(),
      uint32: (json['uint32'] as num?)?.toInt(),
      maxLength: (json['maxLength'] as num?)?.toInt(),
    );

Map<String, dynamic> _$TimelineAttributeToJson(TimelineAttribute instance) {
  final val = <String, dynamic>{};

  void writeNotNull(String key, dynamic value) {
    if (value != null) {
      val[key] = value;
    }
  }

  writeNotNull('id', instance.id);
  writeNotNull('string', instance.string);
  writeNotNull('listOfString', instance.listOfString);
  writeNotNull('uint8', instance.uint8);
  writeNotNull('uint32', instance.uint32);
  writeNotNull('maxLength', instance.maxLength);
  return val;
}
