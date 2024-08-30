
import 'dart:async';
import 'dart:ui';

import 'package:cobble/background/main_background.dart';
import 'package:cobble/domain/logging.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundReceiver.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundRpc.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/datasources/secure_storage.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/localization/localization_delegate.dart';
import 'package:cobble/localization/model/model_generator.model.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/screens/update_prompt.dart';
import 'package:cobble/ui/splash/splash_page.dart';
import 'package:cobble/ui/theme/cobble_scheme.dart';
import 'package:cobble/ui/theme/cobble_theme.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

import 'domain/permissions.dart';
import 'infrastructure/datasources/paired_storage.dart';
import 'infrastructure/pigeons/pigeons.g.dart';
import 'package:logging/logging.dart';

const String bootUrl = "https://boot.rebble.io/api";

void main() {
  if (kDebugMode) {
    Logger.root.level = Level.FINER;
  }

  /*if (kDebugMode) {
    TrackingBuildOwnerWidgetsFlutterBinding.ensureInitialized();

    // initialize `BuildTracker`
    final tracker = BuildTracker(printBuildFrameIncludeRebuildDirtyWidget: false);

    // print top 10 stacks leading to rebuilds every 10 seconds
    Timer.periodic(const Duration(seconds: 10), (_) => tracker.printTopScheduleBuildForStacks());
  }*/

  Logger.root.onRecord.listen((record) { // Makes sure we send logs to native logger so they're stored
    //debugPrint('${record.time} [${record.loggerName}] ${record.message}');

    // I hate that this can't be a switch statement
    if (record.level == Level.SEVERE) {
      Log.e(record.message);
    } else if (record.level == Level.WARNING) {
      Log.w(record.message);
    } else if (record.level == Level.INFO) {
      Log.i(record.message);
    } else if (record.level == Level.FINE) {
      Log.d(record.message);
    } else if (record.level == Level.FINER) {
      Log.v(record.message);
    } else if (record.level == Level.FINEST) {
      Log.v(record.message);
    } else if (record.level == Level.SHOUT) {
      Log.e(record.message);
    }
    if (record.error != null) {
      Log.e(record.error.toString());
    }
  });

  runApp(ProviderScope(child: MyApp()));
  initBackground();
}

void initBackground() {
  final CallbackHandle backgroundCallbackHandle =
      PluginUtilities.getCallbackHandle(main_background)!;
  final wrapper = NumberWrapper();
  wrapper.value = backgroundCallbackHandle.toRawHandle();
  BackgroundSetupControl().setupBackground(wrapper);
}

class MyApp extends HookConsumerWidget {
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final permissionControl = ref.watch(permissionControlProvider);
    final permissionCheck = ref.watch(permissionCheckProvider);
    final defaultWatch = ref.watch(defaultWatchProvider);
    final preferences = ref.watch(preferencesProvider.future);

    useEffect(() {
      Future.microtask(() async {
        if ((await preferences).getBoot()?.isNotEmpty != true) {
          (await preferences).setBoot(bootUrl);
        }

        if (!(await permissionCheck.hasCalendarPermission()).value!) {
          await permissionControl.requestCalendarPermission();
        }
        if (!(await permissionCheck.hasLocationPermission()).value!) {
          await permissionControl.requestLocationPermission();
        }
        await permissionControl.requestBluetoothPermissions();

        if (defaultWatch != null) {
          if (!(await permissionCheck.hasNotificationAccess()).value!) {
            permissionControl.requestNotificationAccess();
          }

          if (!(await permissionCheck.hasBatteryExclusionEnabled()).value!) {
            permissionControl.requestBatteryExclusion();
          }

          if (!(await permissionCheck.hasCallsPermissions()).value!) {
            permissionControl.requestCallsPermissions();
          }
        }

        final token = await ref.read(secureStorageProvider).getToken();
        if (token != null && token.accessToken.isNotEmpty) {
            await KMPApi().updateToken(StringWrapper(value: token.accessToken));
        }
      });
      return null;
    }, ["one-time"]);

    final brightness = usePlatformBrightness();

    return CobbleScheme(
      schemeData: CobbleSchemeData.fromBrightness(brightness),
      child: MaterialApp(
        onGenerateTitle: (context) => tr.common.title,
        theme: CobbleTheme.appTheme(brightness),
        home: SplashPage(),
        // List all of the app's supported locales here
        supportedLocales: supportedLocales,
        // These delegates make sure that the localization data for the proper language is loaded
        localizationsDelegates: [
          // A class which loads the translations from JSON files
          CobbleLocalizationDelegate(supportedLocales),
          // Built-in localization of basic text for Material widgets
          GlobalMaterialLocalizations.delegate,
          // Built-in localization for text direction LTR/RTL
          GlobalWidgetsLocalizations.delegate,
        ],
        localeListResolutionCallback:
            (List<Locale>? locales, Iterable<Locale> supportedLocales) =>
                resolveLocale(locales, supportedLocales),
      ),
    );
  }
}
