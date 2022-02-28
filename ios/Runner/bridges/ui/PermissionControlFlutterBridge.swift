//
//  PermissionControlFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
class PermissionControlFlutterBridge: NSObject, PermissionControl {
    //TODO: finish impl
    func requestLocationPermission(completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        let num = NumberWrapper()
        num.value = 0
        completion(num, nil)
    }
    
    func requestCalendarPermission(completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        let num = NumberWrapper()
        num.value = 0
        completion(num, nil)
    }
    
    func requestNotificationAccess(completion: @escaping (FlutterError?) -> Void) {
        completion(nil)
    }
    
    func requestBatteryExclusion(completion: @escaping (FlutterError?) -> Void) {
        completion(nil)
    }
    
    func openPermissionSettings(completion: @escaping (FlutterError?) -> Void) {
        let settingsUrl = URL.init(string: UIApplication.openSettingsURLString)!
        if #available(iOS 10.0, *) {
            UIApplication.shared.open(settingsUrl, options: [:], completionHandler: nil)
        } else {
            UIApplication.shared.openURL(settingsUrl)
        }
        completion(nil)
    }
}
