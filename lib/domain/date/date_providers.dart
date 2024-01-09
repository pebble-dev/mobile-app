import 'package:hooks_riverpod/hooks_riverpod.dart';

typedef DateTimeProvider = DateTime Function();

final currentDateTimeProvider =
    Provider<DateTimeProvider>((ref) => (() => DateTime.now()));
