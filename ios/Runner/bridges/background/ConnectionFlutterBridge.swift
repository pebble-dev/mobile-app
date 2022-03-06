//
//  ConnectionFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import CocoaLumberjackSwift
class ConnectionFlutterBridge: NSObject, ConnectionControl {
    func isConnectedWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let ret = BooleanWrapper()
        ret.value = NSNumber(value: LECentral.shared.isConnected())
        return ret
    }
    
    func disconnectWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        LECentral.shared.disconnect()
    }
    
    func sendRawPacketList(ofBytes input: ListWrapper, error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        if let rawPacket = input.value as? [UInt8] {
            do {
                try LEPeripheral.shared.writePacket(rawProtocolPacket: rawPacket)
            } catch is PPoGATTServiceNotInitialized {
                DDLogWarn("ConnectionFlutterBridge: Tried to send packet via uninitialized transport, ignoring")
                assertionFailure()
            } catch {
                DDLogWarn("ConnectionFlutterBridge: Unexpected error sending raw packet")
                assertionFailure()
            }
        }
        
    }
    
    func observeConnectionChangesWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        
    }
    
    func cancelObservingConnectionChangesWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        
    }
}