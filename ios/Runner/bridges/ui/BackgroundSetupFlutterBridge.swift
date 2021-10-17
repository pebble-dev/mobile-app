//
//  BackgroundSetupFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
class BackgroundSetupFlutterBridge: BackgroundSetupControl {
    func setupBackground(_ input: NumberWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        let persistentState = UserDefaults.standard
        persistentState.set(input.value! as! Int64, forKey: "FlutterBackgroundHandle")
    }
}
