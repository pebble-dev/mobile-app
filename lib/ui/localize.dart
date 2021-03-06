import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';

class Localize {
  static final Localize _instance = Localize._internal();
  late Locale locale;

  factory Localize([Locale? locale]) {
    print("Localize constructor called!! <<<<<<<");
    if (locale != null) _instance.locale = locale;
    return _instance;
  }

  Localize._internal();

  // Helper method to keep the code in the widgets concise
  static String? it(String key) => _instance.translate(key);

  static String get langCode => _instance.locale.languageCode;

  late Map<String, String> _localizedStrings;

  static List<String> _getWeekDays(String localeName) {
    DateFormat formatter = DateFormat("E", localeName);
    return [
      DateTime(2000, 1, 3, 1),
      DateTime(2000, 1, 4, 1),
      DateTime(2000, 1, 5, 1),
      DateTime(2000, 1, 6, 1),
      DateTime(2000, 1, 7, 1),
      DateTime(2000, 1, 8, 1),
      DateTime(2000, 1, 9, 1)
    ].map((day) => formatter.format(day)).toList();
  }

  static String weekday(int weekday) =>
      Localize._getWeekDays(Localize.langCode)[(weekday - 1) % 7];

  Future<bool> load() async {
    // Load the language JSON file from the "lang" folder
    String jsonString =
        await rootBundle.loadString('lang/${locale.languageCode}.json');
    Map<String, dynamic> jsonMap = json.decode(jsonString);

    _localizedStrings = jsonMap.map((key, value) {
      return MapEntry(key, value.toString());
    });

    return true;
  }

  // This method will be called from every widget which needs a localized text
  String? translate(String key) {
    if (_localizedStrings[key] == null) return key;
    return _localizedStrings[key];
  }

  // Static member to have a simple access to the delegate from the MaterialApp
  static const LocalizationsDelegate<Localize> delegate = _LocalizeDelegate();
}

// LocalizationsDelegate is a factory for a set of localized resources
// In this case, the localized strings will be gotten in an Localize object
class _LocalizeDelegate extends LocalizationsDelegate<Localize> {
  // This delegate instance will never change (it doesn't even have fields!)
  // It can provide a constant constructor.
  const _LocalizeDelegate();

  @override
  bool isSupported(Locale locale) {
    // Include all of your supported language codes here
    return ['en', 'es', 'sk'].contains(locale.languageCode);
  }

  @override
  Future<Localize> load(Locale locale) async {
    // Localize class is where the JSON loading actually runs
    Localize localizations = new Localize(locale);
    await localizations.load();
    return localizations;
  }

  @override
  bool shouldReload(_LocalizeDelegate old) => false;
}
