//
//  LoggingFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 06/03/2022.
//

import Foundation
import CocoaLumberjackSwift
class LoggingFlutterBridge: NSObject, PigeonLogger {
    func vMessage(_ message: StringWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        DDLogVerbose(message.value!)
    }
    
    func dMessage(_ message: StringWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        DDLogDebug(message.value!)
    }
    
    func iMessage(_ message: StringWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        DDLogInfo(message.value!)
    }
    
    func wMessage(_ message: StringWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        DDLogWarn(message.value!)
    }
    
    func eMessage(_ message: StringWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        DDLogError(message.value!)
    }
}
