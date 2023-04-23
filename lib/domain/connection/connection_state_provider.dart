import 'package:cobble/domain/entities/pebble_device.dart';
import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class WatchConnectionState {
  final bool? isConnected;
  final bool? isConnecting;
  final String? currentWatchAddress;
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

  @override
  void dispose() {
    ConnectionCallbacks.setup(null);
    _connectionControl.cancelObservingConnectionChanges();
    super.dispose();
  }
}

final AutoDisposeStateNotifierProvider<ConnectionCallbacksStateNotifier, WatchConnectionState>
    connectionStateProvider =
    StateNotifierProvider.autoDispose<ConnectionCallbacksStateNotifier, WatchConnectionState>((ref) {
  final notifier = ConnectionCallbacksStateNotifier();
  ref.onDispose(notifier.dispose);
  return notifier;
});
