// Autogenerated from Pigeon (v9.2.5), do not edit directly.
// See also: https://pub.dev/packages/pigeon

#import <Foundation/Foundation.h>

@protocol FlutterBinaryMessenger;
@protocol FlutterMessageCodec;
@class FlutterError;
@class FlutterStandardTypedData;

NS_ASSUME_NONNULL_BEGIN

@class BooleanWrapper;
@class NumberWrapper;
@class StringWrapper;
@class ListWrapper;
@class PebbleFirmwarePigeon;
@class PebbleDevicePigeon;
@class PebbleScanDevicePigeon;
@class WatchConnectionStatePigeon;
@class TimelinePinPigeon;
@class ActionTrigger;
@class ActionResponsePigeon;
@class NotifActionExecuteReq;
@class NotificationPigeon;
@class AppEntriesPigeon;
@class PbwAppInfo;
@class WatchappInfo;
@class WatchResource;
@class InstallData;
@class AppInstallStatus;
@class ScreenshotResult;
@class AppLogEntry;
@class OAuthResult;
@class NotifChannelPigeon;
@class NotifyingPackage;

/// Pigeon only supports classes as return/receive type.
/// That is why we must wrap primitive types into wrapper
@interface BooleanWrapper : NSObject
+ (instancetype)makeWithValue:(nullable NSNumber *)value;
@property(nonatomic, strong, nullable) NSNumber * value;
@end

@interface NumberWrapper : NSObject
+ (instancetype)makeWithValue:(nullable NSNumber *)value;
@property(nonatomic, strong, nullable) NSNumber * value;
@end

@interface StringWrapper : NSObject
+ (instancetype)makeWithValue:(nullable NSString *)value;
@property(nonatomic, copy, nullable) NSString * value;
@end

@interface ListWrapper : NSObject
+ (instancetype)makeWithValue:(nullable NSArray *)value;
@property(nonatomic, strong, nullable) NSArray * value;
@end

@interface PebbleFirmwarePigeon : NSObject
+ (instancetype)makeWithTimestamp:(nullable NSNumber *)timestamp
    version:(nullable NSString *)version
    gitHash:(nullable NSString *)gitHash
    isRecovery:(nullable NSNumber *)isRecovery
    hardwarePlatform:(nullable NSNumber *)hardwarePlatform
    metadataVersion:(nullable NSNumber *)metadataVersion;
@property(nonatomic, strong, nullable) NSNumber * timestamp;
@property(nonatomic, copy, nullable) NSString * version;
@property(nonatomic, copy, nullable) NSString * gitHash;
@property(nonatomic, strong, nullable) NSNumber * isRecovery;
@property(nonatomic, strong, nullable) NSNumber * hardwarePlatform;
@property(nonatomic, strong, nullable) NSNumber * metadataVersion;
@end

@interface PebbleDevicePigeon : NSObject
+ (instancetype)makeWithName:(nullable NSString *)name
    address:(nullable NSString *)address
    runningFirmware:(nullable PebbleFirmwarePigeon *)runningFirmware
    recoveryFirmware:(nullable PebbleFirmwarePigeon *)recoveryFirmware
    model:(nullable NSNumber *)model
    bootloaderTimestamp:(nullable NSNumber *)bootloaderTimestamp
    board:(nullable NSString *)board
    serial:(nullable NSString *)serial
    language:(nullable NSString *)language
    languageVersion:(nullable NSNumber *)languageVersion
    isUnfaithful:(nullable NSNumber *)isUnfaithful;
@property(nonatomic, copy, nullable) NSString * name;
@property(nonatomic, copy, nullable) NSString * address;
@property(nonatomic, strong, nullable) PebbleFirmwarePigeon * runningFirmware;
@property(nonatomic, strong, nullable) PebbleFirmwarePigeon * recoveryFirmware;
@property(nonatomic, strong, nullable) NSNumber * model;
@property(nonatomic, strong, nullable) NSNumber * bootloaderTimestamp;
@property(nonatomic, copy, nullable) NSString * board;
@property(nonatomic, copy, nullable) NSString * serial;
@property(nonatomic, copy, nullable) NSString * language;
@property(nonatomic, strong, nullable) NSNumber * languageVersion;
@property(nonatomic, strong, nullable) NSNumber * isUnfaithful;
@end

@interface PebbleScanDevicePigeon : NSObject
+ (instancetype)makeWithName:(nullable NSString *)name
    address:(nullable NSString *)address
    version:(nullable NSString *)version
    serialNumber:(nullable NSString *)serialNumber
    color:(nullable NSNumber *)color
    runningPRF:(nullable NSNumber *)runningPRF
    firstUse:(nullable NSNumber *)firstUse;
@property(nonatomic, copy, nullable) NSString * name;
@property(nonatomic, copy, nullable) NSString * address;
@property(nonatomic, copy, nullable) NSString * version;
@property(nonatomic, copy, nullable) NSString * serialNumber;
@property(nonatomic, strong, nullable) NSNumber * color;
@property(nonatomic, strong, nullable) NSNumber * runningPRF;
@property(nonatomic, strong, nullable) NSNumber * firstUse;
@end

@interface WatchConnectionStatePigeon : NSObject
/// `init` unavailable to enforce nonnull fields, see the `make` class method.
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)makeWithIsConnected:(NSNumber *)isConnected
    isConnecting:(NSNumber *)isConnecting
    currentWatchAddress:(nullable NSString *)currentWatchAddress
    currentConnectedWatch:(nullable PebbleDevicePigeon *)currentConnectedWatch;
@property(nonatomic, strong) NSNumber * isConnected;
@property(nonatomic, strong) NSNumber * isConnecting;
@property(nonatomic, copy, nullable) NSString * currentWatchAddress;
@property(nonatomic, strong, nullable) PebbleDevicePigeon * currentConnectedWatch;
@end

@interface TimelinePinPigeon : NSObject
+ (instancetype)makeWithItemId:(nullable NSString *)itemId
    parentId:(nullable NSString *)parentId
    timestamp:(nullable NSNumber *)timestamp
    type:(nullable NSNumber *)type
    duration:(nullable NSNumber *)duration
    isVisible:(nullable NSNumber *)isVisible
    isFloating:(nullable NSNumber *)isFloating
    isAllDay:(nullable NSNumber *)isAllDay
    persistQuickView:(nullable NSNumber *)persistQuickView
    layout:(nullable NSNumber *)layout
    attributesJson:(nullable NSString *)attributesJson
    actionsJson:(nullable NSString *)actionsJson;
@property(nonatomic, copy, nullable) NSString * itemId;
@property(nonatomic, copy, nullable) NSString * parentId;
@property(nonatomic, strong, nullable) NSNumber * timestamp;
@property(nonatomic, strong, nullable) NSNumber * type;
@property(nonatomic, strong, nullable) NSNumber * duration;
@property(nonatomic, strong, nullable) NSNumber * isVisible;
@property(nonatomic, strong, nullable) NSNumber * isFloating;
@property(nonatomic, strong, nullable) NSNumber * isAllDay;
@property(nonatomic, strong, nullable) NSNumber * persistQuickView;
@property(nonatomic, strong, nullable) NSNumber * layout;
@property(nonatomic, copy, nullable) NSString * attributesJson;
@property(nonatomic, copy, nullable) NSString * actionsJson;
@end

@interface ActionTrigger : NSObject
+ (instancetype)makeWithItemId:(nullable NSString *)itemId
    actionId:(nullable NSNumber *)actionId
    attributesJson:(nullable NSString *)attributesJson;
@property(nonatomic, copy, nullable) NSString * itemId;
@property(nonatomic, strong, nullable) NSNumber * actionId;
@property(nonatomic, copy, nullable) NSString * attributesJson;
@end

@interface ActionResponsePigeon : NSObject
+ (instancetype)makeWithSuccess:(nullable NSNumber *)success
    attributesJson:(nullable NSString *)attributesJson;
@property(nonatomic, strong, nullable) NSNumber * success;
@property(nonatomic, copy, nullable) NSString * attributesJson;
@end

@interface NotifActionExecuteReq : NSObject
+ (instancetype)makeWithItemId:(nullable NSString *)itemId
    actionId:(nullable NSNumber *)actionId
    responseText:(nullable NSString *)responseText;
@property(nonatomic, copy, nullable) NSString * itemId;
@property(nonatomic, strong, nullable) NSNumber * actionId;
@property(nonatomic, copy, nullable) NSString * responseText;
@end

@interface NotificationPigeon : NSObject
+ (instancetype)makeWithPackageId:(nullable NSString *)packageId
    notifId:(nullable NSNumber *)notifId
    appName:(nullable NSString *)appName
    tagId:(nullable NSString *)tagId
    title:(nullable NSString *)title
    text:(nullable NSString *)text
    category:(nullable NSString *)category
    color:(nullable NSNumber *)color
    messagesJson:(nullable NSString *)messagesJson
    actionsJson:(nullable NSString *)actionsJson;
@property(nonatomic, copy, nullable) NSString * packageId;
@property(nonatomic, strong, nullable) NSNumber * notifId;
@property(nonatomic, copy, nullable) NSString * appName;
@property(nonatomic, copy, nullable) NSString * tagId;
@property(nonatomic, copy, nullable) NSString * title;
@property(nonatomic, copy, nullable) NSString * text;
@property(nonatomic, copy, nullable) NSString * category;
@property(nonatomic, strong, nullable) NSNumber * color;
@property(nonatomic, copy, nullable) NSString * messagesJson;
@property(nonatomic, copy, nullable) NSString * actionsJson;
@end

@interface AppEntriesPigeon : NSObject
+ (instancetype)makeWithAppName:(nullable NSArray<NSString *> *)appName
    packageId:(nullable NSArray<NSString *> *)packageId;
@property(nonatomic, strong, nullable) NSArray<NSString *> * appName;
@property(nonatomic, strong, nullable) NSArray<NSString *> * packageId;
@end

@interface PbwAppInfo : NSObject
+ (instancetype)makeWithIsValid:(nullable NSNumber *)isValid
    uuid:(nullable NSString *)uuid
    shortName:(nullable NSString *)shortName
    longName:(nullable NSString *)longName
    companyName:(nullable NSString *)companyName
    versionCode:(nullable NSNumber *)versionCode
    versionLabel:(nullable NSString *)versionLabel
    appKeys:(nullable NSDictionary<NSString *, NSNumber *> *)appKeys
    capabilities:(nullable NSArray<NSString *> *)capabilities
    resources:(nullable NSArray<WatchResource *> *)resources
    sdkVersion:(nullable NSString *)sdkVersion
    targetPlatforms:(nullable NSArray<NSString *> *)targetPlatforms
    watchapp:(nullable WatchappInfo *)watchapp;
@property(nonatomic, strong, nullable) NSNumber * isValid;
@property(nonatomic, copy, nullable) NSString * uuid;
@property(nonatomic, copy, nullable) NSString * shortName;
@property(nonatomic, copy, nullable) NSString * longName;
@property(nonatomic, copy, nullable) NSString * companyName;
@property(nonatomic, strong, nullable) NSNumber * versionCode;
@property(nonatomic, copy, nullable) NSString * versionLabel;
@property(nonatomic, strong, nullable) NSDictionary<NSString *, NSNumber *> * appKeys;
@property(nonatomic, strong, nullable) NSArray<NSString *> * capabilities;
@property(nonatomic, strong, nullable) NSArray<WatchResource *> * resources;
@property(nonatomic, copy, nullable) NSString * sdkVersion;
@property(nonatomic, strong, nullable) NSArray<NSString *> * targetPlatforms;
@property(nonatomic, strong, nullable) WatchappInfo * watchapp;
@end

@interface WatchappInfo : NSObject
+ (instancetype)makeWithWatchface:(nullable NSNumber *)watchface
    hiddenApp:(nullable NSNumber *)hiddenApp
    onlyShownOnCommunication:(nullable NSNumber *)onlyShownOnCommunication;
@property(nonatomic, strong, nullable) NSNumber * watchface;
@property(nonatomic, strong, nullable) NSNumber * hiddenApp;
@property(nonatomic, strong, nullable) NSNumber * onlyShownOnCommunication;
@end

@interface WatchResource : NSObject
+ (instancetype)makeWithFile:(nullable NSString *)file
    menuIcon:(nullable NSNumber *)menuIcon
    name:(nullable NSString *)name
    type:(nullable NSString *)type;
@property(nonatomic, copy, nullable) NSString * file;
@property(nonatomic, strong, nullable) NSNumber * menuIcon;
@property(nonatomic, copy, nullable) NSString * name;
@property(nonatomic, copy, nullable) NSString * type;
@end

@interface InstallData : NSObject
/// `init` unavailable to enforce nonnull fields, see the `make` class method.
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)makeWithUri:(NSString *)uri
    appInfo:(PbwAppInfo *)appInfo
    stayOffloaded:(NSNumber *)stayOffloaded;
@property(nonatomic, copy) NSString * uri;
@property(nonatomic, strong) PbwAppInfo * appInfo;
@property(nonatomic, strong) NSNumber * stayOffloaded;
@end

@interface AppInstallStatus : NSObject
/// `init` unavailable to enforce nonnull fields, see the `make` class method.
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)makeWithProgress:(NSNumber *)progress
    isInstalling:(NSNumber *)isInstalling;
/// Progress in range [0-1]
@property(nonatomic, strong) NSNumber * progress;
@property(nonatomic, strong) NSNumber * isInstalling;
@end

@interface ScreenshotResult : NSObject
/// `init` unavailable to enforce nonnull fields, see the `make` class method.
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)makeWithSuccess:(NSNumber *)success
    imagePath:(nullable NSString *)imagePath;
@property(nonatomic, strong) NSNumber * success;
@property(nonatomic, copy, nullable) NSString * imagePath;
@end

@interface AppLogEntry : NSObject
/// `init` unavailable to enforce nonnull fields, see the `make` class method.
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)makeWithUuid:(NSString *)uuid
    timestamp:(NSNumber *)timestamp
    level:(NSNumber *)level
    lineNumber:(NSNumber *)lineNumber
    filename:(NSString *)filename
    message:(NSString *)message;
@property(nonatomic, copy) NSString * uuid;
@property(nonatomic, strong) NSNumber * timestamp;
@property(nonatomic, strong) NSNumber * level;
@property(nonatomic, strong) NSNumber * lineNumber;
@property(nonatomic, copy) NSString * filename;
@property(nonatomic, copy) NSString * message;
@end

@interface OAuthResult : NSObject
+ (instancetype)makeWithCode:(nullable NSString *)code
    state:(nullable NSString *)state
    error:(nullable NSString *)error;
@property(nonatomic, copy, nullable) NSString * code;
@property(nonatomic, copy, nullable) NSString * state;
@property(nonatomic, copy, nullable) NSString * error;
@end

@interface NotifChannelPigeon : NSObject
+ (instancetype)makeWithPackageId:(nullable NSString *)packageId
    channelId:(nullable NSString *)channelId
    channelName:(nullable NSString *)channelName
    channelDesc:(nullable NSString *)channelDesc
    delete:(nullable NSNumber *)delete;
@property(nonatomic, copy, nullable) NSString * packageId;
@property(nonatomic, copy, nullable) NSString * channelId;
@property(nonatomic, copy, nullable) NSString * channelName;
@property(nonatomic, copy, nullable) NSString * channelDesc;
@property(nonatomic, strong, nullable) NSNumber * delete;
@end

@interface NotifyingPackage : NSObject
/// `init` unavailable to enforce nonnull fields, see the `make` class method.
- (instancetype)init NS_UNAVAILABLE;
+ (instancetype)makeWithPackageId:(NSString *)packageId
    packageName:(NSString *)packageName;
@property(nonatomic, copy) NSString * packageId;
@property(nonatomic, copy) NSString * packageName;
@end

/// The codec used by ScanCallbacks.
NSObject<FlutterMessageCodec> *ScanCallbacksGetCodec(void);

@interface ScanCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
/// pebbles = list of PebbleScanDevicePigeon
- (void)onScanUpdatePebbles:(NSArray<PebbleScanDevicePigeon *> *)pebbles completion:(void (^)(FlutterError *_Nullable))completion;
- (void)onScanStartedWithCompletion:(void (^)(FlutterError *_Nullable))completion;
- (void)onScanStoppedWithCompletion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by ConnectionCallbacks.
NSObject<FlutterMessageCodec> *ConnectionCallbacksGetCodec(void);

@interface ConnectionCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)onWatchConnectionStateChangedNewState:(WatchConnectionStatePigeon *)newState completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by RawIncomingPacketsCallbacks.
NSObject<FlutterMessageCodec> *RawIncomingPacketsCallbacksGetCodec(void);

@interface RawIncomingPacketsCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)onPacketReceivedListOfBytes:(ListWrapper *)listOfBytes completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by PairCallbacks.
NSObject<FlutterMessageCodec> *PairCallbacksGetCodec(void);

@interface PairCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)onWatchPairCompleteAddress:(StringWrapper *)address completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by TimelineCallbacks.
NSObject<FlutterMessageCodec> *TimelineCallbacksGetCodec(void);

@interface TimelineCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)handleTimelineActionActionTrigger:(ActionTrigger *)actionTrigger completion:(void (^)(ActionResponsePigeon *_Nullable, FlutterError *_Nullable))completion;
@end

/// The codec used by IntentCallbacks.
NSObject<FlutterMessageCodec> *IntentCallbacksGetCodec(void);

@interface IntentCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)openUriUri:(StringWrapper *)uri completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by BackgroundAppInstallCallbacks.
NSObject<FlutterMessageCodec> *BackgroundAppInstallCallbacksGetCodec(void);

@interface BackgroundAppInstallCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)beginAppInstallInstallData:(InstallData *)installData completion:(void (^)(FlutterError *_Nullable))completion;
- (void)deleteAppUuid:(StringWrapper *)uuid completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by AppInstallStatusCallbacks.
NSObject<FlutterMessageCodec> *AppInstallStatusCallbacksGetCodec(void);

@interface AppInstallStatusCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)onStatusUpdatedStatus:(AppInstallStatus *)status completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by NotificationListening.
NSObject<FlutterMessageCodec> *NotificationListeningGetCodec(void);

@interface NotificationListening : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)handleNotificationNotification:(NotificationPigeon *)notification completion:(void (^)(TimelinePinPigeon *_Nullable, FlutterError *_Nullable))completion;
- (void)dismissNotificationItemId:(StringWrapper *)itemId completion:(void (^)(FlutterError *_Nullable))completion;
- (void)shouldNotifyChannel:(NotifChannelPigeon *)channel completion:(void (^)(BooleanWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)updateChannelChannel:(NotifChannelPigeon *)channel completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by AppLogCallbacks.
NSObject<FlutterMessageCodec> *AppLogCallbacksGetCodec(void);

@interface AppLogCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)onLogReceivedEntry:(AppLogEntry *)entry completion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by FirmwareUpdateCallbacks.
NSObject<FlutterMessageCodec> *FirmwareUpdateCallbacksGetCodec(void);

@interface FirmwareUpdateCallbacks : NSObject
- (instancetype)initWithBinaryMessenger:(id<FlutterBinaryMessenger>)binaryMessenger;
- (void)onFirmwareUpdateStartedWithCompletion:(void (^)(FlutterError *_Nullable))completion;
- (void)onFirmwareUpdateProgressProgress:(NSNumber *)progress completion:(void (^)(FlutterError *_Nullable))completion;
- (void)onFirmwareUpdateFinishedWithCompletion:(void (^)(FlutterError *_Nullable))completion;
@end

/// The codec used by NotificationUtils.
NSObject<FlutterMessageCodec> *NotificationUtilsGetCodec(void);

@protocol NotificationUtils
- (void)dismissNotificationItemId:(StringWrapper *)itemId completion:(void (^)(BooleanWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)dismissNotificationWatchItemId:(StringWrapper *)itemId error:(FlutterError *_Nullable *_Nonnull)error;
- (void)openNotificationItemId:(StringWrapper *)itemId error:(FlutterError *_Nullable *_Nonnull)error;
- (void)executeActionAction:(NotifActionExecuteReq *)action error:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void NotificationUtilsSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<NotificationUtils> *_Nullable api);

/// The codec used by ScanControl.
NSObject<FlutterMessageCodec> *ScanControlGetCodec(void);

@protocol ScanControl
- (void)startBleScanWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)startClassicScanWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void ScanControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<ScanControl> *_Nullable api);

/// The codec used by ConnectionControl.
NSObject<FlutterMessageCodec> *ConnectionControlGetCodec(void);

@protocol ConnectionControl
/// @return `nil` only when `error != nil`.
- (nullable BooleanWrapper *)isConnectedWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)disconnectWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)sendRawPacketListOfBytes:(ListWrapper *)listOfBytes error:(FlutterError *_Nullable *_Nonnull)error;
- (void)observeConnectionChangesWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)cancelObservingConnectionChangesWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void ConnectionControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<ConnectionControl> *_Nullable api);

/// The codec used by RawIncomingPacketsControl.
NSObject<FlutterMessageCodec> *RawIncomingPacketsControlGetCodec(void);

@protocol RawIncomingPacketsControl
- (void)observeIncomingPacketsWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)cancelObservingIncomingPacketsWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void RawIncomingPacketsControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<RawIncomingPacketsControl> *_Nullable api);

/// The codec used by UiConnectionControl.
NSObject<FlutterMessageCodec> *UiConnectionControlGetCodec(void);

/// Connection methods that require UI reside in separate pigeon class.
/// This allows easier separation between background and UI methods.
@protocol UiConnectionControl
- (void)connectToWatchMacAddress:(StringWrapper *)macAddress error:(FlutterError *_Nullable *_Nonnull)error;
- (void)unpairWatchMacAddress:(StringWrapper *)macAddress error:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void UiConnectionControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<UiConnectionControl> *_Nullable api);

/// The codec used by NotificationsControl.
NSObject<FlutterMessageCodec> *NotificationsControlGetCodec(void);

@protocol NotificationsControl
- (void)sendTestNotificationWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)getNotificationPackagesWithCompletion:(void (^)(NSArray<NotifyingPackage *> *_Nullable, FlutterError *_Nullable))completion;
@end

extern void NotificationsControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<NotificationsControl> *_Nullable api);

/// The codec used by IntentControl.
NSObject<FlutterMessageCodec> *IntentControlGetCodec(void);

@protocol IntentControl
- (void)notifyFlutterReadyForIntentsWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)notifyFlutterNotReadyForIntentsWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)waitForOAuthWithCompletion:(void (^)(OAuthResult *_Nullable, FlutterError *_Nullable))completion;
@end

extern void IntentControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<IntentControl> *_Nullable api);

/// The codec used by DebugControl.
NSObject<FlutterMessageCodec> *DebugControlGetCodec(void);

@protocol DebugControl
- (void)collectLogsRwsId:(NSString *)rwsId error:(FlutterError *_Nullable *_Nonnull)error;
- (void)getSensitiveLoggingEnabledWithCompletion:(void (^)(NSNumber *_Nullable, FlutterError *_Nullable))completion;
- (void)setSensitiveLoggingEnabledEnabled:(NSNumber *)enabled completion:(void (^)(FlutterError *_Nullable))completion;
@end

extern void DebugControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<DebugControl> *_Nullable api);

/// The codec used by TimelineControl.
NSObject<FlutterMessageCodec> *TimelineControlGetCodec(void);

@protocol TimelineControl
- (void)addPinPin:(TimelinePinPigeon *)pin completion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)removePinPinUuid:(StringWrapper *)pinUuid completion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)removeAllPinsWithCompletion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
@end

extern void TimelineControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<TimelineControl> *_Nullable api);

/// The codec used by BackgroundSetupControl.
NSObject<FlutterMessageCodec> *BackgroundSetupControlGetCodec(void);

@protocol BackgroundSetupControl
- (void)setupBackgroundCallbackHandle:(NumberWrapper *)callbackHandle error:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void BackgroundSetupControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<BackgroundSetupControl> *_Nullable api);

/// The codec used by BackgroundControl.
NSObject<FlutterMessageCodec> *BackgroundControlGetCodec(void);

@protocol BackgroundControl
- (void)notifyFlutterBackgroundStartedWithCompletion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
@end

extern void BackgroundControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<BackgroundControl> *_Nullable api);

/// The codec used by PermissionCheck.
NSObject<FlutterMessageCodec> *PermissionCheckGetCodec(void);

@protocol PermissionCheck
/// @return `nil` only when `error != nil`.
- (nullable BooleanWrapper *)hasLocationPermissionWithError:(FlutterError *_Nullable *_Nonnull)error;
/// @return `nil` only when `error != nil`.
- (nullable BooleanWrapper *)hasCalendarPermissionWithError:(FlutterError *_Nullable *_Nonnull)error;
/// @return `nil` only when `error != nil`.
- (nullable BooleanWrapper *)hasNotificationAccessWithError:(FlutterError *_Nullable *_Nonnull)error;
/// @return `nil` only when `error != nil`.
- (nullable BooleanWrapper *)hasBatteryExclusionEnabledWithError:(FlutterError *_Nullable *_Nonnull)error;
/// @return `nil` only when `error != nil`.
- (nullable BooleanWrapper *)hasCallsPermissionsWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void PermissionCheckSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<PermissionCheck> *_Nullable api);

/// The codec used by PermissionControl.
NSObject<FlutterMessageCodec> *PermissionControlGetCodec(void);

@protocol PermissionControl
- (void)requestLocationPermissionWithCompletion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)requestCalendarPermissionWithCompletion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
/// This can only be performed when at least one watch is paired
- (void)requestNotificationAccessWithCompletion:(void (^)(FlutterError *_Nullable))completion;
/// This can only be performed when at least one watch is paired
- (void)requestBatteryExclusionWithCompletion:(void (^)(FlutterError *_Nullable))completion;
/// This can only be performed when at least one watch is paired
- (void)requestCallsPermissionsWithCompletion:(void (^)(FlutterError *_Nullable))completion;
- (void)requestBluetoothPermissionsWithCompletion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)openPermissionSettingsWithCompletion:(void (^)(FlutterError *_Nullable))completion;
@end

extern void PermissionControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<PermissionControl> *_Nullable api);

/// The codec used by CalendarControl.
NSObject<FlutterMessageCodec> *CalendarControlGetCodec(void);

@protocol CalendarControl
- (void)requestCalendarSyncForceResync:(NSNumber *)forceResync error:(FlutterError *_Nullable *_Nonnull)error;
- (void)setCalendarSyncEnabledEnabled:(NSNumber *)enabled completion:(void (^)(FlutterError *_Nullable))completion;
- (void)getCalendarSyncEnabledWithCompletion:(void (^)(NSNumber *_Nullable, FlutterError *_Nullable))completion;
- (void)deleteAllCalendarPinsWithCompletion:(void (^)(FlutterError *_Nullable))completion;
@end

extern void CalendarControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<CalendarControl> *_Nullable api);

/// The codec used by PigeonLogger.
NSObject<FlutterMessageCodec> *PigeonLoggerGetCodec(void);

@protocol PigeonLogger
- (void)vMessage:(StringWrapper *)message error:(FlutterError *_Nullable *_Nonnull)error;
- (void)dMessage:(StringWrapper *)message error:(FlutterError *_Nullable *_Nonnull)error;
- (void)iMessage:(StringWrapper *)message error:(FlutterError *_Nullable *_Nonnull)error;
- (void)wMessage:(StringWrapper *)message error:(FlutterError *_Nullable *_Nonnull)error;
- (void)eMessage:(StringWrapper *)message error:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void PigeonLoggerSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<PigeonLogger> *_Nullable api);

/// The codec used by TimelineSyncControl.
NSObject<FlutterMessageCodec> *TimelineSyncControlGetCodec(void);

@protocol TimelineSyncControl
- (void)syncTimelineToWatchLaterWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void TimelineSyncControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<TimelineSyncControl> *_Nullable api);

/// The codec used by WorkaroundsControl.
NSObject<FlutterMessageCodec> *WorkaroundsControlGetCodec(void);

@protocol WorkaroundsControl
/// @return `nil` only when `error != nil`.
- (nullable ListWrapper *)getNeededWorkaroundsWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void WorkaroundsControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<WorkaroundsControl> *_Nullable api);

/// The codec used by AppInstallControl.
NSObject<FlutterMessageCodec> *AppInstallControlGetCodec(void);

@protocol AppInstallControl
- (void)getAppInfoLocalPbwUri:(StringWrapper *)localPbwUri completion:(void (^)(PbwAppInfo *_Nullable, FlutterError *_Nullable))completion;
- (void)beginAppInstallInstallData:(InstallData *)installData completion:(void (^)(BooleanWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)beginAppDeletionUuid:(StringWrapper *)uuid completion:(void (^)(BooleanWrapper *_Nullable, FlutterError *_Nullable))completion;
/// Read header from pbw file already in Cobble's storage and send it to
/// BlobDB on the watch
- (void)insertAppIntoBlobDbUuidString:(StringWrapper *)uuidString completion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)removeAppFromBlobDbAppUuidString:(StringWrapper *)appUuidString completion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)removeAllAppsWithCompletion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)subscribeToAppStatusWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)unsubscribeFromAppStatusWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)sendAppOrderToWatchUuidStringList:(ListWrapper *)uuidStringList completion:(void (^)(NumberWrapper *_Nullable, FlutterError *_Nullable))completion;
@end

extern void AppInstallControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<AppInstallControl> *_Nullable api);

/// The codec used by AppLifecycleControl.
NSObject<FlutterMessageCodec> *AppLifecycleControlGetCodec(void);

@protocol AppLifecycleControl
- (void)openAppOnTheWatchUuidString:(StringWrapper *)uuidString completion:(void (^)(BooleanWrapper *_Nullable, FlutterError *_Nullable))completion;
@end

extern void AppLifecycleControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<AppLifecycleControl> *_Nullable api);

/// The codec used by PackageDetails.
NSObject<FlutterMessageCodec> *PackageDetailsGetCodec(void);

@protocol PackageDetails
/// @return `nil` only when `error != nil`.
- (nullable AppEntriesPigeon *)getPackageListWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void PackageDetailsSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<PackageDetails> *_Nullable api);

/// The codec used by ScreenshotsControl.
NSObject<FlutterMessageCodec> *ScreenshotsControlGetCodec(void);

@protocol ScreenshotsControl
- (void)takeWatchScreenshotWithCompletion:(void (^)(ScreenshotResult *_Nullable, FlutterError *_Nullable))completion;
@end

extern void ScreenshotsControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<ScreenshotsControl> *_Nullable api);

/// The codec used by AppLogControl.
NSObject<FlutterMessageCodec> *AppLogControlGetCodec(void);

@protocol AppLogControl
- (void)startSendingLogsWithError:(FlutterError *_Nullable *_Nonnull)error;
- (void)stopSendingLogsWithError:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void AppLogControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<AppLogControl> *_Nullable api);

/// The codec used by FirmwareUpdateControl.
NSObject<FlutterMessageCodec> *FirmwareUpdateControlGetCodec(void);

@protocol FirmwareUpdateControl
- (void)checkFirmwareCompatibleFwUri:(StringWrapper *)fwUri completion:(void (^)(BooleanWrapper *_Nullable, FlutterError *_Nullable))completion;
- (void)beginFirmwareUpdateFwUri:(StringWrapper *)fwUri completion:(void (^)(BooleanWrapper *_Nullable, FlutterError *_Nullable))completion;
@end

extern void FirmwareUpdateControlSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<FirmwareUpdateControl> *_Nullable api);

/// The codec used by KeepUnusedHack.
NSObject<FlutterMessageCodec> *KeepUnusedHackGetCodec(void);

/// This class will keep all classes that appear in lists from being deleted
/// by pigeon (they are not kept by default because pigeon does not support
/// generics in lists).
@protocol KeepUnusedHack
- (void)keepPebbleScanDevicePigeonCls:(PebbleScanDevicePigeon *)cls error:(FlutterError *_Nullable *_Nonnull)error;
- (void)keepWatchResourceCls:(WatchResource *)cls error:(FlutterError *_Nullable *_Nonnull)error;
@end

extern void KeepUnusedHackSetup(id<FlutterBinaryMessenger> binaryMessenger, NSObject<KeepUnusedHack> *_Nullable api);

NS_ASSUME_NONNULL_END
