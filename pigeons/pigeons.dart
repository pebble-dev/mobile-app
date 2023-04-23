import 'package:pigeon/pigeon.dart';

/// Pigeon only supports classes as return/receive type.
/// That is why we must wrap primitive types into wrapper
class BooleanWrapper {
  bool? value;
}

class NumberWrapper {
  int? value;
}

class StringWrapper {
  String? value;
}

class ListWrapper {
  List? value;
}

class PebbleFirmwarePigeon {
  int? timestamp;
  String? version;
  String? gitHash;
  bool? isRecovery;
  int? hardwarePlatform;
  int? metadataVersion;
}

class PebbleDevicePigeon {
  String? name;
  String? address;
  PebbleFirmwarePigeon? runningFirmware;
  PebbleFirmwarePigeon? recoveryFirmware;
  int? model;
  int? bootloaderTimestamp;
  String? board;
  String? serial;
  String? language;
  int? languageVersion;
  bool? isUnfaithful;
}

class PebbleScanDevicePigeon {
  String? name;
  String? address;
  String? version;
  String? serialNumber;
  int? color;
  bool? runningPRF;
  bool? firstUse;
}

class WatchConnectionStatePigeon {
  bool isConnected;
  bool isConnecting;
  String? currentWatchAddress;
  PebbleDevicePigeon? currentConnectedWatch;
  WatchConnectionStatePigeon(this.isConnected, this.isConnecting,
      this.currentWatchAddress, this.currentConnectedWatch);
}

class TimelinePinPigeon {
  String? itemId;
  String? parentId;
  int? timestamp;
  int? type;
  int? duration;
  bool? isVisible;
  bool? isFloating;
  bool? isAllDay;
  bool? persistQuickView;
  int? layout;
  String? attributesJson;
  String? actionsJson;
}

class ActionTrigger {
  String? itemId;
  int? actionId;
  String? attributesJson;
}

class ActionResponsePigeon {
  bool? success;
  String? attributesJson;
}

class NotifActionExecuteReq {
  String? itemId;
  int? actionId;
  String? responseText;
}

class NotificationPigeon {
  String? packageId;
  int? notifId;
  String? appName;
  String? tagId;
  String? title;
  String? text;
  String? category;
  int? color;
  String? messagesJson;
  String? actionsJson;
}

class AppEntriesPigeon {
  List<String?>? appName;
  List<String?>? packageId;
}

class PbwAppInfo {
  bool? isValid;
  String? uuid;
  String? shortName;
  String? longName;
  String? companyName;
  int? versionCode;
  String? versionLabel;
  Map<String?, int?>? appKeys;
  List<String?>? capabilities;
  List<WatchResource?>? resources;
  String? sdkVersion;
  List<String?>? targetPlatforms;
  WatchappInfo? watchapp;
}

class WatchappInfo {
  bool? watchface;
  bool? hiddenApp;
  bool? onlyShownOnCommunication;
}

class WatchResource {
  String? file;
  bool? menuIcon;
  String? name;
  String? type;
}

class InstallData {
  String uri;
  PbwAppInfo appInfo;
  bool stayOffloaded;

  InstallData(this.uri, this.appInfo, this.stayOffloaded);
}

class AppInstallStatus {
  /// Progress in range [0-1]
  double progress;
  bool isInstalling;

  AppInstallStatus(this.progress, this.isInstalling);
}

class ScreenshotResult {
  bool success;
  String? imagePath;

  ScreenshotResult(this.success, this.imagePath);
}

class AppLogEntry {
  String uuid;
  int timestamp;
  int level;
  int lineNumber;
  String filename;
  String message;

  AppLogEntry(this.uuid, this.timestamp, this.level, this.lineNumber,
      this.filename, this.message);
}

class OAuthResult {
  String? code;
  String? state;
  String? error;
  OAuthResult(this.code, this.state, this.error);
}

class NotifChannelPigeon {
  String? packageId;
  String? channelId;
  String? channelName;
  String? channelDesc;
  bool? delete;
}

@FlutterApi()
abstract class ScanCallbacks {
  /// pebbles = list of PebbleScanDevicePigeon
  void onScanUpdate(List<PebbleScanDevicePigeon> pebbles);

  void onScanStarted();

  void onScanStopped();
}

@FlutterApi()
abstract class ConnectionCallbacks {
  void onWatchConnectionStateChanged(WatchConnectionStatePigeon newState);
}

@FlutterApi()
abstract class RawIncomingPacketsCallbacks {
  void onPacketReceived(ListWrapper listOfBytes);
}

@FlutterApi()
abstract class PairCallbacks {
  void onWatchPairComplete(StringWrapper address);
}

@FlutterApi()
abstract class CalendarCallbacks {
  @async
  void doFullCalendarSync();
}

@FlutterApi()
abstract class TimelineCallbacks {
  void syncTimelineToWatch();

  @async
  ActionResponsePigeon handleTimelineAction(ActionTrigger actionTrigger);
}

@FlutterApi()
abstract class IntentCallbacks {
  void openUri(StringWrapper uri);
}

@FlutterApi()
abstract class BackgroundAppInstallCallbacks {
  @async
  void beginAppInstall(InstallData installData);

  @async
  void deleteApp(StringWrapper uuid);
}

@FlutterApi()
abstract class AppInstallStatusCallbacks {
  void onStatusUpdated(AppInstallStatus status);
}

@FlutterApi()
abstract class NotificationListening {
  @async
  TimelinePinPigeon handleNotification(NotificationPigeon notification);

  void dismissNotification(StringWrapper itemId);
  @async
  BooleanWrapper shouldNotify(NotifChannelPigeon channel);
  void updateChannel(NotifChannelPigeon channel);
}

@FlutterApi()
abstract class AppLogCallbacks {
  void onLogReceived(AppLogEntry entry);
}

@FlutterApi()
abstract class FirmwareUpdateCallbacks {
  void onFirmwareUpdateStarted();

  void onFirmwareUpdateProgress(double progress);

  void onFirmwareUpdateFinished();
}

@HostApi()
abstract class NotificationUtils {
  @async
  BooleanWrapper dismissNotification(StringWrapper itemId);

  void dismissNotificationWatch(StringWrapper itemId);

  void openNotification(StringWrapper itemId);

  void executeAction(NotifActionExecuteReq action);
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

@HostApi()
abstract class RawIncomingPacketsControl {
  void observeIncomingPackets();

  void cancelObservingIncomingPackets();
}

/// Connection methods that require UI reside in separate pigeon class.
/// This allows easier separation between background and UI methods.
@HostApi()
abstract class UiConnectionControl {
  void connectToWatch(StringWrapper macAddress);

  void unpairWatch(StringWrapper macAddress);
}

@HostApi()
abstract class NotificationsControl {
  void sendTestNotification();
}

@HostApi()
abstract class IntentControl {
  void notifyFlutterReadyForIntents();

  void notifyFlutterNotReadyForIntents();

  @async
  OAuthResult waitForOAuth();
}

@HostApi()
abstract class DebugControl {
  void collectLogs();
}

@HostApi()
abstract class TimelineControl {
  @async
  NumberWrapper addPin(TimelinePinPigeon pin);

  @async
  NumberWrapper removePin(StringWrapper pinUuid);

  @async
  NumberWrapper removeAllPins();
}

@HostApi()
abstract class BackgroundSetupControl {
  void setupBackground(NumberWrapper callbackHandle);
}

@HostApi()
abstract class BackgroundControl {
  @async
  NumberWrapper notifyFlutterBackgroundStarted();
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

  @async
  NumberWrapper requestLocationPermission();

  @async
  NumberWrapper requestCalendarPermission();

  /// This can only be performed when at least one watch is paired
  @async
  void requestNotificationAccess();

  /// This can only be performed when at least one watch is paired
  @async
  void requestBatteryExclusion();

  @async
  NumberWrapper requestBluetoothPermissions();

  @async
  void openPermissionSettings();
}

@HostApi()
abstract class CalendarControl {
  void requestCalendarSync();
}

@HostApi()
abstract class PigeonLogger {
  void v(StringWrapper message);

  void d(StringWrapper message);

  void i(StringWrapper message);

  void w(StringWrapper message);

  void e(StringWrapper message);
}

@HostApi()
abstract class TimelineSyncControl {
  void syncTimelineToWatchLater();
}

@HostApi()
abstract class WorkaroundsControl {
  // List of workaround ID strings that apply to this device
  ListWrapper getNeededWorkarounds();
}

@HostApi()
abstract class AppInstallControl {
  @async
  PbwAppInfo getAppInfo(StringWrapper localPbwUri);

  // Just relay method that triggers copies the app into local storage and
  // begins install on the background flutter side
  @async
  BooleanWrapper beginAppInstall(InstallData installData);

  // Just relay method that deletes app from the local store and triggers
  // deleteApp on background flutter side
  // Return BooleanWrapper as a
  // workaround for https://github.com/flutter/flutter/issues/78536
  @async
  BooleanWrapper beginAppDeletion(StringWrapper uuid);

  /// Read header from pbw file already in Cobble's storage and send it to
  /// BlobDB on the watch
  @async
  NumberWrapper insertAppIntoBlobDb(StringWrapper uuidString);

  @async
  NumberWrapper removeAppFromBlobDb(StringWrapper appUuidString);

  @async
  NumberWrapper removeAllApps();

  void subscribeToAppStatus();

  void unsubscribeFromAppStatus();

  @async
  NumberWrapper sendAppOrderToWatch(ListWrapper uuidStringList);
}

@HostApi()
abstract class AppLifecycleControl {
  @async
  BooleanWrapper openAppOnTheWatch(StringWrapper uuidString);
}

@HostApi()
abstract class PackageDetails {
  AppEntriesPigeon getPackageList();
}

@HostApi()
abstract class ScreenshotsControl {
  @async
  ScreenshotResult takeWatchScreenshot();
}

@HostApi()
abstract class AppLogControl {
  void startSendingLogs();

  void stopSendingLogs();
}

@HostApi()
abstract class FirmwareUpdateControl {
  @async
  BooleanWrapper checkFirmwareCompatible(StringWrapper fwUri);
  @async
  BooleanWrapper beginFirmwareUpdate(StringWrapper fwUri);
}

/// This class will keep all classes that appear in lists from being deleted
/// by pigeon (they are not kept by default because pigeon does not support
/// generics in lists).
@HostApi()
abstract class KeepUnusedHack {
  void keepPebbleScanDevicePigeon(PebbleScanDevicePigeon cls);

  void keepWatchResource(WatchResource cls);
}
