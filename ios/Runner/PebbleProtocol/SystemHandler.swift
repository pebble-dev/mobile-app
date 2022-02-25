//
//  SystemHandler.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import libpebblecommon
class SystemHandler {
    private let handleAppVersionRequest = HandleAppVersionRequest()
    private var negotiatedListeners: [() -> ()] = []
    init(systemService: SystemService) {
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
        for listener in negotiatedListeners {
            listener()
        }
        negotiatedListeners.removeAll()
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
