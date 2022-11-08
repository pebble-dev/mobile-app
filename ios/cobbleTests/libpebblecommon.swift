//
//  libpebblecommon.swift
//  cobbleTests
//
//  Created by crc32 on 06/03/2022.
//

import Foundation
import Runner
@testable import libpebblecommon
import XCTest


class TestLibpebblecommon: XCTestCase {
    func testStructMapperUUID() {
        let testUUID = UuidUuid.fromString(UUID().uuidString)!
        let testMapper = StructMapper()
        SUUID(mapper: testMapper, default: testUUID)
        print((testMapper.toBytes() as! KotlinByteArray).toNative())
    }
    
    func testBlobCommandDelete() {
        let testUUID = UuidUuid.fromString(UUID().uuidString)!
        let testMapper = StructMapper()
        SUUID(mapper: testMapper, default: testUUID)
        let bytes = testMapper.toBytes()
        let cmd = BlobCommand.DeleteCommand(
            token: UInt16.random(in: UInt16.min...UInt16.max),
            database: .app,
            key: bytes
        )
        print((cmd.serialize() as! KotlinByteArray).toNative())
    }
}
