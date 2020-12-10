import 'dart:async';

import 'package:hooks_riverpod/all.dart';

import 'stream_extensions.dart';

extension ContainerExtension on ProviderContainer {
  Future<AsyncValue<T>> readUntilFirstSuccessOrError<T>(
      ProviderBase<Object, AsyncValue<T>> provider) {
    return this.listenStream(provider).firstSuccessOrError();
  }

  /// Listen to the provider as stream
  Stream<T> listenStream<T>(ProviderBase<Object, T> provider) {
    ProviderSubscription<T> subscription;

    // ignore: close_sinks
    StreamController<T> controller;
    controller = StreamController(onListen: () {
      subscription = listen(provider, mayHaveChanged: (sub) {
        controller.add(sub.read());
      });

      controller.add(subscription.read());
    }, onCancel: () {
      subscription?.close();
    });

    return controller.stream;
  }
}
