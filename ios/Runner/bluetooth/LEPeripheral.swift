//
//  LEPeripheral.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import CobbleLE
class LEPeripheral {
    static var shared: LEPeripheral!
    
    let peripheralController: LEPeripheralController
    var gattService: PPoGATTService?
    
    init() {
        peripheralController = LEPeripheralController()
        peripheralController.waitForReady { [self] in
            gattService = PPoGATTService(serverController: peripheralController, packetHandler: packetHandler)
        }
    }
    
    private func packetHandler(rawPacket: [UInt8]) {
        ProtocolService.shared.receivePacket(packet: rawPacket)
    }
    
    public func writePacket(rawProtocolPacket: [UInt8]) throws {
        if let gattService = gattService {
            gattService.write(rawProtocolPacket: rawProtocolPacket)
        }else {
            throw PPoGATTServiceNotInitialized()
        }
    }
}
