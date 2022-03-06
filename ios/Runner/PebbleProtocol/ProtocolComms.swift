//
//  ProtocolService.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import libpebblecommon
import CocoaLumberjackSwift
import PromiseKit

class ProtocolComms {
    static var shared: ProtocolComms!
    
    private let protocolHandler = ProtocolHandlerImpl()
    
    //private let rawSend = RawSend()
    
    public let systemService: SystemService
    public let appFetchService: AppFetchService
    public let putBytesService: PutBytesService
    public let blobDBService: BlobDBService
    
    public let systemHandler: SystemHandler
    public let appInstallHandler: AppInstallHandler
    public let putBytesController: PutBytesController
    
    private let ioDispatch = DispatchQueue(label: Bundle.main.bundleIdentifier!+".ProtocolCommsIO", qos: .default)
    private var sendLoopRunning = false
    
    init() {
        systemService = SystemService(protocolHandler: protocolHandler)
        appFetchService = AppFetchService(protocolHandler: protocolHandler)
        putBytesService = PutBytesService(protocolHandler: protocolHandler)
        blobDBService = BlobDBService(protocolHandler: protocolHandler)
        
        systemHandler = SystemHandler(systemService: systemService)
        self.appInstallHandler = AppInstallHandler(appFetchService: appFetchService)
        
        putBytesController = PutBytesController(putBytesService: putBytesService)
    }
    
    /*private class RawSend: KotlinSuspendFunction1 {
        func invoke(p1: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
            let bytearray = KUtil.shared.uByteArrayAsByteArray(arr: p1!)
            do {
                DDLogDebug("WRITING: \(bytearray.size)")
                try LEPeripheral.shared.writePacket(rawProtocolPacket: [UInt8](bytearray.toNative()))
            }catch is PPoGATTServiceNotInitialized {
                DDLogWarn("Tried to send packet before GATT was init'd")
                assertionFailure()
                completionHandler(false, nil)
                return
            }catch {
                DDLogError(error)
                completionHandler(false, nil)
                return
            }
            completionHandler(true, nil)
        }
    }*/
    
    private func rawSend(bytearray: KotlinByteArray) -> Bool {
        do {
            try LEPeripheral.shared.writePacket(rawProtocolPacket: [UInt8](bytearray.toNative()))
            return true
        }catch is PPoGATTServiceNotInitialized {
            DDLogWarn("Tried to send packet before GATT was init'd")
            assertionFailure()
            return false
        }catch {
            DDLogError(error)
            return false
        }
    }
    
    public func receivePacket(packet: [UInt8]) {
        let bytes = KotlinByteArray.init(size: Int32(packet.count))
        for (i, b) in packet.enumerated() {
            bytes.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        let ubytes = KUtil.shared.byteArrayAsUByteArray(arr: bytes)
        
        DispatchQueue.main.async {
            self.protocolHandler.receivePacket(bytes: ubytes) {_, _ in }
        }
    }
    
    public func sendPacket(packet: PebblePacket) -> Promise<Bool> {
        return Promise { seal in
            DispatchQueue.main.async {[self] in
                protocolHandler.send(packet: packet, priority: .normal) { success, err in
                    seal.resolve(success?.boolValue, err)
                }
            }
        }
    }
    
    @available(iOS 13.0.0, *)
    public func sendPacketAsync(packet: PebblePacket) async throws -> Bool {
        return try await withCheckedThrowingContinuation { continuation in
            sendPacket(packet: packet).done { result in
                continuation.resume(returning: result)
            }.catch { error in
                continuation.resume(throwing: error)
            }
        }
    }
    
    public func startPacketSendingLoop() {
        guard !sendLoopRunning else {
            DDLogWarn("Ignored call on startPacketSendingLoop(): Already running")
            return
        }
        sendLoopRunning = true
        DispatchQueue.main.async {[self] in
            firstly {
                protocolHandler.openProtocolPromise()
            }.done {
                ioDispatch.async {
                    defer {
                        protocolHandler.closeProtocolPromise().cauterize()
                        sendLoopRunning = false
                    }
                    while (true) {
                        do {
                            let packet = try protocolHandler.waitForNextPacketPromise().wait()
                            let success = rawSend(bytearray: KUtil.shared.uByteArrayAsByteArray(arr: packet.data))
                            
                            if (success) {
                                packet.notifyPacketStatus(success: true)
                            }else {
                                packet.notifyPacketStatus(success: false)
                                break
                            }
                        } catch {
                            DDLogError("Error in sending loop: \(error)")
                            break
                        }
                    }
                }
                DDLogInfo("Sending loop ended")
            }.catch { error in
                sendLoopRunning = false
            }
        }
    }
}
