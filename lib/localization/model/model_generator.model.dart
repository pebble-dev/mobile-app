// GENERATED CODE - DO NOT MODIFY BY HAND

// **************************************************************************
// ModelGenerator
// **************************************************************************

import 'package:json_annotation/json_annotation.dart';
import 'dart:ui';

part 'model_generator.model.g.dart';

String _args(
  String value, {
  List<String> positional,
  Map<String, String> named,
}) {
  named.forEach(
    (key, _value) => value = value.replaceAll(RegExp('{$key}'), _value),
  );
  positional.forEach((str) => value = value.replaceFirst(RegExp(r'{}'), str));
  return value;
}

@JsonSerializable(
  createToJson: false,
  nullable: false,
  disallowUnrecognizedKeys: true,
)
class Language {
  @JsonKey(
    name: 'common',
    required: true,
    disallowNullValue: true,
  )
  final LanguageCommon common;

  @JsonKey(
    name: 'first_run',
    required: true,
    disallowNullValue: true,
  )
  final LanguageFirstRun firstRun;

  Language(this.common, this.firstRun);

  factory Language.fromJson(Map<String, dynamic> json) =>
      _$LanguageFromJson(json);
}

@JsonSerializable(
  createToJson: false,
  nullable: false,
  disallowUnrecognizedKeys: true,
)
class LanguageCommon {
  @JsonKey(
    name: 'skip',
    required: true,
    disallowNullValue: true,
  )
  final String skip;

  LanguageCommon(this.skip);

  factory LanguageCommon.fromJson(Map<String, dynamic> json) =>
      _$LanguageCommonFromJson(json);
}

@JsonSerializable(
  createToJson: false,
  nullable: false,
  disallowUnrecognizedKeys: true,
)
class LanguageFirstRun {
  @JsonKey(
    name: 'title',
    required: true,
    disallowNullValue: true,
  )
  final String title;

  @JsonKey(
    name: 'fab',
    required: true,
    disallowNullValue: true,
  )
  final String fab;

  LanguageFirstRun(this.title, this.fab);

  factory LanguageFirstRun.fromJson(Map<String, dynamic> json) =>
      _$LanguageFirstRunFromJson(json);
}

final supportedLocales = [
  Locale('en'),
  Locale('es'),
];
