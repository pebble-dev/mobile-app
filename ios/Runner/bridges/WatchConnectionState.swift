//
//  WatchConnectionState.swift
//  Runner
//
//  Created by crc32 on 05/03/2022.
//

import Foundation
import PromiseKit

enum WatchConnectionState {
    case disconnected
    case connecting(watch: BluePebbleDevice?)
    case connected(watch: BluePebbleDevice?)
    case waitingForReconnect(watch: BluePebbleDevice?)
    case waitingForBluetoothToEnable(watch: BluePebbleDevice?)
}

extension WatchConnectionState {
    static var current = WatchConnectionState.disconnected {
        didSet {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "WatchConnectionState.current"), object: self.current)
        }
    }
    
    static func ~= (lhs: Self, rhs: Self) -> Bool {
        switch (lhs, rhs) {
        case
            (.disconnected, .disconnected),
            (.connecting, .connecting),
            (.connected, .connected),
            (.waitingForReconnect, .waitingForReconnect),
            (.waitingForBluetoothToEnable, .waitingForBluetoothToEnable):
            return true
            
        default:
            return false
        }
    }
    
    var watchOrNil: BluePebbleDevice? {
        switch self {
        case .disconnected:
            return nil
        case .connecting(let watch):
            return watch
        case .connected(let watch):
            return watch
        case .waitingForReconnect(let watch):
            return watch
        case .waitingForBluetoothToEnable(let watch):
            return watch
        }
    }
    
    static func next() -> Promise<WatchConnectionState> {
        return Promise {seal in
            var token: NSObjectProtocol?
            token = NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: "WatchConnectionState.current"), object: nil, queue: nil) { notif in
                seal.fulfill(notif.object! as! WatchConnectionState)
                NotificationCenter.default.removeObserver(token!)
            }
        }
    }
}
