import 'package:cobble/infrastructure/pigeons/pigeons.dart';

/// Log class that sends all logs back to native which saves the logs to disk,
/// handles error reporting etc.
class Log {
  static final _pigeonLogger = PigeonLogger();

  static void v(String message) {
    StringWrapper wrapper = StringWrapper();
    wrapper.value = message;

    _pigeonLogger.v(wrapper);
  }

  static void d(String message) {
    StringWrapper wrapper = StringWrapper();
    wrapper.value = message;

    _pigeonLogger.d(wrapper);
  }

  static void i(String message) {
    StringWrapper wrapper = StringWrapper();
    wrapper.value = message;

    _pigeonLogger.i(wrapper);
  }

  static void w(String message) {
    StringWrapper wrapper = StringWrapper();
    wrapper.value = message;

    _pigeonLogger.w(wrapper);
  }

  static void e(String message) {
    StringWrapper wrapper = StringWrapper();
    wrapper.value = message;

    _pigeonLogger.e(wrapper);
  }
}
