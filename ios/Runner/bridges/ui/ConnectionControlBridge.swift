//
//  ConnectionControlBridge.swift
//  Runner
//
//  Created by crc32 on 18/10/2021.
//

import Foundation
class ConnectionControlBridge: UiConnectionControl {
    private let binaryMessenger: FlutterBinaryMessenger
    private lazy var pairCallbacks = PairCallbacks(binaryMessenger: binaryMessenger)
    
    init(callbackMessenger: FlutterBinaryMessenger) {
        self.binaryMessenger = callbackMessenger
    }
    
    func connect(toWatch input: NumberWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        let watchHash = input.value!.intValue
        LECentral.shared.connectToWatchHash(watchHash: watchHash) { connState in
            if connState {
                self.pairCallbacks.onWatchPairComplete(input) {_ in }
            }
        }
    }
    
    func unpairWatch(_ input: NumberWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        error.pointee = FlutterError(code: "UNSUPPORTED", message: "iOS does not support unpairing devices", details: nil)
    }
}
