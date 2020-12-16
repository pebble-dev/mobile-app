import 'package:cobble/infrastructure/pigeons/pigeons.dart';

class FakePermissionCheck implements PermissionCheck {
  bool reportedBatteryExclusion = true;
  bool reportedCalendarPermission = true;
  bool reportedLocationPermission = true;
  bool reportedNotificationAccess = true;

  @override
  Future<BooleanWrapper> hasBatteryExclusionEnabled() {
    final wrapper = BooleanWrapper();
    wrapper.value = reportedBatteryExclusion;
    return Future.value(wrapper);
  }

  @override
  Future<BooleanWrapper> hasCalendarPermission() {
    final wrapper = BooleanWrapper();
    wrapper.value = reportedCalendarPermission;
    return Future.value(wrapper);
  }

  @override
  Future<BooleanWrapper> hasLocationPermission() {
    final wrapper = BooleanWrapper();
    wrapper.value = reportedLocationPermission;
    return Future.value(wrapper);
  }

  @override
  Future<BooleanWrapper> hasNotificationAccess() {
    final wrapper = BooleanWrapper();
    wrapper.value = reportedNotificationAccess;
    return Future.value(wrapper);
  }
}
