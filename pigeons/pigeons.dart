import 'package:pigeon/pigeon.dart';

class ListOfPebbleDevices {
  List list;
}

class SearchRequest {
  String query;
}

class SearchReply {
  String result;
}

@FlutterApi()
abstract class ScanCallbacks {
  void onScanUpdate(ListOfPebbleDevices pebbles);

  void onScanStarted();

  void onScanStopped();
}

@HostApi()
abstract class ScanControl {
  void startScan();
}

/// Pigeon only supports classes as return/receive type.
/// That is why we must wrap primitive types into wrapper
class BooleanWrapper {
  bool value;
}

class NumberWrapper {
  int value;
}

class ListWrapper {
  List value;
}

@HostApi()
abstract class ConnectionControl {
  BooleanWrapper isConnected();

  void connectToWatch(NumberWrapper macAddress);

  void sendRawPacket(ListWrapper listOfBytes);
}

@HostApi()
abstract class Notifications {
  void sendTestNotification();
}