//
//  FlutterBackgroundController.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
import PromiseKit
import CocoaLumberjackSwift
import shared_preferences_ios
class FlutterBackgroundController: NSObject, BackgroundControl {
    static var shared: FlutterBackgroundController!
    private var engine: FlutterEngine?
    
    private let flutterSideReady = DispatchSemaphore(value: 0)
    private let iOSSideReady = DispatchSemaphore(value: 0)
    
    private let queue = DispatchQueue(label: Bundle.main.bundleIdentifier!+".FlutterBackgroundControllerQueue", qos: .utility)
    
    override init() {
        super.init()
        initEngine().done { initedEngine in
            self.engine = initedEngine
        }.cauterize()
    }
    
    func notifyFlutterBackgroundStarted(completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        DDLogDebug("Flutter bg started")
        flutterSideReady.signal()
        queue.async { [self] in
            iOSSideReady.wait()
            let nwrapper = NumberWrapper.make(withValue: 0)
            completion(nwrapper, nil)
        }
    }
    
    func getBackgroundFlutterEngine() -> Promise<FlutterEngine?> {
        return Promise { seal in
            if let engine = engine {
                seal.fulfill(engine)
            }else {
                initEngine().done { initedEngine in
                    self.engine = initedEngine
                    seal.fulfill(self.engine)
                }.catch { error in
                    DDLogError("Error initializing background engine: \(error.localizedDescription)")
                    seal.reject(error)
                }
            }
        }
    }
    
    private func initEngine() -> Promise<FlutterEngine?> {
        return Promise { seal in
            DispatchQueue.main.async { [self] in
                let flutterEngine = FlutterEngine(name: "CobbleBG", project: nil, allowHeadlessExecution: true)

                let persistentState = UserDefaults.standard
                let backgroundEndpointMethodHandle = (persistentState.value(forKey: "FlutterBackgroundHandle") as? Int64) ?? 0
                if let callbackInfo = FlutterCallbackCache.lookupCallbackInformation(backgroundEndpointMethodHandle) {
                    flutterEngine.run(withEntrypoint: callbackInfo.callbackName, libraryURI: callbackInfo.callbackLibraryPath)
                } else {
                    flutterEngine.run()
                }

                GeneratedPluginRegistrant.register(with: flutterEngine)

                createFlutterBridges(flutterEngine: flutterEngine)
                    .done {
                        seal.fulfill(flutterEngine)
                    }
                    .catch { error in
                        seal.reject(error)
                    }
            }
        }
    }
    
    private func createFlutterBridges(flutterEngine: FlutterEngine) -> Promise<()> {
        return queue.async(.promise) {
            BackgroundControlSetup(flutterEngine.binaryMessenger, self)
            self.flutterSideReady.wait()
            FlutterBridgeSetup.createCommonBridges(binaryMessenger: flutterEngine.binaryMessenger)
            FlutterBridgeSetup.createBackgroundBridges(binaryMessenger: flutterEngine.binaryMessenger)
            self.iOSSideReady.signal()
        }
    }
}
