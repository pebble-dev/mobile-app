//
//  ProtocolService.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import libpebblecommon
import CocoaLumberjackSwift
class ProtocolComms {
    static var shared: ProtocolComms!
    
    private let protocolHandler = ProtocolHandlerImpl()
    
    private let rawSend = RawSend()
    
    public let systemService: SystemService
    public let appFetchService: AppFetchService
    public let putBytesService: PutBytesService
    
    public let systemHandler: SystemHandler
    public let appInstallHandler: AppInstallHandler
    public let putBytesController: PutBytesController
    
    init() {
        systemService = SystemService(protocolHandler: protocolHandler)
        appFetchService = AppFetchService(protocolHandler: protocolHandler)
        putBytesService = PutBytesService(protocolHandler: protocolHandler)
        
        systemHandler = SystemHandler(systemService: systemService)
        self.appInstallHandler = AppInstallHandler(appFetchService: appFetchService)
        
        putBytesController = PutBytesController(putBytesService: putBytesService)
    }
    
    private class RawSend: KotlinSuspendFunction1 {
        func invoke(p1: Any?, completionHandler: @escaping (Any?, Error?) -> Void) {
            let bytearray = KUtil.shared.uByteArrayAsByteArray(arr: p1!)
            do {
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
    }
    
    public func receivePacket(packet: [UInt8]) {
        let bytes = KotlinByteArray.init(size: Int32(packet.count))
        for (i, b) in packet.enumerated() {
            bytes.set(index: Int32(i), value: Int8(bitPattern: b))
        }
        let ubytes = KUtil.shared.byteArrayAsUByteArray(arr: bytes)
        
        protocolHandler.receivePacket(bytes: ubytes) {_, _ in }
    }
    
    public func sendPacket(packet: PebblePacket, completionHandler: @escaping (Bool, Error?) -> Void) {
        DispatchQueue.main.async {[self] in
            protocolHandler.send(packet: packet, priority: .normal) { success, err in
                completionHandler(success?.boolValue ?? false, err)
            }
        }
    }
    
    @available(iOS 13.0.0, *)
    public func sendPacketAsync(packet: PebblePacket) async throws -> Bool {
        return try await withCheckedThrowingContinuation { continuation in
            sendPacket(packet: packet) { result, error in
                if let error = error {
                    continuation.resume(throwing: error)
                    return
                }
                continuation.resume(returning: result)
            }
        }
    }
    
    public func startPacketSendingLoop() {
        DispatchQueue.main.async {[self] in
            protocolHandler.startPacketSendingLoop(rawSend: rawSend) { _, e in
                DDLogInfo("Sending loop ended")
            }
        }
    }
}
