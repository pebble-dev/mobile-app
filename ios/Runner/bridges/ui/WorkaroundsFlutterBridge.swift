//
//  WorkaroundsFlutterBridge.swift
//  Runner
//
//  Created by crc32 on 05/03/2022.
//

import Foundation
class WorkaroundsFlutterBridge: NSObject, WorkaroundsControl {
    func getNeededWorkaroundsWithError(_ error: AutoreleasingUnsafeMutablePointer<FlutterError?>) -> ListWrapper? {
        return ListWrapper.make(withValue: [])
    }

}
