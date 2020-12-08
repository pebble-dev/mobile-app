import 'package:cobble/infrastructure/pigeons/pigeons.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class ConnectionCallbacksStateNotifier
    extends StateNotifier<WatchConnectionState> implements ConnectionCallbacks {
  final _connectionControl = ConnectionControl();

  ConnectionCallbacksStateNotifier() : super(WatchConnectionState()) {
    ConnectionCallbacks.setup(this);
    _connectionControl.observeConnectionChanges();
  }

  @override
  void onWatchConnectionStateChanged(WatchConnectionState arg) {
    state = arg;
  }

  void dispose() {
    ConnectionCallbacks.setup(null);
    _connectionControl.cancelObservingConnectionChanges();
  }
}

final connectionStateProvider =
    StateNotifierProvider.autoDispose<ConnectionCallbacksStateNotifier>((ref) {
  final notifier = ConnectionCallbacksStateNotifier();
  ref.onDispose(notifier.dispose);
  return notifier;
});
