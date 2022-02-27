import UIKit
import Flutter
import CocoaLumberjackSwift
import CocoaLumberjackSwiftLogBackend
import Logging

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    private var flutterBG: FlutterBackgroundController?
    override func application(
      _ application: UIApplication,
      didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        setupLogging()
        GeneratedPluginRegistrant.register(with: self)
        ProtocolComms.shared = ProtocolComms()
        LEPeripheral.shared = LEPeripheral()
        
        setupFlutter()
        flutterBG = FlutterBackgroundController()
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    private func setupFlutter() {
        let binaryMessenger = (window?.rootViewController as! FlutterViewController).binaryMessenger
        FlutterBridgeSetup.createUIBridges(binaryMessenger: binaryMessenger)
        FlutterBridgeSetup.createCommonBridges(binaryMessenger: binaryMessenger)
    }
    
    private func setupLogging() {
        DDLog.add(DDOSLogger.sharedInstance)
    }
}
