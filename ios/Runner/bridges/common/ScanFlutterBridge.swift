//
//  ScanFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import CobbleLE

class ScanFlutterBridge: NSObject, ScanControl {
    private let binaryMessenger: FlutterBinaryMessenger
    
    private let queue = DispatchQueue(label: "io.rebble.cobble.Bridges.common.ScanFlutterBridge", qos: .userInitiated)
    private lazy var scanCallbacks = ScanCallbacks(binaryMessenger: binaryMessenger)
    
    init(callbackMessenger: FlutterBinaryMessenger) {
        self.binaryMessenger = callbackMessenger
    }
    
    func startBleScanWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        queue.async { [self] in
            scanCallbacks.onScanStarted {_ in }
            LECentral.shared.waitForReady {
                let _ = LECentral.shared.scan(foundDevices: { foundDevices in
                    let list = ListWrapper()
                    list.value = foundDevices.map {
                        $0.toPigeon().toMap()
                    }
                    scanCallbacks.onScanUpdatePebbles(list) {_ in }
                }, scanEnded: {
                    scanCallbacks.onScanStopped() {_ in }
                })
            }
        }
    }
    
    func startClassicScanWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        error.pointee = FlutterError(code: "UNSUPPORTED", message: "iOS does not support classic bluetooth", details: nil)
    }
}
