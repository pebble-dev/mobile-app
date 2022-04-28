//
//  FlutterBridgeSetup.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import Flutter
import PromiseKit

class FlutterBridgeSetup {
    static func createCommonBridges(binaryMessenger: FlutterBinaryMessenger) {
        PermissionCheckSetup(binaryMessenger, PermissionCheckFlutterBridge())
        ScanControlSetup(binaryMessenger, ScanFlutterBridge(callbackMessenger: binaryMessenger))
        AppInstallControlSetup(binaryMessenger, AppInstallControlFlutterBridge(callbackMessenger: binaryMessenger))
        ConnectionControlSetup(binaryMessenger, ConnectionFlutterBridge(callbackMessenger: binaryMessenger))
        PigeonLoggerSetup(binaryMessenger, LoggingFlutterBridge())
        AppLifecycleControlSetup(binaryMessenger, AppLifecycleFlutterBridge())
    }
    
    static func createBackgroundBridges(binaryMessenger: FlutterBinaryMessenger) {
        let backgroundSetupBridge = BackgroundSetupFlutterBridge()

        // I am not super familiar with PromiseKit, I didn't know how to get rid of the unused return value of `.done`
        _ = backgroundSetupBridge.waitForBackgroundHandle().done { handle in
            FlutterBackgroundController.shared.setupEngine(handle)
        }

        BackgroundSetupControlSetup(binaryMessenger, backgroundSetupBridge)
    }
    
    static func createUIBridges(binaryMessenger: FlutterBinaryMessenger) {
        PermissionControlSetup(binaryMessenger, PermissionControlFlutterBridge())
        UiConnectionControlSetup(binaryMessenger, ConnectionControlBridge(callbackMessenger: binaryMessenger))
        IntentControlSetup(binaryMessenger, IntentControlFlutterBridge(callbackMessenger: binaryMessenger))
        WorkaroundsControlSetup(binaryMessenger, WorkaroundsFlutterBridge())
    }
}
