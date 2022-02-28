//
//  ConnectionControlBridge.swift
//  Runner
//
//  Created by crc32 on 18/10/2021.
//

import Foundation
class ConnectionControlBridge: NSObject, UiConnectionControl {
    func connect(toWatchMacAddress macAddress: StringWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        LECentral.shared.connectToWatchHash(watchIdentifier: UUID(uuidString: macAddress.value!)!) { connState in
            if connState {
                self.pairCallbacks.onWatchPairCompleteAddress(macAddress) {_ in }
            }
        }
    }
    
    func unpairWatchMacAddress(_ macAddress: StringWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        error.pointee = FlutterError(code: "UNSUPPORTED", message: "iOS does not support unpairing devices", details: nil)
    }
    
    private let binaryMessenger: FlutterBinaryMessenger
    private lazy var pairCallbacks = PairCallbacks(binaryMessenger: binaryMessenger)
    
    init(callbackMessenger: FlutterBinaryMessenger) {
        self.binaryMessenger = callbackMessenger
    }
}
