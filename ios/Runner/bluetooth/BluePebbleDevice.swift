//
//  BluePebbleDevice.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import CoreBluetooth
class BluePebbleDevice {
    let peripheral: CBPeripheral
    var leMeta: LEMeta?
    
    init(peripheral: CBPeripheral, advertiseData: [UInt8]?) {
        self.peripheral = peripheral
        if advertiseData != nil {
            leMeta = LEMeta(rawData: advertiseData!)
        }
    }
}
