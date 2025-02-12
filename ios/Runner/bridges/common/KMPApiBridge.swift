//
//  KMPApiBridge.swift
//  Runner
//
//  Created by Timofei Plotnikov on 08.02.2025.
//

import Foundation
import Flutter
import shared

class KMPApiBridge: NSObject, KMPApi {
    func updateTokenToken(
        _ token: StringWrapper,
        error: AutoreleasingUnsafeMutablePointer<FlutterError?>
    ) {
       //TODO: Implement
    }

    func openLockerViewWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) {
        guard
            let window = UIApplication.shared.windows.first,
            let root = window.rootViewController
        else {
            print("Failed to get rootViewController")
            return
        }
       
        // Notes:
        // 1. This is not the best places to open up mainViewController. Its here just for
        //    demonstration purposes
        // 2. The screen is presented as a modal, use swipe from top to bottom to return back to
        //    the flutter app
        let mainViewController = Main_iosKt.mainViewController()
        root.present(mainViewController, animated: false, completion: nil)
    }
}
