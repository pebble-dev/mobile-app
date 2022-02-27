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
            self.send(packet: packet, completionHandler: kotlinSuspendResolver(seal: seal))
        }
    }
}
