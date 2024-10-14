//
//  OAuthEvent.swift
//  Runner
//
//  Created by crc32 on 16/06/2022.
//

import Foundation
import PromiseKit

class OAuthEvent {
    let code: String?
    let state: String?
    let error: String?
    init(code: String? = nil, state: String? = nil, error: String? = nil) {
        assert((code != nil && state != nil) || error != nil);
        self.code = code
        self.state = state
        self.error = error
    }
    
    static func post(code: String?, state: String?, error: String?) {
        if let error = error {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "OAuthEvent"), object: OAuthEvent(error: error))
        }else if let code = code, let state = state {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "OAuthEvent"), object: OAuthEvent(code: code, state: state))
        }else {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: "OAuthEvent"), object: OAuthEvent(error: "_invalid_callback_params"))
        }
    }
    
    static func next() -> Promise<OAuthEvent> {
        return Promise {seal in
            var token: NSObjectProtocol?
            token = NotificationCenter.default.addObserver(forName: NSNotification.Name(rawValue: "OAuthEvent"), object: nil, queue: nil) { notif in
                seal.fulfill(notif.object! as! OAuthEvent)
                NotificationCenter.default.removeObserver(token!)
            }
        }
    }
}
