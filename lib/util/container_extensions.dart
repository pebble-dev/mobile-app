import 'dart:async';

import 'package:hooks_riverpod/hooks_riverpod.dart';

import 'stream_extensions.dart';

extension ContainerExtension on ProviderContainer {
  Future<AsyncValue<T>> readUntilFirstSuccessOrError<T>(
      ProviderBase<AsyncValue<T>> provider) {
    return this.listenStream(provider).firstSuccessOrError() as Future<AsyncValue<T>>;
  }

  /// Listen to the provider as stream
  Stream<T> listenStream<T>(ProviderBase<T> provider) {
    ProviderSubscription<T>? subscription;

    // ignore: close_sinks
    late StreamController<T> controller;
    controller = StreamController(onListen: () {
      subscription = listen<T>(provider, (previous, value) {});

      controller.add(subscription!.read());
    }, onCancel: () {
      subscription?.close();
    });

    return controller.stream;
  }
}

extension ProviderReferenceExtension on Ref {
  Future<AsyncValue<T>> readUntilFirstSuccessOrError<T>(
      ProviderBase<AsyncValue<T>> provider) {
    return this.container.listenStream(provider).firstSuccessOrError() as Future<AsyncValue<T>>;
  }
}
