import 'package:hooks_riverpod/all.dart';

typedef DateTimeProvider = DateTime Function();

final currentDateTimeProvider =
    Provider<DateTimeProvider>((ref) => (() => DateTime.now()));
