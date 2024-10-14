import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:collection/collection.dart';
import 'package:hooks_riverpod/hooks_riverpod.dart';

class CalendarListStateNotifier extends StateNotifier<List<CalendarPigeon>> implements CalendarCallbacks{
  final _calendarControl = CalendarControl();

  CalendarListStateNotifier() : super([]) {
    CalendarCallbacks.setup(this);
    _calendarControl.getCalendars().then((value) {
      state = value.whereNotNull().toList();
    });
  }

  @override
  void dispose() {
    if (mounted) {
      super.dispose();
    }
  }

  @override
  void onCalendarListUpdated(List<CalendarPigeon?> calendars) {
    state = calendars.whereNotNull().toList();
  }
}

final AutoDisposeStateNotifierProvider<CalendarListStateNotifier, List<CalendarPigeon>> calendarListProvider = StateNotifierProvider.autoDispose<CalendarListStateNotifier, List<CalendarPigeon>>((ref) {
  final notifier = CalendarListStateNotifier();
  ref.onDispose(() {
    if (notifier.mounted) {
      notifier.dispose();
    }
  });
  return notifier;
});