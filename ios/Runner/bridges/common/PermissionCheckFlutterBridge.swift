//
//  PermissionCheckFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import Flutter

class PermissionCheckFlutterBridge: PermissionCheck {
    func hasLocationPermission(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
    
    func hasCalendarPermission(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
    
    func hasNotificationAccess(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
    
    func hasBatteryExclusionEnabled(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
}
