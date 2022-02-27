//
//  AppInstallHandler.swift
//  Runner
//
//  Created by crc32 on 26/02/2022.
//

import Foundation
import libpebblecommon
import CocoaLumberjackSwift
import PromiseKit

class AppInstallHandler {
    private let appFetchService: AppFetchService
    
    init(appFetchService: AppFetchService) {
        self.appFetchService = appFetchService
        appFetchService.receivedMessages.receive(completionHandler: onAppFetchServiceMessage)
    }
    
    func onAppFetchServiceMessage(message: Any?, e: Error?) {
        if (message is AppFetchRequest) {
            let message: AppFetchRequest = message as! AppFetchRequest
            let appUuid = message.uuid.get()
            do {
                let appFile = try getAppPbwFile(appUuid: appUuid?.description() ?? "")
                guard FileManager.default.fileExists(atPath: appFile.path) else {
                    respondFetchRequest(status: .invalidUuid).cauterize()
                    DDLogError("Watch requested nonexistent app data \(String(describing: appUuid))")
                    return
                }
                
                guard ProtocolComms.shared.putBytesController.status.state == .Idle else {
                    DDLogError("Watch requested new app data but PutBytes is busy")
                    respondFetchRequest(status: .busy).cauterize()
                    return
                }
                
                guard let hardwarePlatformNumber = LECentral.shared.currentDevice?.leMeta?.hardwarePlatform else {
                    DDLogError("No watch metadata available. Cannot deduce watch type.")
                    respondFetchRequest(status: .noData).cauterize()
                    return
                }
                
                guard let connectedWatchType = WatchHardwarePlatform.companion.fromProtocolNumber(number: hardwarePlatformNumber) else {
                    DDLogError("Unknown hardware platform \(hardwarePlatformNumber)")
                    respondFetchRequest(status: .noData).cauterize()
                    return
                }
                
                let appInfo = try requirePbwAppInfo(pbwFile: appFile)
                guard let targetWatchType = connectedWatchType.watchType.getBestVariant(availableAppVariants: appInfo.targetPlatforms) else {
                    DDLogError("Watch \(connectedWatchType) is not compatible with app \(String(describing: appUuid)) Compatible platforms: \(appInfo.targetPlatforms)")
                    respondFetchRequest(status: .noData).cauterize()
                    return
                }
                
                self.respondFetchRequest(status: .start).then { () -> Promise<Void> in
                    guard let appId = message.appId.get()?.uintValue else {
                        DDLogError("appID was null")
                        return self.respondFetchRequest(status: .noData)
                    }
                    return try ProtocolComms.shared.putBytesController.startAppInstall(appId: appId, pbwFile: appFile, watchType: targetWatchType).asVoid()
                }.catch { error in
                    DDLogError("Exception while catering to to AppFetchService message:" + error.localizedDescription)
                }
            } catch {
                DDLogError("Exception while handling AppFetchService message:" + error.localizedDescription)
            }
        }
        appFetchService.receivedMessages.receive(completionHandler: onAppFetchServiceMessage)
    }
    
    private func respondFetchRequest(status: AppFetchResponseStatus) -> Promise<Void> {
        return Promise<Void> { (seal) throws -> () in
            appFetchService.send(packet: AppFetchResponse(status: status)) {_,e in
                guard e != nil else {
                    DDLogError("Failed to respond to AppFetchRequest: " + e!.localizedDescription)
                    seal.reject(e!)
                    return
                }
                seal.fulfill(())
            }
        }
        
    }
}
