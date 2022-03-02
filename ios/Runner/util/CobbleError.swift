//
//  CobbleError.swift
//  Runner
//
//  Created by crc32 on 02/03/2022.
//

import Foundation
protocol CobbleError : Error {
    var message: String { get }
}
