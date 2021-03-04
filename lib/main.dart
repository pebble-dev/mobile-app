import 'dart:ui';

import 'package:cobble/background/main_background.dart';
import 'package:cobble/ui/localize.dart';
import 'package:cobble/ui/splash/splash_page.dart';
import 'package:cobble/ui/theme/cobble_scheme.dart';
import 'package:cobble/ui/theme/cobble_theme.dart';
import 'package:cobble/ui/theme/use_platform_brightness.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:hooks_riverpod/all.dart';

import 'domain/permissions.dart';
import 'infrastructure/datasources/paired_storage.dart';
import 'infrastructure/pigeons/pigeons.g.dart';

String getBootUrl = "https://boot.rebble.io/";

void main() {
  runApp(ProviderScope(child: MyApp()));
  initBackground();
}

void initBackground() {
  final CallbackHandle backgroundCallbackHandle =
      PluginUtilities.getCallbackHandle(main_background);
  final wrapper = NumberWrapper();
  wrapper.value = backgroundCallbackHandle.toRawHandle();
  BackgroundSetupControl().setupBackground(wrapper);
}

class MyApp extends HookWidget {
  @override
  Widget build(BuildContext context) {
    final permissionControl = useProvider(permissionControlProvider);
    final permissionCheck = useProvider(permissionCheckProvider);
    final defaultWatch = useProvider(defaultWatchProvider);

    useEffect(() {
      Future.microtask(() async {
        if (!(await permissionCheck.hasCalendarPermission()).value) {
          await permissionControl.requestCalendarPermission();
        }
        if (!(await permissionCheck.hasLocationPermission()).value) {
          await permissionControl.requestLocationPermission();
        }

        if (defaultWatch != null) {
          if (!(await permissionCheck.hasNotificationAccess()).value) {
            permissionControl.requestNotificationAccess();
          }

          if (!(await permissionCheck.hasBatteryExclusionEnabled()).value) {
            permissionControl.requestBatteryExclusion();
          }
        }
      });
      return null;
    }, ["one-time"]);

    final brightness = usePlatformBrightness();

    return CobbleScheme(
      schemeData: CobbleSchemeData.fromBrightness(brightness),
      child: MaterialApp(
        title: 'Cobble',
        theme: CobbleTheme.appTheme(brightness),
        home: SplashPage(),
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
      ),
    );
  }
}
