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
    private var backgroundEngineGetters = [Resolver<FlutterEngine>]()
    
    private let flutterSideReady = DispatchSemaphore(value: 0)
    private let iOSSideReady = DispatchSemaphore(value: 0)
    
    private let queue = DispatchQueue(label: Bundle.main.bundleIdentifier!+".FlutterBackgroundControllerQueue", qos: .utility)
    
    func notifyFlutterBackgroundStarted(completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        DDLogDebug("Flutter background started")
        flutterSideReady.signal()
        queue.async { [self] in
            iOSSideReady.wait()
            let nwrapper = NumberWrapper.make(withValue: 0)
            completion(nwrapper, nil)
        }
    }
    
    func getBackgroundFlutterEngine() -> Promise<FlutterEngine> {
        if let engine = engine {
            return Promise { $0.fulfill(engine) }
        } else {
            return Promise { backgroundEngineGetters.append($0) }
        }
    }

    func setupEngine(_ handle: Int64) {
        initEngine(callbackHandle: handle)
            .done { initedEngine in
                DDLogDebug("Done initializing background engine")
                self.engine = initedEngine
                self.backgroundEngineGetters.forEach({ $0.fulfill(initedEngine) })
            }.catch { error in
                DDLogError("Error initializing background engine: \(error.localizedDescription)")
                self.backgroundEngineGetters.forEach({ $0.reject(error) })
            }
    }
    
    private func initEngine(callbackHandle: Int64) -> Promise<FlutterEngine> {
        return Promise { seal in
            DispatchQueue.main.async { [self] in
                let flutterEngine = FlutterEngine(name: "CobbleBG", project: nil, allowHeadlessExecution: true)

                if let callbackInfo = FlutterCallbackCache.lookupCallbackInformation(callbackHandle) {
                    flutterEngine.run(withEntrypoint: callbackInfo.callbackName, libraryURI: callbackInfo.callbackLibraryPath)
                } else {
                    assertionFailure("Failed to initialize Flutter engine")
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
