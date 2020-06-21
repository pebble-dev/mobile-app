import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:fossil/ui/Localize.dart';
import 'package:fossil/ui/Router.dart';
import 'package:fossil/ui/Theme.dart';

String getBootUrl = "https://boot.rebble.io/";

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Fossil',
      theme: RebbleTheme.appTheme,
      //home: SplashPage(),
      initialRoute: '/',
      onGenerateRoute: Router.generateRoute,
      // List all of the app's supported locales here
      supportedLocales: [
        Locale('en'),
        Locale('es'),
      ],
      // These delegates make sure that the localization data for the proper language is loaded
      localizationsDelegates: [
        // A class which loads the translations from JSON files
        Localize.delegate,
        // Built-in localization of basic text for Material widgets
        GlobalMaterialLocalizations.delegate,
        // Built-in localization for text direction LTR/RTL
        GlobalWidgetsLocalizations.delegate,
      ],
      // Returns a locale which will be used by the app
      localeResolutionCallback: (locale, supportedLocales) {
        var retLocale = supportedLocales.first;
        // Check if the current device locale is supported
        if (locale != null)
          for (var supportedLocale in supportedLocales)
            if (supportedLocale.languageCode == locale.languageCode) {
              retLocale = supportedLocale;
              if (supportedLocale.countryCode == locale.countryCode) break;
            }
        return retLocale;
      },
    );
  }
}
