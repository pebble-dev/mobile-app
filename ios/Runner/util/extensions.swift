//
//  extensions.swift
//  Runner
//
//  Created by crc32 on 27/02/2022.
//

import Foundation
import libpebblecommon
import PromiseKit

private func kotlinSuspendResolver<T>(seal: Resolver<T>) -> ((T?, Error?) -> Void) {
    return { res, error in
        seal.resolve(res, error)
    }
}

//MARK: - Coroutines extensions

extension Kotlinx_coroutines_coreChannel {
    func receivePromise() -> Promise<Any> {
        return Promise { seal in
            self.receive(completionHandler: kotlinSuspendResolver(seal: seal))
        }
    }
}

extension Kotlinx_coroutines_coreChannelIterator {
    func hasNextPromise() -> Promise<KotlinBoolean> {
        return Promise { seal in
            self.hasNext(completionHandler: kotlinSuspendResolver(seal: seal))
        }
    }
}

//MARK: - Protocol service extensions

extension PutBytesService {
    func sendPromise(packet: PutBytesOutgoingPacket) -> Promise<KotlinUnit> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.send(packet: packet, completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
    
    func sendAppPartPromise(appId: UInt32, blob: Data,
                            watchType: WatchType, watchVersion: WatchVersion.WatchVersionResponse,
                            manifestEntry: PbwBlob, type: ObjectType) -> Promise<Void> {
        return Promise {seal in
            DispatchQueue.main.async {
                self.sendAppPart(appId: appId,
                                 blob: KUtil.shared.byteArrayFromNative(arr: blob),
                                 watchType: watchType,
                                 watchVersion: watchVersion,
                                 manifestEntry: manifestEntry,
                                 type: type,
                                 completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.asVoid()
    }
}

extension SystemService {
    func requestWatchModelPromise() -> Promise<Int> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.requestWatchModel(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { $0.intValue }
    }
    
    func requestWatchVersionPromise() -> Promise<WatchVersion.WatchVersionResponse> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.requestWatchVersion(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
    
    func sendPromise(packet: SystemPacket, priority: PacketPriority) -> Promise<Void> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.send(packet: packet, priority: priority, completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { _ in}
    }
}

//MARK: - Misc. extensions

extension ProtocolHandlerImpl {
    func openProtocolPromise() -> Promise<Void> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.openProtocol(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { _ in }
    }
    
    func closeProtocolPromise() -> Promise<Void> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.closeProtocol(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { _ in }
    }
    
    func waitForNextPacketPromise() -> Promise<ProtocolHandlerPendingPacket> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.waitForNextPacket(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
}
