import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class WatchConnectionState {
  final bool? isConnected;
  final bool? isConnecting;
  final int? currentWatchAddress;
  final PebbleDevice? currentConnectedWatch;

  WatchConnectionState(this.isConnected, this.isConnecting,
      this.currentWatchAddress, this.currentConnectedWatch);
}

class ConnectionCallbacksStateNotifier
    extends StateNotifier<WatchConnectionState> implements ConnectionCallbacks {
  final _connectionControl = ConnectionControl();

  ConnectionCallbacksStateNotifier()
      : super(WatchConnectionState(false, false, null, null)) {
    ConnectionCallbacks.setup(this);
    _connectionControl.observeConnectionChanges();
  }

  @override
  void onWatchConnectionStateChanged(WatchConnectionStatePigeon pigeon) {
    state = WatchConnectionState(
        pigeon.isConnected,
        pigeon.isConnecting,
        pigeon.currentWatchAddress,
        PebbleDevice.fromPigeon(pigeon.currentConnectedWatch));
  }

  void dispose() {
    ConnectionCallbacks.setup(null);
    _connectionControl.cancelObservingConnectionChanges();
  }
}

final AutoDisposeStateNotifierProvider<ConnectionCallbacksStateNotifier>? connectionStateProvider =
StateNotifierProvider.autoDispose<ConnectionCallbacksStateNotifier>((ref) {
  final notifier = ConnectionCallbacksStateNotifier();
  ref.onDispose(notifier.dispose);
  return notifier;
});
