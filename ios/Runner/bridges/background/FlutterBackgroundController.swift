//
//  FlutterBackgroundController.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
class FlutterBackgroundController: NSObject, BackgroundControl {
    private var engine: FlutterEngine?
    
    private let flutterSideReady = DispatchSemaphore(value: 0)
    private let iOSSideReady = DispatchSemaphore(value: 0)
    
    func notifyFlutterBackgroundStarted(completion: @escaping (NumberWrapper?, FlutterError?) -> Void) {
        DispatchQueue.main.async { [self] in
            flutterSideReady.signal()
            iOSSideReady.wait()
            let nwrapper = NumberWrapper()
            nwrapper.value = 0
            completion(nwrapper, nil)
        }
    }
    
    func getBackgroundFlutterEngine(result: @escaping (FlutterEngine?) -> ()) {
        if engine != nil {
            result(engine)
        }
        
        initEngine() { [self] in
            engine = $0
            result(engine)
        }
    }
    
    private func initEngine(result: @escaping (FlutterEngine?) -> ()) {
        DispatchQueue.main.async { [self] in
            let persistentState = UserDefaults.standard
            let backgroundEndpointMethodHandle = persistentState.value(forKey: "FlutterBackgroundHandle") as! Int64
            let callbackInfo = FlutterCallbackCache.lookupCallbackInformation(backgroundEndpointMethodHandle)!
            
            let flutterEngine = FlutterEngine(name: "CobbleBG", project: nil, allowHeadlessExecution: true)
            createFlutterBridges(flutterEngine: flutterEngine, callbackToStart: callbackInfo) { [self] in
                result(engine)
            }
        }
    }
    
    private func createFlutterBridges(flutterEngine: FlutterEngine, callbackToStart: FlutterCallbackInformation?, complete: () -> ()) {
        DispatchQueue.main.async { [self] in
            BackgroundControlSetup(flutterEngine.binaryMessenger, self)
            if callbackToStart != nil {
                flutterEngine.run(withEntrypoint: callbackToStart!.callbackName, libraryURI: callbackToStart!.callbackLibraryPath)
                GeneratedPluginRegistrant.register(with: flutterEngine)
            }
            flutterSideReady.wait()
            FlutterBridgeSetup.createCommonBridges(binaryMessenger: flutterEngine.binaryMessenger)
            FlutterBridgeSetup.createBackgroundBridges(binaryMessenger: flutterEngine.binaryMessenger)
            iOSSideReady.signal()
        }
    }
}
