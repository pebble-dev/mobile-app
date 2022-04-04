// @dart=2.9
import 'dart:async';

import 'package:cobble/domain/connection/pair_provider.dart' as pair_provider;
import 'package:cobble/domain/connection/scan_provider.dart' as scan_provider;
import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/setup/pair_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';
import 'package:mockito/mockito.dart';

final device = PebbleScanDevice(
  'Test',
  1,
  'v1',
  'asdasdsd',
  0,
  true,
  true,
);

class ScanCallbacks extends scan_provider.ScanCallbacks {
  void startScanning() {
    this.state = scan_provider.ScanState(true, state.devices);
  }

  void stopScanning() {
    this.state = scan_provider.ScanState(false, state.devices);
  }

  void updateDevices(int length) {
    this.state = scan_provider.ScanState(
      state.scanning,
      List.generate(length, (index) => device),
    );
  }
}

class Observer extends Mock implements NavigatorObserver {
  // @override
  // void didPush(Route route, Route previousRoute) {
  //   print(route);
  // }
}

Widget wrapper(
        {ScanCallbacks scanMock,
        StreamProvider<int> pairMock,
        Observer navigatorObserver}) =>
    ProviderScope(
      overrides: [
        scan_provider.scanProvider.overrideWithValue(
          scanMock ?? ScanCallbacks(),
        ),
        pair_provider.pairProvider.overrideWithProvider(
          pairMock ??
              StreamProvider<int>((ref) async* {
                yield null;
              }),
        )
      ],
      child: MaterialApp(
        navigatorObservers: [if (navigatorObserver != null) navigatorObserver],
        home: PairPage.fromLanding(),
        routes: {
          '/moresetup': (_) => Container(),
        },
      ),
    );

void main() {
  group('PairPage', () {
    testWidgets('should work', (tester) async {
      await tester.pumpWidget(
        wrapper(),
      );
    });
    testWidgets('shouldn\'t display loader by default', (tester) async {
      await tester.pumpWidget(
        wrapper(),
      );
      expect(find.byType(CircularProgressIndicator), findsNothing);
    });
    testWidgets('should display loader when scan starts', (tester) async {
      final mock = ScanCallbacks();

      await tester.pumpWidget(wrapper(scanMock: mock));
      mock.startScanning();
      await tester.pump();
      expect(find.byType(CircularProgressIndicator), findsOneWidget);
    });
    testWidgets('should hide loader when scan stops', (tester) async {
      final mock = ScanCallbacks();

      mock.startScanning();
      await tester.pumpWidget(wrapper(scanMock: mock));
      expect(find.byType(CircularProgressIndicator), findsOneWidget);

      mock.stopScanning();
      await tester.pump();
      expect(find.byType(CircularProgressIndicator), findsNothing);
    });
    testWidgets('should display devices if there are any', (tester) async {
      final mock = ScanCallbacks();
      mock.updateDevices(3);

      await tester.pumpWidget(wrapper(scanMock: mock));
      expect(find.byType(PebbleWatchIcon), findsNWidgets(3));
    });
    testWidgets('should update devices', (tester) async {
      final mock = ScanCallbacks();
      mock.updateDevices(3);

      await tester.pumpWidget(wrapper(scanMock: mock));
      mock.updateDevices(5);
      await tester.pump();
      expect(find.byType(PebbleWatchIcon), findsNWidgets(5));
    });
    testWidgets('should respond to paired device', (tester) async {
      final scan = ScanCallbacks();
      final StreamController<int> pairStream = StreamController.broadcast();
      final pair = StreamProvider<int>((ref) => pairStream.stream);
      final observer = Observer();
      scan.updateDevices(1);

      await tester.pumpWidget(wrapper(
        scanMock: scan,
        pairMock: pair,
        navigatorObserver: observer,
      ));
      pairStream.add(device.address);
      await tester.pump();
      verify(observer.didPush(any, any)).called(1);
      pairStream.close();
    });
  });
}
