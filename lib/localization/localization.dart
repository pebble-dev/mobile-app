import 'dart:convert';

import 'package:cobble/localization/model/model_generator.model.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:json_annotation/json_annotation.dart';

class Localization {
  final Language _language;
  final StateError _error;

  Localization({
    Language language,
    StateError error,
  })  : _language = language,
        _error = error;

  static Localization _instance;

  static Localization get instance {
    if (_instance == null) {
      throw StateError(
        'Localization.instance is null, call Localization.load first. '
        'CobbleLocalizationDelegate should call it automatically, '
        'did you configure it correctly?',
      );
    }
    return _instance;
  }

  static Future<Localization> load(Locale locale) async {
    String jsonString = await rootBundle.loadString(
      'lang/${locale.languageCode}.json',
    );
    Localization instance;
    try {
      final language = Language.fromJson(json.decode(jsonString));
      instance = Localization(language: language);
    } on BadKeyException catch (e) {
      instance = Localization(error: StateError(e.message));
    }

    assert(instance != null);
    _instance = instance;
    return _instance;
  }
}

extension Tr on String {
  String args({
    List<String> positional,
    Map<String, String> named,
  }) {
    String value = this;
    value = _replaceNamedArgs(value, named);
    value = _replaceArgs(value, positional);
    return value;
  }

  String _replaceArgs(String res, List<String> args) {
    if (args == null || args.isEmpty) return res;
    args.forEach((str) => res = res.replaceFirst(RegExp(r'{}'), str));
    return res;
  }

  String _replaceNamedArgs(String res, Map<String, String> args) {
    if (args == null || args.isEmpty) return res;
    args.forEach(
      (key, value) => res = res.replaceAll(RegExp('{$key}'), value),
    );
    return res;
  }
}

/// Singleton with parsed locale file.
///
/// Example usage:
/// ```dart
/// Text(
///   tr.splash_page.title.args(
///     positional: ['first', 'second'],
///     named: {
///       'param': 'value',
///     },
///   ),
/// )
/// ```
Language get tr {
  if (Localization.instance._error != null) {
    throw Localization.instance._error;
  }
  return Localization.instance._language;
}
