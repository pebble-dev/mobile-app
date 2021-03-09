// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'model_generator.model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Language _$LanguageFromJson(Map<String, dynamic> json) {
  $checkKeys(json,
      allowedKeys: const ['common', 'first_run'],
      requiredKeys: const ['common', 'first_run'],
      disallowNullValues: const ['common', 'first_run']);
  return Language(
    LanguageCommon.fromJson(json['common'] as Map<String, dynamic>),
    LanguageFirstRun.fromJson(json['first_run'] as Map<String, dynamic>),
  );
}

LanguageCommon _$LanguageCommonFromJson(Map<String, dynamic> json) {
  $checkKeys(json,
      allowedKeys: const ['skip'],
      requiredKeys: const ['skip'],
      disallowNullValues: const ['skip']);
  return LanguageCommon(
    json['skip'] as String,
  );
}

LanguageFirstRun _$LanguageFirstRunFromJson(Map<String, dynamic> json) {
  $checkKeys(json,
      allowedKeys: const ['title', 'fab'],
      requiredKeys: const ['title', 'fab'],
      disallowNullValues: const ['title', 'fab']);
  return LanguageFirstRun(
    json['title'] as String,
    json['fab'] as String,
  );
}
