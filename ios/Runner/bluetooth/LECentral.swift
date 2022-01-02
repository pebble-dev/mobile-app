//
//  LECentral.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import CobbleLE
import CoreBluetooth

class LECentral {
    static let shared = LECentral()
    
    let centralController: LECentralController
    
    private var scannedDevices = [BluePebbleDevice]()
    private let readyGroup = DispatchGroup()
    private let queue = DispatchQueue(label: "io.rebble.cobble.bluetooth.LECentral", qos: .utility)
    
    private var peripheralClient: LEClientController?
    private var connStateCallback: ((Bool) -> ())?
    private var targetDevice: BluePebbleDevice?
    
    private var connected = false
    
    private init() {
        readyGroup.enter()
        centralController = LECentralController()
        centralController.stateUpdateCallback = stateUpdate
    }
    
    private func stateUpdate(manager: CBCentralManager) {
        switch manager.state {
        case .poweredOn:
            readyGroup.leave()
        default:
            break
        }
    }
    
    func isConnected() -> Bool {
        return connected
    }
    
    func waitForReady(onReady: @escaping () -> ()) {
        readyGroup.notify(queue: queue) {
            onReady()
        }
    }
    
    func scan(foundDevices: @escaping ([BluePebbleDevice]) -> (), scanEnded: @escaping () -> ()) -> Bool {
        if centralController.centralManager.state != .poweredOn {
            return false
        }
        centralController.startScan() { peripheral, rssi, advData in
            if peripheral.name != nil, peripheral.name!.starts(with: "Pebble") || peripheral.name!.starts(with: "Pebble-LE") {
                let ind = self.scannedDevices.firstIndex { device in
                    return device.peripheral.identifier == peripheral.identifier
                }
                if ind == nil {
                    self.scannedDevices.append(BluePebbleDevice(peripheral: peripheral, advertiseData: advData))
                }else if self.scannedDevices[ind!].leMeta?.color == nil {
                    self.scannedDevices[ind!] = BluePebbleDevice(peripheral: peripheral, advertiseData: advData)
                }
                foundDevices(self.scannedDevices)
            }
        }
        queue.asyncAfter(deadline: .now()+30) {
            self.centralController.stopScan()
            scanEnded()
        }
        return true
    }
    
    func connectToWatchHash(watchHash: Int, onConnectState: @escaping (Bool) -> ()) {
        var device = scannedDevices.first {
            $0.peripheral.identifier.uuidString.hashValue == watchHash
        }
        if device == nil {
            if let stored = PersistentStorage.shared.devices.first(where: {device in
                return device.identifier.uuidString.hashValue == watchHash
            }) {
                if let periph = centralController.centralManager.retrievePeripherals(withIdentifiers: [stored.identifier]).first {
                    device = BluePebbleDevice(peripheral: periph, advertiseData: nil)
                }
            }
        }
        
        
        if let device = device {
            targetDevice = device
            if let serv = LEPeripheral.shared.gattService {
                serv.targetWatchHash = watchHash
            }
            centralController.stopScan()
            connStateCallback = onConnectState
            centralController.centralManager.cancelPeripheralConnection(device.peripheral)
            peripheralClient = LEClientController(peripheral: device.peripheral, centralManager: centralController.centralManager, stateCallback: connStatusChange)
            peripheralClient!.connect()
            ProtocolService.shared.startPacketSendingLoop()
        }else {
            onConnectState(false)
            connected = false
            print("LECentral: Couldn't find peripheral from scanned devices or iOS side")
        }
    }
    
    func disconnect() {
        peripheralClient?.disconnect()
    }
    
    private func connStatusChange(connStatus: ConnectivityStatus) {
        if connStatus.pairingErrorCode != .noError {
            peripheralClient?.disconnect()
            connStateCallback?(false)
            connected = false
            print("LECentral: Error \(connStatus.pairingErrorCode)")
        } else if connStatus.connected == true && connStatus.paired == true {
            print("LECentral: Connected")
            connStateCallback?(true)
            connected = true
            if let targetDevice = targetDevice {
                if !PersistentStorage.shared.devices.contains(where: {dev in dev.identifier == targetDevice.peripheral.identifier}) {
                    var devs = PersistentStorage.shared.devices
                    devs.append(targetDevice.toStoredPebbleDevice())
                    PersistentStorage.shared.devices = devs
                    print(PersistentStorage.shared.devices)
                }
            }
        }else if connStatus.connected == true && connStatus.paired == false {
            print("LECentral: Pairing")
        }else {
            print("LECentral: Connecting")
        }
    }
}
