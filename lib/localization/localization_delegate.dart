import 'dart:ui';

import 'package:cobble/localization/model/model_generator.dart';
import 'package:flutter/widgets.dart' as widgets;

import 'localization.dart';

/// We use Flutter's localization to tell us which locale should be loaded.
/// This class is pretty standard way to include custom localization:
/// https://flutter.dev/docs/development/accessibility-and-localization/internationalization#adding-your-own-localized-messages
/// What is new is [Localization] class that loads correct `.json` file and
/// [ModelGenerator] that validates `.json` files and generates statically typed
/// model based on their structure.
///
/// NOTE:
/// `.json` files are compared by their language code only! That means only
/// `en.json` is supported, don't use `en-US.json`. If you wish to support
/// country codes at one point, update this class.
///
/// See also:
/// * [ModelGenerator]
/// * [tr]
///
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
