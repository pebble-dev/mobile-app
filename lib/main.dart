import 'dart:ui';

import 'package:cobble/ui/localize.dart';
import 'package:cobble/ui/router.dart' as router;
import 'package:cobble/ui/theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:hooks_riverpod/all.dart';

import 'domain/calendar/calendar_permission.dart';

String getBootUrl = "https://boot.rebble.io/";

void main() {
  runApp(ProviderScope(child: MyApp()));
}

class MyApp extends HookWidget {
  @override
  Widget build(BuildContext context) {
    final CalendarPermission calendarPermission =
        useProvider(calendarPermissionProvider);

    useEffect(() {
      calendarPermission.requestPermission();
      return null;
    }, ["one-time"]);

    return MaterialApp(
      title: 'Cobble',
      theme: RebbleTheme.appTheme,
      //home: SplashPage(),
      initialRoute: '/',
      onGenerateRoute: router.Router.generateRoute,
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
