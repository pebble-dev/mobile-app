//
//  BackgroundSetupFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
class BackgroundSetupFlutterBridge: NSObject, BackgroundSetupControl {
    func setupBackgroundCallbackHandle(_ callbackHandle: NumberWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        let persistentState = UserDefaults.standard
        persistentState.set(callbackHandle.value! as! Int64, forKey: "FlutterBackgroundHandle")
    }
}
