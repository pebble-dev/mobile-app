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

class PebbleFirmwarePigeon {
  int timestamp;
  String version;
  String gitHash;
  bool isRecovery;
  int hardwarePlatform;
  int metadataVersion;
}

class PebbleDevicePigeon {
  String name;
  int address;
  PebbleFirmwarePigeon runningFirmware;
  PebbleFirmwarePigeon recoveryFirmware;
  int model;
  int bootloaderTimestamp;
  String board;
  String serial;
  String language;
  int languageVersion;
  bool isUnfaithful;
}

class PebbleScanDevicePigeon {
  String name;
  int address;
  String version;
  String serialNumber;
  int color;
  bool runningPRF;
  bool firstUse;
}

class WatchConnectionStatePigeon {
  bool isConnected;
  bool isConnecting;
  int currentWatchAddress;
  PebbleDevicePigeon currentConnectedWatch;
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
  /// pebbles = list of PebbleScanDevicePigeon
  void onScanUpdate(ListWrapper pebbles);

  void onScanStarted();

  void onScanStopped();
}

@FlutterApi()
abstract class ConnectionCallbacks {
  void onWatchConnectionStateChanged(WatchConnectionStatePigeon newState);
}

@FlutterApi()
abstract class PairCallbacks {
  void onWatchPairComplete(NumberWrapper address);
}

@FlutterApi()
abstract class CalendarCallbacks {
  @async
  void doFullCalendarSync() async {}
}

@HostApi()
abstract class ScanControl {
  void startBleScan();

  void startClassicScan();
}

@HostApi()
abstract class ConnectionControl {
  BooleanWrapper isConnected();

  void disconnect();

  void sendRawPacket(ListWrapper listOfBytes);

  void observeConnectionChanges();

  void cancelObservingConnectionChanges();
}

/// Connection methods that require UI reside in separate pigeon class.
/// This allows easier separation between background and UI methods.
@HostApi()
abstract class UiConnectionControl {
  void connectToWatch(NumberWrapper macAddress);
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

@HostApi()
abstract class BackgroundSetupControl {
  void setupBackground(NumberWrapper callbackHandle);
}

@HostApi()
abstract class BackgroundControl {
  void notifyFlutterBackgroundStarted();
}

@HostApi()
abstract class PermissionCheck {
  BooleanWrapper hasLocationPermission();

  BooleanWrapper hasCalendarPermission();

  BooleanWrapper hasNotificationAccess();

  BooleanWrapper hasBatteryExclusionEnabled();
}

@HostApi()
abstract class PermissionControl {
  // All NumberWrapper reqeust* callbacks return:
  // 0 - permission granted
  // 1 - permission denied by user
  // 2 - permission was denied by user and "Don't show again" was selected.
  //     If we want to request permission again,
  //     we must direct user to the settings (use openPermissionSettings())
  //     This might be Android-specific behavior.

  NumberWrapper requestLocationPermission();

  NumberWrapper requestCalendarPermission();

  /// This can only be performed when at least one watch is paired
  void requestNotificationAccess();

  /// This can only be performed when at least one watch is paired
  void requestBatteryExclusion();

  void openPermissionSettings();
}

/// This class will keep all classes that appear in lists from being deleted
/// by pigeon (they are not kept by default because pigeon does not support
/// generics in lists).
@HostApi()
abstract class KeepUnusedHack {
  void keepPebbleScanDevicePigeon(PebbleScanDevicePigeon cls);
}
