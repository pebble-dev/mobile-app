import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

import 'translations.dart';

class Localization {
  final Translations _translations;

  final RegExp _replaceArgRegex = RegExp(r'{}');

  Localization(this._translations);

  static Localization _instance;

  static Localization get instance =>
      _instance ??
      StateError(
        'Localization.instance is null, call Localization.load first. '
        'CobbleLocalizationDelegate should call it automatically, '
        'did you configure it correctly?',
      );

  static Future<Localization> load(Locale locale) async {
    String jsonString = await rootBundle.loadString(
      'lang/${locale.languageCode}.json',
    );
    Map<String, dynamic> jsonMap = json.decode(jsonString);

    _instance = Localization(Translations(jsonMap));
    return _instance;
  }

  String tr(
    String key, {
    List<String> args,
    Map<String, String> namedArgs,
  }) {
    String res = _resolve(key);
    res = _replaceNamedArgs(res, namedArgs);
    return _replaceArgs(res, args);
  }

  String _replaceArgs(String res, List<String> args) {
    if (args == null || args.isEmpty) return res;
    args.forEach((String str) => res = res.replaceFirst(_replaceArgRegex, str));
    return res;
  }

  String _replaceNamedArgs(String res, Map<String, String> args) {
    if (args == null || args.isEmpty) return res;
    args.forEach((String key, String value) =>
        res = res.replaceAll(RegExp('{$key}'), value));
    return res;
  }

  String _resolve(String key, {bool logging = true}) {
    var resource = _translations?.get(key);
    if (resource == null) {
      if (logging) {
        print('Localization key [$key] not found');
      }
      return key;
    }
    return resource;
  }
}

extension Tr on String {
  String tr({
    List<String> args,
    Map<String, String> namedArgs,
  }) =>
      Localization.instance.tr(
        this,
        args: args,
        namedArgs: namedArgs,
      );
}
