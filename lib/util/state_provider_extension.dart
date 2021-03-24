import 'dart:async';

import 'package:hooks_riverpod/all.dart';

extension StateProviderExtension<T> on StateNotifier<T> {
  /// Variant of StateNotifier.stream that also returns existing value
  /// as the first element of the stream
  Stream<T> get streamWithExistingValue {
    late StreamController<T> streamController;
    RemoveListener? removeListener;

    streamController = StreamController(
      onListen: () {
        removeListener = addListener((state) {
          streamController.add(state);
        }, fireImmediately: true);
      },
      onCancel: () {
        removeListener?.call();
      },
    );

    return streamController.stream;
  }
}
