//
//  SystemHandler.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import libpebblecommon
class SystemHandler {
    init(systemService: SystemService) {
        systemService.appVersionRequestHandler = HandleAppVersionRequest()
    }
    
    private class HandleAppVersionRequest: KotlinSuspendFunction0 {
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
        }
    }
}
