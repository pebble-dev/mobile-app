//
//  AppInstallControlFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 04/03/2022.
//

import Foundation
import CocoaLumberjackSwift
import libpebblecommon
import PromiseKit

class AppInstallControlFlutterBridge: NSObject, AppInstallControl {
    private let ioQueue = DispatchQueue(label: Bundle.main.bundleIdentifier!+".AppInstallIOQueue", qos: .utility)
    private let appInstallStatusCallbacks: AppInstallStatusCallbacks
    private var appStatusObserver: Any? = nil
    init(callbackMessenger: FlutterBinaryMessenger) {
        appInstallStatusCallbacks = AppInstallStatusCallbacks(binaryMessenger: callbackMessenger)
        super.init()
    }
    
    func getAppInfoLocalPbwUri(_ localPbwUri: StringWrapper?, completion: @escaping (Pigeon_PbwAppInfo?, FlutterError?) -> Void) {
        guard let url = URL(string: (localPbwUri?.value)!) else {
            completion(nil, FlutterError(code: "INVALID_URI", message: "Url '\(localPbwUri?.value ?? "nil")' passed to getAppInfo is invalid.", details: nil))
            return
        }
        
        do {
            let appInfo = try requirePbwAppInfo(pbwFile: url)
            completion(appInfo.toPigeon(), nil)
        } catch let error as PbwSpecError {
            DDLogError("Failed to parse PBW when handling getAppInfo: \(error), replying with invalid obj")
            let invalidPbw = Pigeon_PbwAppInfo()
            invalidPbw.isValid = false
            invalidPbw.watchapp = WatchappInfo()
            completion(invalidPbw, nil)
        } catch {
            completion(nil, FlutterError(code: "ERROR", message: error.localizedDescription, details: nil))
        }
    }
    
    func beginAppInstallInstallData(_ installData: InstallData?, completion: @escaping (BooleanWrapper?, FlutterError?) -> Void) {
        ioQueue.async {
            guard let installData = installData else {
                assertionFailure("installData == nil")
                return
            }

            guard let originUrl = URL(string: installData.uri) else {
                let msg = "Url '\(installData.uri )' passed to beginAppInstall is invalid."
                DDLogError(msg)
                completion(nil, FlutterError(code: "INVALID_URI", message: msg, details: nil))
                return
            }
            
            do {
                let appUuid = (installData.appInfo.uuid)!
                let targetUrl = try getAppPbwFile(appUuid: appUuid)
                if (FileManager.default.fileExists(atPath: targetUrl.path)) {
                    DDLogInfo("App is already imported, will overwrite stored copy")
                    try? FileManager.default.removeItem(at: targetUrl)
                }
                try FileManager.default.copyItem(at: originUrl, to: targetUrl)
                
                let _ = try BackgroundAppInstallFlutterBridge.shared.installAppNow(uri: installData.uri, appInfo: installData.appInfo).wait()
                completion(BooleanWrapper.make(withValue: NSNumber(value: true)), nil)
            } catch {
                DDLogError("Error during beginAppInstall: \(error.localizedDescription)")
                completion(nil, FlutterError(code: "ERROR", message: error.localizedDescription, details: nil))
            }
        }
    }
    
    func beginAppDeletionUuid(_ uuid: StringWrapper?, completion: @escaping (BooleanWrapper?, FlutterError?) -> Void) {
        ioQueue.async {
            guard let uuid = uuid?.value else {
                assertionFailure("uuid == nil")
                return
            }
            do {
                let appFile = try getAppPbwFile(appUuid: uuid)
                try FileManager.default.removeItem(at: appFile)
                BackgroundAppInstallFlutterBridge.shared.deleteApp(uuid: StringWrapper.make(withValue: uuid))
                    .done { result in
                        completion(BooleanWrapper.make(withValue: NSNumber(value: result)), nil)
                    }.catch{ error in
                        DDLogError("Error during beginAppDeletion: \(error.localizedDescription)")
                        completion(BooleanWrapper.make(withValue: NSNumber(value: false)), nil)
                    }
            } catch {
                DDLogError("Error during beginAppDeletion: \(error.localizedDescription)")
                completion(nil, FlutterError(code: "ERROR", message: error.localizedDescription, details: nil))
            }
        }
    }
    
    func insertApp(intoBlobDbUuidString uuidString: StringWrapper?, completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        ioQueue.async {
            guard let appUuid = uuidString?.value else {
                assertionFailure("uuidString == nil")
                return
            }
            do {
                let appFile = try getAppPbwFile(appUuid: appUuid)
                if !FileManager.default.fileExists(atPath: appFile.path) {
                    DDLogError("Error during insertAppIntoBlobDb: PBW file \(appUuid) missing")
                    completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
                }else {
                    guard let hardwarePlatformNumber = WatchMetadataStore.shared.lastConnectedWatchMetadata?.running.hardwarePlatform.get()?.uint8Value else {
                        DDLogError("Error during insertAppIntoBlobDb: Watch not connected")
                        completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
                        return
                    }
                    
                    guard let connectedWatchType = WatchHardwarePlatform.companion.fromProtocolNumber(number: hardwarePlatformNumber)?.watchType else {
                        DDLogError("Error during insertAppIntoBlobDb: Unknown watch type")
                        completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
                        return
                    }
                    
                    let appInfo = try requirePbwAppInfo(pbwFile: appFile)
                    guard let targetWatchType = connectedWatchType.getBestVariant(availableAppVariants: appInfo.targetPlatforms) else {
                        DDLogError("Error during insertAppIntoBlobDb: Watch \(connectedWatchType) is not compatible with app \(appUuid). " +
                                   "Compatible apps: \(appInfo.targetPlatforms)")
                        completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
                        return
                    }
                    
                    let manifest = try requirePbwManifest(pbwFile: appFile, watchType: targetWatchType)
                    
                    let appBlob = try requirePbwBinaryBlob(pbwFile: appFile, watchType: targetWatchType, blobName: manifest.application.name)
                    let appBlobHeader = appBlob[..<PbwBinHeader.companion.SIZE]
                    let parsedHeader = PbwBinHeader.companion.parseFileHeader(data: KUtil.shared.byteArrayAsUByteArray(
                        arr: KUtil.shared.byteArrayFromNative(arr: appBlobHeader)
                    ))
                    ProtocolComms.shared.blobDBService.sendPromise(
                        packet: BlobCommand.InsertCommand(
                            token: UInt16.random(in: UInt16.min...UInt16.max),
                            database: .app,
                            key: parsedHeader.uuid.toBytes(),
                            value: parsedHeader.toBlobDbApp().toBytes()
                        ),
                        priority: .normal
                    ).done {result in
                        completion(NumberWrapper.make(withValue: NSNumber(value: Int(result.responseValue.value))), nil)
                    }.catch { error in
                        DDLogError("Error during insertAppIntoBlobDb InsertCommand: \(error)")
                        completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
                    }
                }
            }catch {
                DDLogError("Error during insertAppIntoBlobDb: \(error)")
                completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
            }
        }
    }
    
    func removeApp(fromBlobDbAppUuidString appUuidString: StringWrapper?, completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        guard let appUuidString = appUuidString?.value else {
            assertionFailure("appUuidString == nil")
            return
        }
        guard let appUuid = UuidUuid.fromString(appUuidString) else {
            DDLogError("Error during removeAppFromBlobDb: Invalid UUID \(appUuidString)")
            completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
            return
        }
        
        Promise { seal in
            ProtocolComms.shared.blobDBService.send(
                packet: BlobCommand.DeleteCommand(
                    token: UInt16.random(in: UInt16.min...UInt16.max),
                    database: .app,
                    key: SUUID(mapper: StructMapper(), default: appUuid).toBytes()
                ),
                priority: .normal,
                completionHandler: seal.resolve
            )
        }.done { result in
            completion(NumberWrapper.make(withValue: NSNumber(value: Int(result.response.get()!.uint8Value))), nil)
        }.catch { error in
            completion(nil, FlutterError(code: "ERROR", message: String(describing: error), details: nil))
        }
    }
    
    func removeAllApps(completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        ProtocolComms.shared.blobDBService.sendPromise(
            packet: BlobCommand.ClearCommand(
                token: UInt16.random(in: UInt16.min...UInt16.max),
                database: .app
            ),
            priority: .normal
        ).done { result in
            completion(NumberWrapper.make(withValue: NSNumber(value: Int(result.response.get()!.uint8Value))), nil)
        }.catch { error in
            completion(nil, FlutterError(code: "ERROR", message: String(describing: error), details: nil))
        }
    }

    func subscribeToAppStatusWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        if (appStatusObserver != nil) {
            NotificationCenter.default.removeObserver(appStatusObserver!, name: NSNotification.Name(rawValue: "PutBytesController.Status"), object: nil)
        }
        appStatusObserver = NotificationCenter.default.addObserver(
            forName: NSNotification.Name(rawValue: "PutBytesController.Status"),
            object: nil,
            queue: nil
        ) {notif in
            let status = notif.object as! PutBytesController.Status
            self.appInstallStatusCallbacks.onStatusUpdatedStatus(
                AppInstallStatus.make(withProgress: NSNumber(value: status.progress), isInstalling: NSNumber(value: status.state == .Sending))
            ) {_ in}
        }
    }
    
    func unsubscribeFromAppStatusWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        guard let appStatusObserver = appStatusObserver else {
            return
        }
        NotificationCenter.default.removeObserver(appStatusObserver, name: NSNotification.Name(rawValue: "PutBytesController.Status"), object: nil)
        self.appStatusObserver = nil
    }
    
    func sendAppOrder(toWatchUuidStringList uuidStringList: ListWrapper?, completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        let uuids = uuidStringList!.value!.map { UuidUuid.fromString($0 as! String)! }
        withTimeoutOrNull(timeoutMs: 10000,
                          promise: ProtocolComms.shared.reorderService.sendPromise(packet: AppReorderRequest(appList: uuids)))
            .done { result in
                if let result = result {
                    if result.status.get()?.uint8Value == AppOrderResultCode.success.value {
                        completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.success.value))), nil)
                    }
                }else {
                    completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.watchdisconnected.value))), nil)
                }
            }
            .catch { error in
                DDLogDebug("Error during sendAppOrderToWatch: \(error)")
                completion(NumberWrapper.make(withValue: NSNumber(value: Int(BlobResponse.BlobStatus.generalfailure.value))), nil)
            }
    }
    
}
