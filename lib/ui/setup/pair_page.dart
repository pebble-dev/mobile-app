import 'package:cobble/domain/connection/connection_state_provider.dart';
import 'package:cobble/domain/connection/scan_provider.dart';
import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/datasources/paired_storage.dart';
import 'package:cobble/infrastructure/datasources/preferences.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:cobble/localization/localization.dart';
import 'package:cobble/ui/common/components/cobble_button.dart';
import 'package:cobble/ui/common/icons/fonts/rebble_icons.dart';
import 'package:cobble/ui/common/icons/watch_icon.dart';
import 'package:cobble/ui/router/cobble_navigator.dart';
import 'package:cobble/ui/router/cobble_scaffold.dart';
import 'package:cobble/ui/router/cobble_screen.dart';
import 'package:cobble/ui/screens/update_prompt.dart';
import 'package:cobble/ui/setup/boot/rebble_setup.dart';
import 'package:cobble/ui/setup/more_setup.dart';
import 'package:collection/collection.dart' show IterableExtension;
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

final ConnectionControl connectionControl = ConnectionControl();
final UiConnectionControl uiConnectionControl = UiConnectionControl();
final ScanControl scanControl = ScanControl();

class PairPage extends HookConsumerWidget implements CobbleScreen {
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
  Widget build(BuildContext context, WidgetRef ref) {
    final pairedStorage = ref.watch(pairedStorageProvider.notifier);
    final scan = ref.watch(scanProvider);
    //final pair = ref.watch(pairProvider).value;
    final preferences = ref.watch(preferencesProvider);
    final connectionState = useProvider(connectionStateProvider.state);

    useEffect(() {
      if (/*pair == null*/ connectionState.isConnected != true || connectionState.currentConnectedWatch?.address == null || scan.devices.isEmpty) return null;

      /*PebbleScanDevice? dev = scan.devices.firstWhereOrNull(
        (element) => element.address == pair,
      );*/

      PebbleScanDevice? dev = scan.devices.firstWhereOrNull(
        (element) => element.address == connectionState.currentConnectedWatch?.address
      );

      if (dev == null) return null;

      if (connectionState.currentConnectedWatch?.address != dev.address) {
        return null;
      }

      preferences.data?.value.setHasBeenConnected();

      WidgetsBinding.instance.scheduleFrameCallback((timeStamp) {
        pairedStorage.register(dev);
        pairedStorage.setDefault(dev.address!);
        if (fromLanding) {
          context.pushAndRemoveAllBelow(UpdatePrompt(popOnSuccess: true))
              .then((value) => context.pushReplacement(MoreSetup()));
        } else {
          context.pushAndRemoveAllBelow(UpdatePrompt(popOnSuccess: false));
        }
      });

      return null;
    }, [scan, /*pair,*/ connectionState]);

    useEffect(() {
      scanControl.startBleScan();
      return null;
    }, []);

    _refreshDevicesBle() {
      if (!scan.scanning) {
        ref.refresh(scanProvider.notifier).onScanStarted();
        scanControl.startBleScan();
      }
    }

    _refreshDevicesClassic() {
      if (!scan.scanning) {
        ref.refresh(scanProvider.notifier).onScanStarted();
        scanControl.startClassicScan();
      }
    }

    _targetPebble(PebbleScanDevice dev) async {
      StringWrapper addressWrapper = StringWrapper();
      addressWrapper.value = dev.address;
      await uiConnectionControl.connectToWatch(addressWrapper);
      preferences.value?.setHasBeenConnected();
    }

    final title = tr.pairPage.title;
    final body = ListView(
      children: [
        if (scan.scanning)
          const Padding(
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
                      PebbleWatchIcon(
                        PebbleWatchModel.values[e.color ?? 0],
                        size: 56,
                      ),
                      const SizedBox(width: 16),
                      Column(
                        children: <Widget>[
                          Text(
                            e.name!,
                            style: TextStyle(fontSize: 16),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            e.version ?? "",
                          ),
                          Text(connectionState.isConnected == true ? "Connected" : "Not connected"),
                          Wrap(
                            spacing: 4,
                            children: [
                              if (e.runningPRF == true && e.firstUse == false)
                                Chip(
                                  backgroundColor: Colors.deepOrange,
                                  label: Text(tr.pairPage.status.recovery),
                                ),
                              if (e.firstUse == true)
                                Chip(
                                  backgroundColor: const Color(0xffd4af37),
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
                      if (e.address == connectionState.currentConnectedWatch?.address &&
                          connectionState.isConnecting == true)
                        const CircularProgressIndicator()
                      else
                        Icon(RebbleIcons.caret_right,
                            color: Theme.of(context).colorScheme.secondary),
                    ],
                  ),
                  margin: const EdgeInsets.all(16),
                ),
                onTap: () {
                  if (connectionState.isConnected == true || connectionState.isConnecting == true) {
                    return;
                  }
                  _targetPebble(e);
                },
              ),
            )
            .toList(),
        if (!scan.scanning) ...[
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 32.0),
            child: CobbleButton(
              outlined: false,
              label: tr.pairPage.searchAgain.ble,
              onPressed: connectionState.isConnected == true || connectionState.isConnecting == true ? null : _refreshDevicesBle,
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 32.0),
            child: CobbleButton(
              outlined: false,
              label: tr.pairPage.searchAgain.classic,
              onPressed: connectionState.isConnected == true || connectionState.isConnecting == true ? null : _refreshDevicesClassic,
            ),
          ),
        ],
        if (fromLanding)
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 32.0),
            child: CobbleButton(
              outlined: false,
              label: tr.common.skip,
              onPressed:
                connectionState.isConnected == true || connectionState.isConnecting == true ? null : () {
                  context.pushReplacement(RebbleSetup());
                },
            ),
          )
      ],
    );
    return WillPopScope(
      child:fromLanding ?
        CobbleScaffold.page(
          title: title,
          child: body,
        ) :
        CobbleScaffold.tab(
        title: title,
        child: body,
        ),
      onWillPop: () async {
        if (connectionState.isConnecting == true) {
          return false;
        }
        return true;
      }
    );
  }
}
