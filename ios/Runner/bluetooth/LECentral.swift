//
//  LECentral.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import CobbleLE
import CoreBluetooth
import CocoaLumberjackSwift

class LECentral {
    static let shared = LECentral()
    static let scanDuration: TimeInterval = 30
    
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
    
    public var currentDevice: BluePebbleDevice? {
        get {
            if (connected) {
                return targetDevice
            } else {
                return nil
            }
        }
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
    
    @available(iOS 13.0, *)
    func waitForReadyAsync() async {
        return await withCheckedContinuation { continuation in
            waitForReady() {
                continuation.resume(returning: ())
            }
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
        queue.asyncAfter(deadline: .now()+LECentral.scanDuration) {
            self.centralController.stopScan()
            scanEnded()
        }
        return true
    }
    
    func getAssociatedWatchFromIdentifier(identifier: UUID) -> BluePebbleDevice? {
        var device = scannedDevices.first {
            $0.peripheral.identifier == identifier
        }
        if device == nil {
            DDLogDebug(PersistentStorage.shared.devices)
            if let stored = PersistentStorage.shared.devices.first(where: {device in
                return device.identifier == identifier
            }) {
                if let periph = centralController.centralManager.retrievePeripherals(withIdentifiers: [stored.identifier]).first {
                    device = BluePebbleDevice(peripheral: periph, advertiseData: nil)
                }
            }
        }
        return device
    }
    
    func connectToWatchHash(watchIdentifier: UUID, onConnectState: @escaping (Bool) -> ()) {
        let device = getAssociatedWatchFromIdentifier(identifier: watchIdentifier)
        ProtocolComms.shared.systemHandler.waitNegotiationComplete { [self] in
            connected = true
            connStateCallback?(true)
        }
        if let device = device {
            targetDevice = device
            if let serv = LEPeripheral.shared.gattService {
                serv.targetWatchIdentifier = watchIdentifier
            }
            centralController.stopScan()
            connStateCallback = onConnectState
            centralController.centralManager.cancelPeripheralConnection(device.peripheral)
            peripheralClient = LEClientController(peripheral: device.peripheral, centralManager: centralController.centralManager, stateCallback: connStatusChange)
            peripheralClient!.connect()
            ProtocolComms.shared.startPacketSendingLoop()
        }else {
            onConnectState(false)
            connected = false
            DDLogError("LECentral: Couldn't find peripheral from scanned devices or iOS side")
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
            DDLogError("LECentral: Error \(connStatus.pairingErrorCode)")
        } else if connStatus.connected == true && connStatus.paired == true {
            DDLogInfo("LECentral: Connected")
            if let targetDevice = targetDevice {
                if !PersistentStorage.shared.devices.contains(where: { $0.identifier == targetDevice.peripheral.identifier }) {
                    var devs = PersistentStorage.shared.devices
                    devs.append(targetDevice.toStoredPebbleDevice())
                    PersistentStorage.shared.devices = devs
                }
            }
        }else if connStatus.connected == true && connStatus.paired == false {
            DDLogInfo("LECentral: Pairing")
        }else {
            DDLogInfo("LECentral: Connecting")
        }
    }
}
