enum TimelinePinType { notification, pin, reminder }

extension ProtocolExtension on TimelinePinType? {
  int toProtocolNumber() {
    switch (this) {
      case TimelinePinType.notification:
        return 1;
      case TimelinePinType.pin:
        return 2;
      case TimelinePinType.reminder:
      default:
        throw Exception("Unknown pin type $this");
    }
  }
}
