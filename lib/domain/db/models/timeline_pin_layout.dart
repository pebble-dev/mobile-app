enum TimelinePinLayout {
  genericPin,
  calendarPin,
  genericReminder,
  genericNotification,
  commNotification,
  weatherPin,
  sportsPin
}

extension ProtocolExtension on TimelinePinLayout {
  int toProtocolNumber() {
    switch (this) {
      case TimelinePinLayout.genericPin:
        return 1;
      case TimelinePinLayout.calendarPin:
        return 2;
      case TimelinePinLayout.genericReminder:
        return 3;
      case TimelinePinLayout.genericNotification:
        return 4;
      case TimelinePinLayout.commNotification:
        return 5;
      case TimelinePinLayout.weatherPin:
        return 6;
      case TimelinePinLayout.sportsPin:
        return 7;
      default:
        throw Exception("Unknown layout $this");
    }
  }
}
