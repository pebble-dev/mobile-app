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
import CocoaLumberjackSwift
import PromiseKit

/// Handles hosting a PPoGATT service on the device (peripheral mode) for paired Pebbles to connect to
public class PPoGATTService {
    let serverController: LEPeripheralController
    
    private var deviceCharacteristic: CBMutableCharacteristic!
    
    enum GATTIOException : CobbleError {
        case timeout(String)
        
        var message: String {
            switch self {
            case .timeout(let message):
                return message
            }
        }
    }
    
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
    
    private let rxQueue = DispatchQueue(label: Bundle.main.bundleIdentifier!+".PPoGATTServiceRXQueue", qos: .utility)
    private let txQueue = DispatchQueue(label: Bundle.main.bundleIdentifier!+".PPoGATTServiceTXQueue", qos: .utility)
    private let chunkWriteQueue = DispatchQueue(label: Bundle.main.bundleIdentifier!+".PPoGATTServicePPChunkQueue", qos: .utility)
    
    private let packetHandler: ([UInt8]) -> ()
    private var pendingLength = 0
    private var receiveBuf = [UInt8]()
    
    public var targetWatchIdentifier: UUID?
    
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
                DDLogError("GATTService: Error adding service: \(error!.localizedDescription)")
            }else {
                DDLogDebug("GATTService: Added service")
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
                DDLogError("GATTService: Error adding applaunch service: \(error!.localizedDescription)")
            }else {
                DDLogDebug("GATTService: Added applaunch service")
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
        rxQueue.async { [self] in
            /*let timeout = packetReadSemaphore.wait(timeout: .now() + 5)
            assert(timeout == .success, "Timed out waiting for packetRead unlock")
            defer {
                packetReadSemaphore.signal()
            }*/
            for request in requests {
                if request.central.identifier != targetWatchIdentifier {
                    break
                }
                if request.value != nil {
                    let packet = GATTPacket(data: KUtil.shared.byteArrayFromNative(arr: request.value!))
                    switch packet.type {
                    case .data:
                        DDLogDebug("-> DATA \(packet.sequence)")
                        if packet.sequence == remoteSeq {
                            sendAck(sequence: UInt8(packet.sequence)).cauterize()
                            remoteSeq = getNextSeq(current: remoteSeq)
                            let nData = packet.toByteArray().toNative().advanced(by: 1)
                            if receiveBuf.isEmpty {
                                pendingLength = getPacketLength(packet: [UInt8](nData))
                            }
                            receiveBuf.append(contentsOf: nData)
                            
                            // Handle like a stream, multiple protocol packets / packet boundaries can be in a single gatt chunk
                            while receiveBuf.count >= pendingLength+4 {
                                let range = ...(pendingLength+3)
                                packetHandler([UInt8](receiveBuf[range]))
                                receiveBuf.removeSubrange(range)
                                if !receiveBuf.isEmpty {
                                    pendingLength = getPacketLength(packet: receiveBuf)
                                }
                            }
                        }else {
                            DDLogWarn("Unexpected sequence \(packet.sequence), expected \(remoteSeq)")
                            if let lastAck = lastAck {
                                DDLogInfo("Sending clarifying ACK (\(lastAck.sequence))")
                                writePacket(type: .ack, data: nil, sequence: UInt8(lastAck.sequence)).cauterize()
                            }else {
                                DDLogWarn("No previous ACK to send on sequence mismatch")
                            }
                        }
                        
                    case .ack:
                        DDLogDebug("-> ACK \(packet.sequence)")
                        for i in 0...packet.sequence {
                            let ind = ackPending.index(forKey: i)
                            if ind != nil {
                                ackPending.remove(at: ind!).value.signal()
                                updateData()
                            }
                        }
                        DDLogDebug("GATTService: Got ACK for \(packet.sequence)")
                        
                    case .reset:
                        DDLogDebug("-> RESET \(packet.sequence)")
                        if (seq != 0) {
                            DDLogError("GATTService: Got reset on non zero sequence")
                        }
                        connectionVersion = packet.getPPoGConnectionVersion()
                        sendResetAck()
                    case .resetAck:
                        DDLogDebug("-> RESETACK \(packet.sequence)")
                        DDLogDebug("GATTService: Got reset ACK")
                        DDLogDebug("GATTService: Connection version \(connectionVersion.value)")
                        if connectionVersion.supportsWindowNegotiation, !packet.hasWindowSizes() {
                            DDLogWarn("GATTService: FW does not support window sizes in reset complete, reverting to connectionVersion 0")
                            connectionVersion = .zero
                        }
                        
                        if connectionVersion.supportsWindowNegotiation {
                            maxRXWindow = min(UInt8(bitPattern: packet.getMaxRXWindow()), UInt8(LEConstants.shared.MAX_RX_WINDOW))
                            maxTXWindow = min(UInt8(bitPattern: packet.getMaxTXWindow()), UInt8(LEConstants.shared.MAX_TX_WINDOW))
                            DDLogInfo("GATTService: Windows negotiated: rx = \(maxRXWindow), tx = \(maxTXWindow)")
                        }
                        sendResetAck()
                        if !initialReset {
                            DDLogInfo("Initial reset, everything is connected now")
                        }
                        initialReset = true
                    default:
                        assertionFailure()
                    }
                }
            }
        }
    }
    
    /// Sends a reset request GATT packet to the pebble
    func requestReset() {
        writePacket(type: .reset, data: [UInt8(bitPattern: connectionVersion.value)], sequence: 0).cauterize()
    }
    
    /// Sends an ACK GATT packet to the pebble
    /// - Parameter sequence: The sequence of the packet to acknowledge
    private func sendAck(sequence: UInt8) -> Promise<Void> {
        return Promise {seal in
            if !connectionVersion.supportsCoalescedAcking {
                currentRXPend = 0
                writePacket(type: .ack, data: nil, sequence: sequence).done {
                    seal.fulfill(())
                }.catch { error in
                    seal.reject(error)
                }
            }else {
                currentRXPend += 1
                delayedAckJob?.cancel()
                if currentRXPend >= maxRXWindow / 2 {
                    currentRXPend = 0
                    writePacket(type: .ack, data: nil, sequence: sequence).done {
                        seal.fulfill(())
                    }.catch { error in
                        seal.reject(error)
                    }
                }else {
                    delayedAckJob = DispatchWorkItem(qos: .userInteractive) { [self] in
                        currentRXPend = 0
                        writePacket(type: .ack, data: nil, sequence: sequence).cauterize()
                    }
                    seal.fulfill(())
                    txQueue.asyncAfter(deadline: .now() + 0.5, execute: delayedAckJob!)
                }
            }
        }
    }
    
    /// Sends a reset ACK GATT packet to the pebble and resets the PPoGATT service
    private func sendResetAck() {
        writePacket(type: .resetAck, data: connectionVersion.supportsWindowNegotiation ? [maxRXWindow, maxTXWindow] : nil, sequence: 0).catch { error in
            assertionFailure(error.localizedDescription)
        }
        reset()
    }
    
    /// Non-blocking function to update the PPoGATT device characteristic's value with new data that was pending (TX to pebble)
    private func updateData() {
        txQueue.async { [self] in
            if !pendingPackets.isEmpty {
                let packet = pendingPackets.removeFirst()
                serverController.updateValue(value: packet.toByteArray().toNative(), forChar: deviceCharacteristic) {
                    
                }
            }
        }
    }
    
    /// Non-blocking function to update the PPoGATT device characteristic's value with new meta packet (ACK etc.) that was pending (TX to pebble)
    private func updateMeta(packet: GATTPacket) -> Promise<Void> {
        return Promise {seal in
            serverController.updateValue(value: packet.data.toNative(), forChar: deviceCharacteristic) {
                seal.fulfill(())
            }
        }
    }
    
    /// Called on read of the PPoGATT meta characteristic, which is done as part of the connection handshake
    /// - Parameter request: The ATT read request
    private func onMetaRead(request: CBATTRequest) {
        if request.central.identifier == targetWatchIdentifier {
            serverController.respond(to: request, withResult: .success, data: Data(metaResponse))
        }else {
            maxPayload = request.central.maximumUpdateValueLength
            serverController.respond(to: request, withResult: .insufficientAuthorization, data: nil)
        }
        
        DDLogDebug("GATTService: Meta read")
    }
    
    
    /// Called when the central (Pebble) subscribes to the PPoGATT data characteristic, used as an indicator the central is ready to start the connection handshake
    /// - Parameter central: The central that subscribed
    private func onDataSubscribed(central: CBCentral) {
        DDLogDebug("GATTService: Data subscribed")
        if central.identifier == targetWatchIdentifier {
            maxPayload = central.maximumUpdateValueLength
            requestReset()
        }
    }
    
    /// Sends PPoGATT packets to the Pebble
    /// - Parameters:
    ///   - type: The packet type
    ///   - data: The packet body
    ///   - sequence: The sequence of the packet
    private func writePacket(type: GATTPacket.PacketType, data: [UInt8]?, sequence: UInt8? = nil) -> Promise<Void> {
        return Promise { seal in
            let kData = KUtil.shared.byteArrayFromNative(arr: Data(data ?? []))
            let packet = GATTPacket(type: type, sequence: Int32(sequence ?? UInt8(seq)), data: kData.size > 0 ? kData : nil)
            switch (type) {
            case .data:
                DDLogDebug("<- DATA \(packet.sequence)")
                let ackPendingSemaphore = DispatchSemaphore(value: 0)
                ackPending[packet.sequence] = ackPendingSemaphore
                
                if ackPending.count >= maxTXWindow {
                    DispatchQueue(label: "AckWaiter", qos: .background).async {
                        let timeout = ackPendingSemaphore.wait(timeout: .now()+5)
                        if (timeout == .timedOut) {
                            seal.reject(GATTIOException.timeout("Timed out waiting for ack"))
                        }else {
                            seal.fulfill(())
                        }
                    }
                }else {
                    seal.fulfill(())
                }
                
                pendingPackets.append(packet)
                if sequence == nil {
                    self.seq = self.getNextSeq(current: self.seq)
                }
                
                updateData()
                break
                
            case .reset:
                DDLogDebug("<- RESET \(packet.sequence)")
                updateMeta(packet: packet).pipe(to: seal.resolve)
                break
            
            case .resetAck:
                DDLogDebug("<- RESETACK \(packet.sequence)")
                updateMeta(packet: packet).pipe(to: seal.resolve)
                break
            
            case .ack:
                DDLogDebug("<- ACK \(packet.sequence)")
                lastAck = packet
                updateMeta(packet: packet).pipe(to: seal.resolve)
                break
            
            default:
                assertionFailure()
            }
        }
    }
    
    /// Sends Pebble protocol packets to the Pebble
    /// - Parameter rawProtocolPacket: The raw serialized bytes of the packet
    public func write(rawProtocolPacket: [UInt8]) -> Promise<Void> {
        return Promise { seal in
            let maxPacketSize = maxPayload-1
            var chunked = rawProtocolPacket.chunked(into: maxPacketSize)
            DDLogDebug("Writing packet of length \(rawProtocolPacket.count) in \(chunked.count) chunks")
            chunkWriteQueue.async {
                while !chunked.isEmpty {
                    do {
                        try self.writePacket(type: .data, data: chunked.removeFirst()).wait()
                    }catch {
                        seal.reject(error)
                    }
                }
                seal.fulfill(())
            }
        }
        
    }
    
    /// Resets the PPoGATT service
    private func reset() {
        DDLogInfo("GATTService: Resetting LE")
        remoteSeq = 0
        seq = 0
        lastAck = nil
        pendingPackets.removeAll()
        ackPending.removeAll()
    }
}
