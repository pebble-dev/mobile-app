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

    // MARK: - Bridge Creation

    static func createCommonBridges(binaryMessenger: FlutterBinaryMessenger) {
        PermissionCheckSetup(binaryMessenger, PermissionCheckFlutterBridge())
        ScanControlSetup(binaryMessenger, ScanFlutterBridge(callbackMessenger: binaryMessenger))
        AppInstallControlSetup(binaryMessenger, AppInstallControlFlutterBridge(callbackMessenger: binaryMessenger))
        ConnectionControlSetup(binaryMessenger, ConnectionFlutterBridge(callbackMessenger: binaryMessenger))
        PigeonLoggerSetup(binaryMessenger, LoggingFlutterBridge())
        AppLifecycleControlSetup(binaryMessenger, AppLifecycleFlutterBridge())
    }
    
    static func createBackgroundBridges(binaryMessenger: FlutterBinaryMessenger) {
    }

    static func createUIBridges(binaryMessenger: FlutterBinaryMessenger) {
        BackgroundSetupControlSetup(binaryMessenger, backgroundSetupBridge)
        PermissionControlSetup(binaryMessenger, PermissionControlFlutterBridge())
        UiConnectionControlSetup(binaryMessenger, ConnectionControlBridge(callbackMessenger: binaryMessenger))
        IntentControlSetup(binaryMessenger, IntentControlFlutterBridge(callbackMessenger: binaryMessenger))
        WorkaroundsControlSetup(binaryMessenger, WorkaroundsFlutterBridge())
    }

    // MARK: - BackgroundSetup Bridge

    private static var backgroundSetupBridge: BackgroundSetupFlutterBridge = {
        let bridge = BackgroundSetupFlutterBridge()

        bridge.waitForBackgroundHandle().done {
            FlutterBackgroundController.shared.setupEngine($0)
        }.cauterize()

        return bridge
    }()
}
