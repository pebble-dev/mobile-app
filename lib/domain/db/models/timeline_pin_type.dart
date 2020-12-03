enum TimelinePinType { NOTIFICATION, PIN, REMINDER }

extension ProtocolExtension on TimelinePinType {
  int toProtocolNumber() {
    switch (this) {
      case TimelinePinType.NOTIFICATION:
        return 1;
      case TimelinePinType.PIN:
        return 2;
      case TimelinePinType.REMINDER:
      default:
        throw Exception("Unknown pin type $this");
    }
  }
}
