//
//  PromiseTimeout.swift
//  Runner
//
//  Created by crc32 on 06/03/2022.
//

import Foundation
import PromiseKit

class PromiseTimeoutError: LocalizedError {
    var errorDescription = "Promise timed out"
}

public func withTimeout<T>(timeoutMs: UInt, promise: Promise<T>) -> Promise<T> {
    return Promise { seal in
        var rejected = false
        after(seconds: Double(timeoutMs)/1000).done {
            rejected = true
            seal.reject(PromiseTimeoutError())
        }
        promise.done { value in
            if (!rejected) {
                seal.fulfill(value)
            }
        }.cauterize()
    }
}

public func withTimeoutOrNull<T>(timeoutMs: UInt, promise: Promise<T>) -> Promise<T?> {
    return Promise { seal in
        withTimeout(timeoutMs: timeoutMs, promise: promise)
        .done { result in
            seal.fulfill(result)
        }
        .catch { error in
            if error is PromiseTimeoutError {
                seal.fulfill(nil)
            }else {
                seal.reject(error)
            }
        }
    }
}
