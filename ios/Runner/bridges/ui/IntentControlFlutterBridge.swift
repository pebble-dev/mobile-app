//
//  IntentControlFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 04/03/2022.
//

import Foundation
class IntentControlFlutterBridge: NSObject, IntentControl {
    
    private let binaryMessenger: FlutterBinaryMessenger
    private lazy var intentCallbacks = IntentCallbacks(binaryMessenger: binaryMessenger)
    private var flutterReadyForIntents = false
    private var pendingUrl: URL?
    
    init(callbackMessenger: FlutterBinaryMessenger) {
        self.binaryMessenger = callbackMessenger
        super.init()
        OpenWith.shared.setupOpenCallback(callback: self.onOpen)
    }
    
    private func onOpen(url: URL) {
        if (!flutterReadyForIntents) {
            pendingUrl = url
        }else {
            intentCallbacks.openUriUri(StringWrapper.make(withValue: url.absoluteString)) { _ in }
        }
    }
    
    func notifyFlutterReadyForIntentsWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        flutterReadyForIntents = true
        if let pendingUrl = pendingUrl {
            onOpen(url: pendingUrl)
        }
    }
    
    func notifyFlutterNotReadyForIntentsWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        flutterReadyForIntents = false
    }
    
    func waitForBoot(completion: @escaping (BooleanWrapper?, FlutterError?) -> Void) {
        //TODO: wait for boot
    }
    
}
