import 'dart:async';

import 'package:cobble/infrastructure/pigeons/pigeons.g.dart' as pigeon;
import 'package:hooks_riverpod/hooks_riverpod.dart';

class PairCallbacks implements pigeon.PairCallbacks {
  final StreamController<int?> streamController;

  PairCallbacks(this.streamController);

  @override
  void onWatchPairComplete(pigeon.NumberWrapper arg) {
    this.streamController.add(arg.value);
  }
}

/// Stores the address of device you are paired to. Can be null.
final pairProvider = StreamProvider<int?>((ref) {
  final StreamController<int> streamController = StreamController.broadcast();
  ref.onDispose(() {
    streamController.close();
  });

  pigeon.PairCallbacks.setup(PairCallbacks(streamController));
  ref.onDispose(() {
    pigeon.PairCallbacks.setup(null);
  });

  return streamController.stream;
});
