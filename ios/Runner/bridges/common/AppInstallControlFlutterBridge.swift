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
    
    func getAppInfoLocalPbwUri(
        _ localPbwUri: StringWrapper,
        completion: @escaping (Pigeon_PbwAppInfo?, FlutterError?) -> Void
    ) {
        guard let url = URL(string: (localPbwUri.value)!) else {
            completion(nil, FlutterError(code: "INVALID_URI", message: "Url '\(localPbwUri.value ?? "nil")' passed to getAppInfo is invalid.", details: nil))
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
    
    func sendAppOrder(
        toWatchUuidStringList uuidStringList: ListWrapper,
        completion: @escaping (NumberWrapper?, FlutterError?) -> Void
    ) {
        let uuids = uuidStringList.value!.map { UuidUuid.fromString($0 as! String)! }
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
