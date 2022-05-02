//
//  BackgroundSetupFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import PromiseKit

typealias CallbackHandle = Int64

class BackgroundSetupFlutterBridge: NSObject, BackgroundSetupControl {

    private var resolver: Resolver<CallbackHandle>?

    func waitForBackgroundHandle() -> Promise<CallbackHandle> {
        Promise { self.resolver = $0 }
    }

    func setupBackgroundCallbackHandle(_ callbackHandle: NumberWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        if let value = callbackHandle.value as? CallbackHandle {
            resolver?.fulfill(value)
        } else {
            error.pointee = .init(code: "INVALID_HANDLE", message: "setupBackgroundCallbackHandle called without a callback handle", details: nil)
        }
    }
}
