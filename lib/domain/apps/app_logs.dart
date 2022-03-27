import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class AppLogReceiver extends StateNotifier<List<AppLogEntry>>
    implements AppLogCallbacks {
  final _control = AppLogControl();

  AppLogReceiver() : super(List.empty()) {
    AppLogCallbacks.setup(this);

    _control.startSendingLogs();
  }

  void close() {
    clear();
    _control.stopSendingLogs();
    AppLogCallbacks.setup(null);
  }

  @override
  void onLogReceived(AppLogEntry arg) {
    state = [...state, arg];
  }

  void clear() {
    state = List.empty();
  }
}

final recievedLogsProvider = StateNotifierProvider.autoDispose<AppLogReceiver, dynamic>((ref) {
  final receiver = AppLogReceiver();

  ref.onDispose(() {
    receiver.close();
  });

  return receiver;
});
