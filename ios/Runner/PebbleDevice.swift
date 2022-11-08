//
//  PebbleDevice.swift
//  Runner
//
//  Created by crc32 on 26/10/2021.
//

import Foundation
struct StoredPebbleDevice: Codable {
    let name: String
    let identifier: UUID
    let serialNumber: String?
    let color: UInt8?
}
