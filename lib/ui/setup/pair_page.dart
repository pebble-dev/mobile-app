import 'dart:ui';

import 'package:cobble/domain/connection/pair_provider.dart';
import 'package:cobble/domain/connection/scan_provider.dart';
import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/home/home_page.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/setup/more_setup.dart';
import 'package:collection/collection.dart' show IterableExtension;
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final ConnectionControl connectionControl = ConnectionControl();
final UiConnectionControl uiConnectionControl = UiConnectionControl();
final ScanControl scanControl = ScanControl();

class PairPage extends HookWidget implements CobbleScreen {
  final bool fromLanding;

  const PairPage._({
    Key? key,
    this.fromLanding = false,
  }) : super(key: key);

  factory PairPage.fromLanding({
    Key? key,
  }) =>
      PairPage._(
        fromLanding: true,
        key: key,
      );

  factory PairPage.fromTab({
    Key? key,
  }) =>
      PairPage._(
        fromLanding: false,
        key: key,
      );

  @override
  Widget build(BuildContext context) {
    final pairedStorage = useProvider(pairedStorageProvider);
    final scan = useProvider(scanProvider.state);
    final pair = useProvider(pairProvider).data?.value;

    useEffect(() {
      if (pair == null || scan.devices.isEmpty) return null;

      PebbleScanDevice? dev = scan.devices.firstWhereOrNull(
        (element) => element.address == pair,
      );

      if (dev == null) return null;

      WidgetsBinding.instance!.scheduleFrameCallback((timeStamp) {
        pairedStorage.register(dev);
        if (fromLanding) {
          context.pushReplacement(MoreSetup());
        } else {
          context.pushReplacement(HomePage());
        }
      });

      return null;
    }, [scan, pair]);

    useEffect(() {
      scanControl.startBleScan();
      return null;
    }, []);

    final _refreshDevicesBle = () {
      if (!scan.scanning) {
        context.refresh(scanProvider).onScanStarted();
        scanControl.startBleScan();
      }
    };

    final _refreshDevicesClassic = () {
      if (!scan.scanning) {
        context.refresh(scanProvider).onScanStarted();
        scanControl.startClassicScan();
      }
    };

    final _targetPebble = (PebbleScanDevice dev) {
      NumberWrapper addressWrapper = NumberWrapper();
      addressWrapper.value = dev.address;
      uiConnectionControl.connectToWatch(addressWrapper);
    };

    final title = tr.pairPage.title;
    final body = ListView(
      children: [
        if (scan.scanning)
          Padding(
            padding: EdgeInsets.all(16.0),
            child: UnconstrainedBox(
              child: CircularProgressIndicator(),
            ),
          ),
        ...scan.devices
            .map(
              (e) => InkWell(
                child: Container(
                  child: Row(
                    children: <Widget>[
                      Container(
                        child: Center(
                          child: PebbleWatchIcon(
                            PebbleWatchModel.values[e.color!],
                          ),
                        ),
                        width: 56,
                        height: 56,
                        decoration: BoxDecoration(
                            color: Theme.of(context).dividerColor,
                            shape: BoxShape.circle),
                      ),
                      SizedBox(width: 16),
                      Column(
                        children: <Widget>[
                          Text(
                            e.name!,
                            style: TextStyle(fontSize: 16),
                          ),
                          SizedBox(height: 4),
                          Text(
                            e.address!
                                .toRadixString(16)
                                .padLeft(6, '0')
                                .toUpperCase(),
                          ),
                          Wrap(
                            spacing: 4,
                            children: [
                              if (e.runningPRF! && !e.firstUse!)
                                Chip(
                                  backgroundColor: Colors.deepOrange,
                                  label: Text(tr.pairPage.status.recovery),
                                ),
                              if (e.firstUse!)
                                Chip(
                                  backgroundColor: Color(0xffd4af37),
                                  label: Text(tr.pairPage.status.newDevice),
                                ),
                            ],
                          ),
                        ],
                        crossAxisAlignment: CrossAxisAlignment.start,
                      ),
                      Expanded(
                        child: Container(width: 0.0, height: 0.0),
                      ),
                      Icon(RebbleIcons.caret_right,
                          color: Theme.of(context).colorScheme.secondary),
                    ],
                  ),
                  margin: EdgeInsets.all(16),
                ),
                onTap: () {
                  _targetPebble(e);
                },
              ),
            )
            .toList(),
        if (!scan.scanning) ...[
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 32.0),
            child: CobbleButton(
              outlined: false,
              label: tr.pairPage.searchAgain.ble,
              color: Theme.of(context).accentColor,
              onPressed: _refreshDevicesBle,
            ),
          ),
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 32.0),
            child: CobbleButton(
              outlined: false,
              label: tr.pairPage.searchAgain.classic,
              color: Theme.of(context).accentColor,
              onPressed: _refreshDevicesClassic,
            ),
          ),
        ],
        if (fromLanding)
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 32.0),
            child: CobbleButton(
              outlined: false,
              label: tr.common.skip,
              onPressed: () => context.pushAndRemoveAllBelow(
                HomePage(),
              ),
            ),
          )
      ],
    );

    if (fromLanding)
      return CobbleScaffold.page(
        title: title,
        child: body,
      );
    else
      return CobbleScaffold.tab(
        title: title,
        child: body,
      );
  }
}
