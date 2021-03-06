import 'dart:collection';
import 'dart:ui';

import 'package:device_calendar/device_calendar.dart';

class FakeDeviceCalendarPlugin implements DeviceCalendarPlugin {
  List<Calendar> reportedCalendars = [];
  List<Event> reportedEvents = [];

  @override
  Future<Result<String>> createCalendar(String calendarName,
      {Color? calendarColor, String? localAccountName}) {
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
    throw UnimplementedError("All classes should use PermissionCheck instead");
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
    final filteredEvents = reportedEvents.where(
      (element) =>
          element.calendarId == calendarId &&
          (retrieveEventsParams == null ||
              (retrieveEventsParams.eventIds == null ||
                      retrieveEventsParams.eventIds
                          .contains(element.eventId)) &&
                  (retrieveEventsParams.startDate == null ||
                      element.end
                          .isAtSameMomentAs(retrieveEventsParams.startDate) ||
                      element.end.isAfter(retrieveEventsParams.startDate)) &&
                  (retrieveEventsParams.endDate == null ||
                      element.start
                          .isAtSameMomentAs(retrieveEventsParams.endDate) ||
                      element.start.isBefore(retrieveEventsParams.endDate))),
    );

    final result = Result<UnmodifiableListView<Event>>();
    result.data = UnmodifiableListView(filteredEvents);
    return Future.value(result);
  }
}
