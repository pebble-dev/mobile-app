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
