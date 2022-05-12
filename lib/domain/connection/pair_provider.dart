import 'dart:async';

import 'package:cobble/infrastructure/pigeons/pigeons.g.dart' as pigeon;
import 'package:hooks_riverpod/all.dart';

class PairCallbacks implements pigeon.PairCallbacks {
  final StreamController<String?> streamController;

  PairCallbacks(this.streamController);

  @override
  void onWatchPairComplete(pigeon.StringWrapper arg) {
    this.streamController.add(arg.value);
  }
}

/// Stores the address of device you are paired to. Can be null.
final pairProvider = StreamProvider<String?>((ref) {
  final StreamController<String> streamController = StreamController.broadcast();
  ref.onDispose(() {
    streamController.close();
  });

  pigeon.PairCallbacks.setup(PairCallbacks(streamController));
  ref.onDispose(() {
    pigeon.PairCallbacks.setup(null);
  });

  return streamController.stream;
});
