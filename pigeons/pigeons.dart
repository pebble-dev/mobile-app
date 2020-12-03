import 'package:pigeon/pigeon.dart';

/// Pigeon only supports classes as return/receive type.
/// That is why we must wrap primitive types into wrapper
class BooleanWrapper {
  bool value;
}

class NumberWrapper {
  int value;
}

class StringWrapper {
  String value;
}

class ListWrapper {
  List value;
}

class WatchConnectionState {
  bool isConnected;
  bool isConnecting;
  int currentWatchAddress;
}

class TimelinePinPigeon {
  String itemId;
  String parentId;
  int timestamp;
  int type;
  int duration;
  bool isVisible;
  bool isFloating;
  bool isAllDay;
  bool persistQuickView;
  int layout;
  String attributesJson;
  String actionsJson;
}

@FlutterApi()
abstract class ScanCallbacks {
  void onScanUpdate(ListWrapper pebbles);

  void onScanStarted();

  void onScanStopped();
}

@FlutterApi()
abstract class ConnectionCallbacks {
  void onWatchConnectionStateChanged(WatchConnectionState newState);
}

@FlutterApi()
abstract class PairCallbacks {
  void onWatchPairComplete(NumberWrapper address);
}

@HostApi()
abstract class ScanControl {
  void startBleScan();

  void startClassicScan();
}

@HostApi()
abstract class ConnectionControl {
  BooleanWrapper isConnected();

  void connectToWatch(NumberWrapper macAddress);

  void disconnect();

  void sendRawPacket(ListWrapper listOfBytes);
}

@HostApi()
abstract class NotificationsControl {
  void sendTestNotification();
}

@HostApi()
abstract class AppLifecycleControl {
  BooleanWrapper waitForBoot();
}

@HostApi()
abstract class DebugControl {
  void collectLogs();
}

@HostApi()
abstract class TimelineControl {
  NumberWrapper addPin(TimelinePinPigeon pin);

  NumberWrapper removePin(StringWrapper pinUuid);

  NumberWrapper removeAllPins();
}