import UIKit
import Flutter
import CocoaLumberjackSwift
import CocoaLumberjackSwiftLogBackend
import Logging
import shared

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    override func application(
      _ application: UIApplication,
      didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        setupLogging()
        GeneratedPluginRegistrant.register(with: self)
        WatchMetadataStore.shared = WatchMetadataStore()
        ProtocolComms.shared = ProtocolComms()
        LEPeripheral.shared = LEPeripheral()
        LECentral.shared = LECentral()
        
        setupFlutter()
        setupKmp()
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    private func setupFlutter() {
        let binaryMessenger = (window?.rootViewController as! FlutterViewController).binaryMessenger
        if #available(iOS 10.0, *) {
          UNUserNotificationCenter.current().delegate = self as UNUserNotificationCenterDelegate
        }
        FlutterBridgeSetup.createUIBridges(binaryMessenger: binaryMessenger)
        FlutterBridgeSetup.createCommonBridges(binaryMessenger: binaryMessenger)
    }
    
    private func setupKmp() {
        Koin_iosKt.doInitKoin()
    }
    
    private func setupLogging() {
        DDLog.add(DDOSLogger.sharedInstance)
    }
    
    override func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        if url.scheme == "rebble" && url.host == "auth_complete" {
            let urlc = URLComponents(string: url.absoluteString)
            OAuthEvent.post(
                code: urlc?.queryItems?.first(where: { item in item.name == "code" })?.value,
                state: urlc?.queryItems?.first(where: { item in item.name == "state" })?.value,
                error: urlc?.queryItems?.first(where: { item in item.name == "error" })?.value
            )
        }else {
            OpenWith.shared.openUrl(url: url)
        }
        return true
    }
}
