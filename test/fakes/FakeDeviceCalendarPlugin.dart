import 'dart:collection';
import 'dart:ui';

import 'package:device_calendar/device_calendar.dart';

class FakeDeviceCalendarPlugin implements DeviceCalendarPlugin {
  bool reportedHasPermissionsValue = true;
  List<Calendar> reportedCalendars = [];

  @override
  Future<Result<String>> createCalendar(String calendarName,
      {Color calendarColor, String localAccountName}) {
    throw UnimplementedError("Not supported for tests yet");
  }

  @override
  Future<Result<String>> createOrUpdateEvent(Event event) {
    throw UnimplementedError("Not supported for tests yet");
  }

  @override
  Future<Result<bool>> deleteEvent(String calendarId, String eventId) {
    throw UnimplementedError("Not supported for tests yet");
  }

  @override
  Future<Result<bool>> deleteEventInstance(String calendarId, String eventId,
      int startDate, int endDate, bool deleteFollowingInstances) {
    throw UnimplementedError("Not supported for tests yet");
  }

  @override
  Future<Result<bool>> hasPermissions() {
    final result = Result<bool>();
    result.data = reportedHasPermissionsValue;
    return Future.value(result);
  }

  @override
  Future<Result<bool>> requestPermissions() {
    throw UnimplementedError("Not supported for tests yet");
  }

  @override
  Future<Result<UnmodifiableListView<Calendar>>> retrieveCalendars() {
    final result = Result<UnmodifiableListView<Calendar>>();
    result.data = UnmodifiableListView(reportedCalendars);
    return Future.value(result);
  }

  @override
  Future<Result<UnmodifiableListView<Event>>> retrieveEvents(
      String calendarId, RetrieveEventsParams retrieveEventsParams) {
    throw UnimplementedError("Not supported for tests yet");
  }
}
