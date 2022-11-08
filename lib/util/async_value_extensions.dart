import 'package:hooks_riverpod/hooks_riverpod.dart';

extension AsyncValueExtension<T> on AsyncValue<T> {
  T? resultOrThrow() {
    if (this is AsyncData<T>) {
      return (this as AsyncData<T>).value;
    } else if (this is AsyncError<T>) {
      throw (this as AsyncError<T>).error;
    } else {
      throw Exception("Value is still loading");
    }
  }
}
