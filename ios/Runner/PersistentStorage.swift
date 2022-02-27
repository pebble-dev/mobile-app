//
//  PersistentStorage.swift
//  Runner
//
//  Created by crc32 on 26/10/2021.
//

import Foundation
import CocoaLumberjackSwift
class PersistentStorage {
    static let shared = PersistentStorage()
    
    private static let KEY_DEVICES = "devices"
    
    var devices: [StoredPebbleDevice] {
        get {
            if let data = UserDefaults.standard.data(forKey: PersistentStorage.KEY_DEVICES) {
                do {
                    let decoder = JSONDecoder()
                    let res = try decoder.decode([StoredPebbleDevice].self, from: data)
                    return res
                } catch {
                    DDLogError("PersistentStorage: Error decoding stored devices")
                }
            }
            return []
        }
        
        set(nw) {
            do {
                let encoder = JSONEncoder()
                let res = try encoder.encode(nw)
                UserDefaults.standard.set(res, forKey: PersistentStorage.KEY_DEVICES)
            }catch {
                DDLogError("PerisistentStorage: Error encoding stored devices")
            }
        }
    }
}
