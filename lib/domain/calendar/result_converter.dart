import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';

extension ResultConverter<T> on Result<T> {
  AsyncValue<T> toAsyncValue() {
    if (isSuccess && data != null) {
      return AsyncValue.data(data!);
    } else {
      return AsyncValue.error(errors);
    }
  }
}
