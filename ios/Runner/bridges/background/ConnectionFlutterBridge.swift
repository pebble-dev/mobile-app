//
//  ConnectionFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
class ConnectionFlutterBridge: ConnectionControl {
    func isConnected(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let ret = BooleanWrapper()
        ret.value = NSNumber(value: LECentral.shared.isConnected())
        return ret
    }
    
    func disconnect(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        LECentral.shared.disconnect()
    }
    
    func sendRawPacket(_ input: ListWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        if let rawPacket = input.value as? [UInt8] {
            do {
                try LEPeripheral.shared.writePacket(rawProtocolPacket: rawPacket)
            } catch is PPoGATTServiceNotInitialized {
                print("ConnectionFlutterBridge: Tried to send packet via uninitialized transport, ignoring")
                assertionFailure()
            } catch {
                print("ConnectionFlutterBridge: Unexpected error sending raw packet")
                assertionFailure()
            }
        }
        
    }
    
    func observeConnectionChanges(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        
    }
    
    func cancelObservingConnectionChanges(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        
    }
}
