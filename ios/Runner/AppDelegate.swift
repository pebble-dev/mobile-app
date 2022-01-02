import UIKit
import Flutter

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    private var flutterBG: FlutterBackgroundController?
    override func application(
      _ application: UIApplication,
      didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        GeneratedPluginRegistrant.register(with: self)
        ProtocolService.shared = ProtocolService()
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
}
