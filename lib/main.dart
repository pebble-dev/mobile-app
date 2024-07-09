
import 'dart:async';
import 'dart:ui';

import 'package:cobble/background/main_background.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundReceiver.dart';
import 'package:cobble/infrastructure/backgroundcomm/BackgroundRpc.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
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
import 'package:noob/noob.dart';

import 'domain/permissions.dart';
import 'infrastructure/datasources/paired_storage.dart';
import 'infrastructure/pigeons/pigeons.g.dart';
import 'package:logging/logging.dart';

const String bootUrl = "https://boot.rebble.io/api";

void main() {
  if (kDebugMode) {
    Logger.root.level = Level.FINER;
  }

  Logger.root.onRecord.listen((record) {
    debugPrint('${record.time} [${record.loggerName}] ${record.message}');
    if (record.error != null) {
      debugPrint(record.error.toString());
    }
  });

  /*if (kDebugMode) {
    TrackingBuildOwnerWidgetsFlutterBinding.ensureInitialized();

    // initialize `BuildTracker`
    final tracker = BuildTracker(printBuildFrameIncludeRebuildDirtyWidget: false);

    // print top 10 stacks leading to rebuilds every 10 seconds
    Timer.periodic(const Duration(seconds: 10), (_) => tracker.printTopScheduleBuildForStacks());
  }*/

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
