import 'package:pigeon/pigeon.dart';

class SearchRequest {
  String query;
}

class SearchReply {
  String result;
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

@FlutterApi()
abstract class ScanCallbacks {
  void onScanUpdate(ListWrapper pebbles);

  void onScanStarted();

  void onScanStopped();
}

@HostApi()
abstract class ScanControl {
  void startScan();
}

@HostApi()
abstract class ConnectionControl {
  BooleanWrapper isConnected();

  void connectToWatch(NumberWrapper macAddress);

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