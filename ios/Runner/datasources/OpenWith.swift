//
//  OpenWith.swift
//  Runner
//
//  Created by crc32 on 04/03/2022.
//

import Foundation
class OpenWith {
    static var shared = OpenWith()
    private var openCallback: ((URL) -> ())?
    
    func setupOpenCallback(callback: @escaping (URL) -> ()) {
        self.openCallback = callback
    }
    
    func openUrl(url: URL) {
        openCallback?(url)
    }
}
