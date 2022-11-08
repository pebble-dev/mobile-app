//
//  AppLifecycleFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 06/03/2022.
//

import Foundation
import libpebblecommon
import CocoaLumberjackSwift
class AppLifecycleFlutterBridge: NSObject, AppLifecycleControl {
    func openApp(onTheWatchUuidString uuidString: StringWrapper?, completion: @escaping (BooleanWrapper?, FlutterError?) -> Void) {
        ProtocolComms.shared.appRunStateService.sendPromise(
            packet: AppRunStateMessage.AppRunStateStart(uuid: UuidUuid.fromString(uuidString!.value!)!)
        ).done {
            completion(BooleanWrapper.make(withValue: NSNumber(value: true)), nil)
        }.catch { error in
            DDLogError("Error during openAppOnTheWatch: \(error)")
            completion(BooleanWrapper.make(withValue: NSNumber(value: false)), nil)
        }
    }
}
