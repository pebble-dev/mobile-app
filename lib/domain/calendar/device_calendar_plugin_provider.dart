import 'package:cobble/infrastructure/pigeons/pigeons.g.dart';
import 'package:device_calendar/device_calendar.dart';
import 'package:hooks_riverpod/all.dart';

final deviceCalendarPluginProvider = Provider((ref) => DeviceCalendarPlugin());

final calendarControlProvider = Provider((ref) => CalendarControl());
