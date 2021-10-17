//
//  FlutterBridgeSetup.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import Flutter
class FlutterBridgeSetup {
    static func createCommonBridges(binaryMessenger: FlutterBinaryMessenger) {
        PermissionCheckSetup(binaryMessenger, PermissionCheckFlutterBridge())
        ScanControlSetup(binaryMessenger, ScanFlutterBridge(callbackMessenger: binaryMessenger))
    }
    
    static func createBackgroundBridges(binaryMessenger: FlutterBinaryMessenger) {
        
    }
    
    static func createUIBridges(binaryMessenger: FlutterBinaryMessenger) {
        BackgroundSetupControlSetup(binaryMessenger, BackgroundSetupFlutterBridge())
        PermissionControlSetup(binaryMessenger, PermissionControlFlutterBridge())
    }
}
