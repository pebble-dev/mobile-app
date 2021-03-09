// GENERATED CODE - DO NOT MODIFY BY HAND

// **************************************************************************
// ModelGenerator
// **************************************************************************

import 'package:cobble/localization/model/annotations.dart';
import 'package:json_annotation/json_annotation.dart';

part 'model_generator.model.g.dart';

@Model()
class Language {
  @Field()
  final LanguageCommon common;

  @Field()
  final LanguageFirstRun firstRun;

  Language(this.common, this.firstRun);

  factory Language.fromJson(Map<String, dynamic> json) =>
      _$LanguageFromJson(json);
}

@Model()
class LanguageCommon {
  @Field()
  final String skip;

  LanguageCommon(this.skip);

  factory LanguageCommon.fromJson(Map<String, dynamic> json) =>
      _$LanguageCommonFromJson(json);
}

@Model()
class LanguageFirstRun {
  @Field()
  final String title;

  @Field()
  final String fab;

  LanguageFirstRun(this.title, this.fab);

  factory LanguageFirstRun.fromJson(Map<String, dynamic> json) =>
      _$LanguageFirstRunFromJson(json);
}
