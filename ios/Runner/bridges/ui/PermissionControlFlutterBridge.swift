//
//  PermissionControlFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
class PermissionControlFlutterBridge: PermissionControl { //TODO
    func requestLocationPermission(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> NumberWrapper? {
        let num = NumberWrapper()
        num.value = 0
        return num
    }
    
    func requestCalendarPermission(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> NumberWrapper? {
        let num = NumberWrapper()
        num.value = 0
        return num
    }
    
    func requestNotificationAccess(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        
    }
    
    func requestBatteryExclusion(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        
    }
    
    func openPermissionSettings(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        let settingsUrl = URL.init(string: UIApplication.openSettingsURLString)!
        if #available(iOS 10.0, *) {
            UIApplication.shared.open(settingsUrl, options: [:], completionHandler: nil)
        } else {
            UIApplication.shared.openURL(settingsUrl)
        }
    }
}
