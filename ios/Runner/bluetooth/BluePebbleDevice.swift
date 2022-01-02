//
//  BluePebbleDevice.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import CoreBluetooth
import CobbleLE
class BluePebbleDevice {
    
    let peripheral: CBPeripheral
    var leMeta: GAPMeta?
    
    init(peripheral: CBPeripheral, advertiseData: [UInt8]?) {
        self.peripheral = peripheral
        if advertiseData != nil {
            leMeta = GAPMeta(data: advertiseData!)
        }
    }
    
    func toPigeon() -> PebbleScanDevicePigeon {
        let pigeon = PebbleScanDevicePigeon()
        pigeon.name = peripheral.name
        pigeon.address = NSNumber(value: peripheral.identifier.uuidString.hashValue)
        
        if leMeta?.major != nil {
            pigeon.version = "\(leMeta!.major!).\(leMeta!.minor!).\(leMeta!.patch!)"
        }
        
        pigeon.serialNumber = leMeta?.serialNumber
        
        if leMeta?.color != nil {
            pigeon.color = NSNumber(value: leMeta!.color!)
        }
        
        if leMeta?.runningPRF != nil {
            pigeon.runningPRF = NSNumber(value: leMeta!.runningPRF!)
        }
        
        if leMeta?.firstUse != nil {
            pigeon.firstUse = NSNumber(value: leMeta!.firstUse!)
        }
        return pigeon
    }
    
    func toStoredPebbleDevice() -> StoredPebbleDevice {
        return StoredPebbleDevice(name: self.peripheral.name!, identifier: self.peripheral.identifier, serialNumber: self.leMeta?.serialNumber, color: self.leMeta?.color)
    }
}
