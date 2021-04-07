import 'dart:ui';

import 'package:flutter/widgets.dart' as widgets;

import 'localization.dart';

class CobbleLocalizationDelegate
    extends widgets.LocalizationsDelegate<Localization> {
  final List<String> _supportedLocales;

  CobbleLocalizationDelegate(List<Locale> supportedLocale)
      : _supportedLocales = supportedLocale.map((e) => e.languageCode).toList();

  @override
  bool isSupported(Locale locale) {
    return _supportedLocales.contains(locale.languageCode);
  }

  @override
  Future<Localization> load(Locale locale) => Localization.load(locale);

  @override
  bool shouldReload(CobbleLocalizationDelegate old) => false;
}
