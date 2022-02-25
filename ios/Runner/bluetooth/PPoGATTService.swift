//
//  PPoGATTService.swift
//
//
//  Created by crc32 on 15/10/2021.
//

import Foundation
import CoreBluetooth
import CobbleLE
import libpebblecommon

/// Handles hosting a PPoGATT service on the device (peripheral mode) for paired Pebbles to connect to
public class PPoGATTService {
    let serverController: LEPeripheralController
    
    private var deviceCharacteristic: CBMutableCharacteristic!
    
    // ?, connection version, ? ...
    private let metaResponse: [UInt8] = [0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    //private let metaResponse: [UInt8] = [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]
    
    private var seq = 0
    private var remoteSeq = 0
    private var currentRXPend = 0
    private var maxPayload = 25
    
    private var lastAck: GATTPacket?
    private var delayedAckJob: DispatchWorkItem?
    
    private var initialReset = false
    private var connectionVersion: GATTPacket.PPoGConnectionVersion = .zero
    private var maxRXWindow: UInt8 = UInt8(LEConstants.shared.MAX_RX_WINDOW)
    private var maxTXWindow: UInt8 = UInt8(LEConstants.shared.MAX_TX_WINDOW)
    private var pendingPackets = [GATTPacket]()
    private var ackPending = Dictionary<Int32, DispatchSemaphore>()
    
    private let queue = DispatchQueue.global(qos: .utility)
    private let highPrioQueue = DispatchQueue.global(qos: .userInteractive)
    private let packetWriteSemaphore = DispatchSemaphore(value: 1)
    private let packetReadSemaphore = DispatchSemaphore(value: 1)
    private let dataUpdateSemaphore = DispatchSemaphore(value: 1)
    
    private let packetHandler: ([UInt8]) -> ()
    private var pendingLength = 0
    private var receiveBuf = [UInt8]()
    
    public var targetWatchHash: Int?
    
    /// - Parameters:
    ///   - serverController: Server controller to add the service to
    ///   - packetHandler: Callback for receiving Pebble protocol packets
    public init(serverController: LEPeripheralController, packetHandler: @escaping ([UInt8]) -> ()) {
        self.serverController = serverController
        self.packetHandler = packetHandler
        serverController.removeAllServices()
        let service = CBMutableService(type: CBUUID(string: LEConstants.UUIDs.shared.PPOGATT_DEVICE_SERVICE_UUID_SERVER), primary: true)
        let metaCharacteristic = CBMutableCharacteristic(type: CBUUID(string: LEConstants.UUIDs.shared.META_CHARACTERISTIC_SERVER), properties: .read, value: nil, permissions: [.readable])
        deviceCharacteristic = CBMutableCharacteristic(type: CBUUID(string: LEConstants.UUIDs.shared.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), properties: [.writeWithoutResponse, .notify], value: nil, permissions: [.writeable])
        //let dummyService = CBMutableService(type: CBUUID(string: libpebblecommon.LEConstants.UUIDs.shared.FAKE_SERVICE_UUID), primary: true)
        //dummyService.characteristics = [CBMutableCharacteristic(type: CBUUID(string: libpebblecommon.LEConstants.UUIDs.shared.FAKE_SERVICE_UUID), properties: .read, value: nil, permissions: .readable)]
        service.characteristics = [metaCharacteristic, deviceCharacteristic]
        
        serverController.addService(service: service) { error in
            if error != nil {
                print("GATTService: Error adding service: \(error!.localizedDescription)")
            }else {
                print("GATTService: Added service")
            }
        }
        /*serverController.addService(service: dummyService) { error in
            if error != nil {
                print("GATTService: Error adding dummy service: \(error!.localizedDescription)")
            }else {
                print("GATTService: Added dummy service")
            }
        }*/
        
        let applaunchService = CBMutableService(type: CBUUID(string: LEConstants.UUIDs.shared.APPLAUNCH_SERVICE_UUID), primary: true)
        applaunchService.characteristics = [CBMutableCharacteristic(type: CBUUID(string: LEConstants.UUIDs.shared.APPLAUNCH_CHARACTERISTIC), properties: .read, value: Data(), permissions: [.readable])]
        
        serverController.addService(service: applaunchService) { error in
            if error != nil {
                print("GATTService: Error adding applaunch service: \(error!.localizedDescription)")
            }else {
                print("GATTService: Added applaunch service")
            }
        }
        
        serverController.setCharacteristicCallback(uuid: CBUUID(string: LEConstants.UUIDs.shared.META_CHARACTERISTIC_SERVER), onRead: self.onMetaRead)
        serverController.setCharacteristicCallback(uuid: CBUUID(string: LEConstants.UUIDs.shared.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), onWrite: self.onWrite)
        serverController.setCharacteristicCallback(uuid: CBUUID(string: LEConstants.UUIDs.shared.PPOGATT_DEVICE_CHARACTERISTIC_SERVER), onSubscribe: self.onDataSubscribed)
    }
    
    private func getNextSeq(current: Int) -> Int {
        return (current + 1) % 32
    }
    
    /// Reads total length from the header of a protocol packet
    /// - Parameter packet: Packet or first chunk of packet
    /// - Returns: Length of the provided packet
    private func getPacketLength(packet: [UInt8]) -> Int {
        let bytes = KotlinByteArray.init(size: Int32(packet.count))
        for (i, b) in packet.enumerated() {
            bytes.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        let ubytes = KUtil.shared.byteArrayAsUByteArray(arr: bytes)
        
        let headBuf = DataBuffer(bytes: ubytes)
        let length = headBuf.getUShort()
        return Int(length)
    }
    
    /// Called on write to PPoGATT device characteristic
    /// - Parameter requests: The ATT write request(s) holding written data
    private func onWrite(requests: [CBATTRequest]) {
        queue.async { [self] in
            packetReadSemaphore.wait()
            for request in requests {
                if request.central.identifier.uuidString.hashValue != targetWatchHash {
                    break
                }
                if request.value != nil {
                    let packet = GATTPacket(data: KUtil.shared.byteArrayFromNative(arr: request.value!))
                    print("-> \(packet.sequence)")
                    switch packet.type {
                    case .data:
                        if packet.sequence == remoteSeq {
                            sendAck(sequence: UInt8(packet.sequence))
                            remoteSeq = getNextSeq(current: remoteSeq)
                            let nData = packet.toByteArray().toNative().advanced(by: 1)
                            if receiveBuf.isEmpty {
                                pendingLength = getPacketLength(packet: [UInt8](nData))
                            }
                            receiveBuf.append(contentsOf: nData)
                            
                            // Handle like a stream, multiple protocol packets / packet boundaries can be in a single gatt chunk
                            while receiveBuf.count >= pendingLength {
                                let range = ...(pendingLength+3)
                                DispatchQueue.main.sync {
                                    packetHandler([UInt8](receiveBuf[range]))
                                }
                                receiveBuf.removeSubrange(range)
                                if !receiveBuf.isEmpty {
                                    pendingLength = getPacketLength(packet: receiveBuf)
                                }
                            }
                        }else {
                            print("Unexpected sequence \(packet.sequence), expected \(remoteSeq)")
                            if let lastAck = lastAck {
                                print("Sending clarifying ACK (\(lastAck.sequence))")
                                sendAck(sequence: UInt8(lastAck.sequence))
                                writePacket(type: .ack, data: nil, sequence: UInt8(lastAck.sequence))
                            }else {
                                assertionFailure("No previous ACK to send on sequence mismatch")
                            }
                        }
                        
                    case .ack:
                        for i in 0...packet.sequence {
                            let ind = ackPending.index(forKey: i)
                            if ind != nil {
                                ackPending.remove(at: ind!).value.signal()
                                updateData()
                            }
                        }
                        print("GATTService: Got ACK for \(packet.sequence)")
                        
                    case .reset:
                        assert(seq == 0, "GATTService: Got reset on non zero sequence")
                        connectionVersion = packet.getPPoGConnectionVersion()
                        sendResetAck()
                    case .resetAck:
                        print("GATTService: Got reset ACK")
                        print("GATTService: Connection version \(connectionVersion.value)")
                        if connectionVersion.supportsWindowNegotiation, !packet.hasWindowSizes() {
                            print("GATTService: FW does not support window sizes in reset complete, reverting to connectionVersion 0")
                            connectionVersion = .zero
                        }
                        
                        if connectionVersion.supportsWindowNegotiation {
                            maxRXWindow = min(UInt8(bitPattern: packet.getMaxRXWindow()), UInt8(LEConstants.shared.MAX_RX_WINDOW))
                            maxTXWindow = min(UInt8(bitPattern: packet.getMaxTXWindow()), UInt8(LEConstants.shared.MAX_TX_WINDOW))
                            print("GATTService: Windows negotiated: rx = \(maxRXWindow), tx = \(maxTXWindow)")
                        }
                        sendResetAck()
                        if !initialReset {
                            print("Initial reset, everything is connected now")
                        }
                        initialReset = true
                    default:
                        assertionFailure()
                    }
                }
            }
            packetReadSemaphore.signal()
        }
    }
    
    /// Sends a reset request GATT packet to the pebble
    func requestReset() {
        writePacket(type: .reset, data: [UInt8(bitPattern: connectionVersion.value)], sequence: 0)
    }
    
    /// Sends an ACK GATT packet to the pebble
    /// - Parameter sequence: The sequence of the packet to acknowledge
    private func sendAck(sequence: UInt8) {
        if !connectionVersion.supportsCoalescedAcking {
            currentRXPend = 0
            writePacket(type: .ack, data: nil, sequence: sequence)
        }else {
            currentRXPend += 1
            delayedAckJob?.cancel()
            if currentRXPend >= maxRXWindow / 2 {
                currentRXPend = 0
                writePacket(type: .ack, data: nil, sequence: sequence)
            }else {
                delayedAckJob = DispatchWorkItem { [self] in
                    currentRXPend = 0
                    writePacket(type: .ack, data: nil, sequence: sequence)
                }
                highPrioQueue.asyncAfter(deadline: .now() + 0.5, execute: delayedAckJob!)
            }
        }
    }
    
    /// Sends a reset ACK GATT packet to the pebble and resets the PPoGATT service
    private func sendResetAck() {
        writePacket(type: .resetAck, data: connectionVersion.supportsWindowNegotiation ? [maxRXWindow, maxTXWindow] : nil, sequence: 0)
        reset()
    }
    
    /// Non-blocking function to update the PPoGATT device characteristic's value with new data that was pending (TX to pebble)
    private func updateData() {
        highPrioQueue.async { [self] in
            dataUpdateSemaphore.wait()
            if !pendingPackets.isEmpty {
                if ackPending.count-1 < maxTXWindow {
                    let packet = pendingPackets.removeFirst()
                    print(packet.sequence)
                    serverController.updateValue(value: packet.toByteArray().toNative(), forChar: deviceCharacteristic) {
                        dataUpdateSemaphore.signal()
                    }
                }else {
                    dataUpdateSemaphore.signal()
                }
            }else {
                dataUpdateSemaphore.signal()
            }
        }
    }
    
    /// Called on read of the PPoGATT meta characteristic, which is done as part of the connection handshake
    /// - Parameter request: The ATT read request
    private func onMetaRead(request: CBATTRequest) {
        if request.central.identifier.uuidString.hashValue == targetWatchHash {
            serverController.respond(to: request, withResult: .success, data: Data(metaResponse))
        }else {
            maxPayload = request.central.maximumUpdateValueLength
            serverController.respond(to: request, withResult: .insufficientAuthorization, data: nil)
        }
        
        print("GATTService: Meta read")
    }
    
    
    /// Called when the central (Pebble) subscribes to the PPoGATT data characteristic, used as an indicator the central is ready to start the connection handshake
    /// - Parameter central: The central that subscribed
    private func onDataSubscribed(central: CBCentral) {
        print("GATTService: Data subscribed")
        if central.identifier.uuidString.hashValue == targetWatchHash {
            maxPayload = central.maximumUpdateValueLength
            requestReset()
        }
    }
    
    /// Sends PPoGATT packets to the Pebble
    /// - Parameters:
    ///   - type: The packet type
    ///   - data: The packet body
    ///   - sequence: The sequence of the packet
    private func writePacket(type: GATTPacket.PacketType, data: [UInt8]?, sequence: UInt8? = nil) {
        queue.async { [self] in
            let timeout = packetWriteSemaphore.wait(timeout: .now() + 3)
            assert(timeout == .success, "Timed out waiting for packetWrite unlock")
            let kData = KUtil.shared.byteArrayFromNative(arr: Data(data ?? []))
            let packet = GATTPacket(type: type, sequence: Int32(sequence ?? UInt8(seq)), data: kData.size > 0 ? kData : nil)
            if type == .ack {
                print("<- ACK \(packet.sequence)")
                lastAck = packet
            }
            if type == .data {
                highPrioQueue.async {
                    dataUpdateSemaphore.wait()
                    let sem = DispatchSemaphore(value: 0)
                    ackPending[packet.sequence] = sem
                    pendingPackets.append(packet)
                    if sequence == nil {
                        self.seq = self.getNextSeq(current: self.seq)
                    }
                    dataUpdateSemaphore.signal()
                    updateData()
                    if ackPending.count >= maxTXWindow {
                        sem.wait()
                    }
                    packetWriteSemaphore.signal()
                }
            }else {
                serverController.updateValue(value: packet.data.toNative(), forChar: deviceCharacteristic) {
                    packetWriteSemaphore.signal()
                }
            }
        }
    }
    
    /// Sends Pebble protocol packets to the Pebble
    /// - Parameter rawProtocolPacket: The raw serialized bytes of the packet
    public func write(rawProtocolPacket: [UInt8]) {
        let maxPacketSize = maxPayload-1
        var chunked = rawProtocolPacket.chunked(into: maxPacketSize)
        print("Writing packet of length \(rawProtocolPacket.count) in \(chunked.count) chunks")
        while !chunked.isEmpty {
            writePacket(type: .data, data: chunked.removeFirst())
        }
    }
    
    /// Resets the PPoGATT service
    private func reset() {
        DispatchQueue.main.sync { [self] in
            print("GATTService: Resetting LE")
            remoteSeq = 0
            seq = 0
            lastAck = nil
            pendingPackets.removeAll()
            ackPending.removeAll()
        }
    }
}
