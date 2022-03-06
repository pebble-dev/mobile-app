//
//  ConnectionFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 20/10/2021.
//

import Foundation
import CocoaLumberjackSwift
import libpebblecommon

class ConnectionFlutterBridge: NSObject, ConnectionControl {
    private let connectionCallbacks: ConnectionCallbacks
    
    private var connStateObserver: Any? = nil
    
    init(callbackMessenger: FlutterBinaryMessenger) {
        self.connectionCallbacks = ConnectionCallbacks(binaryMessenger: callbackMessenger)
    }
    
    func isConnectedWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> BooleanWrapper? {
        let ret = BooleanWrapper()
        ret.value = NSNumber(value: WatchConnectionState.current ~= .connected(watch: nil))
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
        //TODO
        connStateObserver = NotificationCenter.default.addObserver(
            forName: NSNotification.Name(rawValue: "WatchConnectionState.current"),
            object: nil,
            queue: nil
        ) {notif in
            let status = notif.object as! WatchConnectionState
            let bluetoothDevice = status.watchOrNil
            let model = WatchMetadataStore.shared.lastConnectedWatchModel
            let devicePigeon = WatchMetadataStore.shared.lastConnectedWatchMetadata?.toPigeon(device: bluetoothDevice, model: model)
                ?? WatchVersion.WatchVersionResponse().toPigeon(device: bluetoothDevice, model: model)
            
            self.connectionCallbacks.onWatchConnectionStateChangedNewState(
                WatchConnectionStatePigeon.make(
                    withIsConnected: NSNumber(value: status ~= .connected(watch: nil)),
                    isConnecting: NSNumber(value: status ~= .connecting(watch: nil) ||
                                           status ~= .waitingForReconnect(watch: nil) /*||
                                           status ~= .waitingForBluetoothToEnable(watch: nil)*/),
                    currentWatchAddress: bluetoothDevice?.peripheral.identifier.uuidString,
                    currentConnectedWatch: devicePigeon)
            ) {_ in}
        }
    }
    
    func cancelObservingConnectionChangesWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        if let connStateObserver = connStateObserver {
            NotificationCenter.default.removeObserver(connStateObserver)
            self.connStateObserver = nil
        }
    }
}
