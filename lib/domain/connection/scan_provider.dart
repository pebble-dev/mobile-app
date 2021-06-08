import 'package:cobble/domain/entities/pebble_scan_device.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart' as pigeon;
import 'package:hooks_riverpod/hooks_riverpod.dart';

/// Stores state of current scan operation. Devices can be empty array but
/// will never be null.
class ScanState {
  final bool scanning;
  final List<PebbleScanDevice> devices;

  ScanState(this.scanning, this.devices);
}

class ScanCallbacks extends StateNotifier<ScanState>
    implements pigeon.ScanCallbacks {
  ScanCallbacks()
      : super(
          ScanState(
            false,
            [],
          ),
        );

  @override
  void onScanStarted() {
    state = ScanState(true, state.devices);
  }

  @override
  void onScanStopped() {
    state = ScanState(false, state.devices);
  }

  @override
  void onScanUpdate(pigeon.ListWrapper arg) {
    final devices = (arg.value!.cast<Map>())
        .map((element) => PebbleScanDevice.fromMap(element))
        .toList();
    state = ScanState(state.scanning, devices);
  }
}

final scanProvider = StateNotifierProvider<ScanCallbacks, ScanState>((ref) {
  final notifier = ScanCallbacks();
  pigeon.ScanCallbacks.setup(notifier);
  ref.onDispose(() {
    pigeon.ScanCallbacks.setup(null);
  });

  return notifier;
});
