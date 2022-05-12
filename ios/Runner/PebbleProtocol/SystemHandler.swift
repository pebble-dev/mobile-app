//
//  SystemHandler.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import libpebblecommon
import PromiseKit
import CocoaLumberjackSwift

class SystemHandler {
    private let handleAppVersionRequest = HandleAppVersionRequest()
    private var negotiatedListeners: [() -> ()] = []
    private let systemService: SystemService
    init(systemService: SystemService) {
        self.systemService = systemService
        handleAppVersionRequest.negotiatedCallback = onNegotiationComplete
        systemService.appVersionRequestHandler = handleAppVersionRequest
    }
    
    private class HandleAppVersionRequest: KotlinSuspendFunction0 {
        public var negotiatedCallback: (() -> ())?
        func invoke(completionHandler: @escaping (Any?, Error?) -> Void) {
            var platformFlags = [PhoneAppVersion.PlatformFlag]()
            platformFlags.append(.btle)
            let res = PhoneAppVersion.AppVersionResponse(protocolVersion: UInt32.max, sessionCaps: 0,
                                                         platformFlags: PhoneAppVersion.PlatformFlagCompanion.init().makeFlags(
                                                            osType: .ios,
                                                            flags: platformFlags
                                                         ),
                                                         responseVersion: 2, majorVersion: 4, minorVersion: 4, bugfixVersion: 2,
                                                         protocolCaps: ProtocolCapsFlag.Companion.init().makeFlags(flags: [
                                                            .supports8kappmessage, .supportsapprunstateprotocol
                                                         ])
            )
            completionHandler(res, nil)
            negotiatedCallback?()
        }
    }
    
    private func onNegotiationComplete() {
        firstly {
            sendCurrentTime()
        }.then {
            self.refreshWatchMetadata()
        }.done {
            for listener in self.negotiatedListeners {
                listener()
            }
            self.negotiatedListeners.removeAll()
        }.cauterize()
    }
    
    private func sendCurrentTime() -> Promise<Void> {
        let timezone = TimeZone.current
        let utcTime = Date().timeIntervalSince1970
        let utcOffset = timezone.secondsFromGMT()

        let updateTimePacket = TimeMessage.SetUTC(unixTime: UInt32(round(utcTime)), utcOffset: Int16(utcOffset), timeZoneName: timezone.identifier)
        return systemService.sendPromise(packet: updateTimePacket, priority: .low)
    }
    
    private func refreshWatchMetadata() -> Promise<Void> {
        return Promise { seal in
            DDLogDebug("Refreshing watch metadata")
            firstly {
                return self.systemService.requestWatchModelPromise()
            }.done { model in
                DDLogDebug("Refreshing watch metadata: model")
                WatchMetadataStore.shared.lastConnectedWatchModel = model
            }.then {
                return self.systemService.requestWatchVersionPromise()
            }.done {version in
                DDLogDebug("Refreshing watch metadata: version")
                WatchMetadataStore.shared.lastConnectedWatchMetadata = version
                DDLogDebug("Watch metadata refreshed: \(WatchMetadataStore.shared.lastConnectedWatchMetadata?.debugDescription ?? "nil") - Model \(WatchMetadataStore.shared.lastConnectedWatchModel?.description ?? "nil")")
                seal.fulfill(())
            }.catch { error in
                DDLogError("Exception while refreshing watch metadata: \(error)")
            }
        }
    }
    
    public func waitNegotiationComplete(completionHandler: @escaping () -> ()) {
        negotiatedListeners.append(completionHandler)
    }
    
    @available(iOS 13.0.0, *)
    public func waitNegotiationCompleteAsync() async {
        return await withCheckedContinuation { continuation in
            waitNegotiationComplete() {
                continuation.resume(returning: ())
            }
        }
    }
    
}
