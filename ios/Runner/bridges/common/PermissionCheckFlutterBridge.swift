//
//  PermissionCheckFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import Flutter

//TODO: Permission checks
class PermissionCheckFlutterBridge: NSObject, PermissionCheck {
    func hasLocationPermissionWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
    
    func hasCalendarPermissionWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
    
    func hasNotificationAccessWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
    
    func hasBatteryExclusionEnabledWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let bool = BooleanWrapper()
        bool.value = false
        return bool
    }
}
