import 'package:hooks_riverpod/all.dart';

extension StreamExtension<T> on Stream<AsyncValue<T>?> {
  Future<AsyncValue<T>?> firstSuccessOrError() {
    return firstWhere(
        (element) => element is AsyncData || element is AsyncError,
        orElse: () => null);
  }
}
