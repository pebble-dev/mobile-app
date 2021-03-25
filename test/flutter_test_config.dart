import 'dart:async';

import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/theme/cobble_scheme.dart';
import 'package:cobble/ui/theme/cobble_theme.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:golden_toolkit/golden_toolkit.dart';

Future<void> testExecutable(FutureOr<void> Function() testMain) async {
  await loadAppFonts();
  return testMain();
}

Widget wrapper({required Widget child, Brightness? brightness}) {
  final theme = CobbleTheme.appTheme(brightness);
  final scheme = CobbleSchemeData.fromBrightness(brightness);
  return CobbleScheme(
    schemeData: scheme,
    child: MaterialApp(
      debugShowCheckedModeBanner: false,
      theme: theme,
      home: CobbleScaffold.tab(
        child: child,
      ),
    ),
  );
}

extension DeviceBuilderX on DeviceBuilder {
  void addBothScenarios(Widget child) {
    this
      ..addScenario(
        name: 'Dark theme',
        widget: wrapper(
          child: child,
          brightness: Brightness.dark,
        ),
      )
      ..addScenario(
        name: 'Light theme',
        widget: wrapper(
          child: child,
          brightness: Brightness.light,
        ),
      );
  }
}

void testUi(String name, Widget child, [List<Device>? devices]) {
  devices ??= [
    Device.phone,
  ];
  testGoldens(name, (WidgetTester tester) async {
    final devicesBuilder = DeviceBuilder()
      ..overrideDevicesForAllScenarios(devices: devices!)
      ..addBothScenarios(
        child,
      );
    await tester.pumpDeviceBuilder(devicesBuilder);
    await screenMatchesGolden(tester, name);
  });
}
