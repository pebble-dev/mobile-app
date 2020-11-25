enum TimelinePinLayout {
  GENERIC_PIN,
  CALENDAR_PIN,
  GENERIC_REMINDER,
  GENERIC_NOTIFICATION,
  COMM_NOTIFICATION,
  WEATHER_PIN,
  SPORTS_PIN
}

extension ProtocolExtension on TimelinePinLayout {
  int toProtocolNumber() {
    switch (this) {
      case TimelinePinLayout.GENERIC_PIN:
        return 1;
      case TimelinePinLayout.CALENDAR_PIN:
        return 2;
      case TimelinePinLayout.GENERIC_REMINDER:
        return 3;
      case TimelinePinLayout.GENERIC_NOTIFICATION:
        return 4;
      case TimelinePinLayout.COMM_NOTIFICATION:
        return 5;
      case TimelinePinLayout.WEATHER_PIN:
        return 6;
      case TimelinePinLayout.SPORTS_PIN:
        return 7;
      default:
        throw Exception("Unknown layout $this");
    }
  }
}
