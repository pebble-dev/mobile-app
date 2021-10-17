//
//  LEMeta.swift
//  Runner
//
//  Created by crc32 on 17/10/2021.
//

import Foundation
class LEMeta {
    let vendor: UInt16
    let payloadType: UInt8
    let serialNumber: String
    
    let hardwarePlatform: UInt8?
    let color: UInt8?
    let major: UInt8?
    let minor: UInt8?
    let patch: UInt8?
    
    private let flags: UInt8?
    let runningPRF: Bool?
    let firstUse: Bool?
    
    private let mandatoryDataSize = (UInt16.bitWidth/8)+(UInt8.bitWidth/8)+((UInt8.bitWidth/8)*12)
    private let fullDataSize = 32
    
    init (rawData: [UInt8]) {
        //TODO
    }
}
