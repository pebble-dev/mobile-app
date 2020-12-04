// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_attribute.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

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
