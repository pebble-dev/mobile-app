//
//  cobbleTests.swift
//  cobbleTests
//
//  Created by crc32 on 24/02/2022.
//

import XCTest
import libpebblecommon
@testable import Runner

class TransportTests: XCTestCase {
    private let queue = DispatchQueue(label: "io.rebble.cobbleTests.TransportTests", qos: .utility)
    
    override func setUp() async throws {
        await LECentral.shared.waitForReadyAsync()
    }

    override func tearDownWithError() throws {
        LECentral.shared.disconnect()
    }

    func testConnectionFromScratch() async throws {
        let remote: BluePebbleDevice? = await withCheckedContinuation { continuation in
            var remote: BluePebbleDevice?
            let success = LECentral.shared.scan { foundDevices in
                remote = foundDevices.first
            } scanEnded: {
                continuation.resume(returning: remote)
            }
            XCTAssertTrue(success, "Scan did not succeed")
        }
        XCTAssertNotNil(remote, "No remote device found in scan")
        
        ProtocolService.shared.systemHandler.waitNegotiationComplete {
            print("Test: Sending ping")
            ProtocolService.shared.sendPacket(packet: PingPong.Ping(cookie: 0x1337)) {success, e in
                XCTAssertTrue(success, "Failed to send ping")
            }
        }
        
        var cont = false
        let connected: Bool = await withCheckedContinuation { continuation in
            LECentral.shared.connectToWatchHash(watchHash: remote!.peripheral.identifier.uuidString.hashValue) { connState in
                if (!cont) {
                    cont = true
                    continuation.resume(returning: connState)
                }
            }
        }
        XCTAssertTrue(connected, "Failed to connect to remote device")
    }
    
    func testConnectionExistingBond() throws {
        let remote: BluePebbleDevice? = LECentral.shared.getAssociatedWatchFromhHash(watchHash: PersistentStorage.shared.devices[0].identifier.hashValue)
        XCTAssertNotNil(remote, "Saved remote device not found")
        
        ProtocolService.shared.systemHandler.waitNegotiationComplete {
            print("Test: Sending ping")
            ProtocolService.shared.sendPacket(packet: PingPong.Ping(cookie: 0x1337)) {success, e in
                XCTAssertTrue(success, "Failed to send ping")
            }
        }
        
        Task.init(priority: .userInitiated) {
            var cont = false
            let connected: Bool = await withCheckedContinuation { continuation in
                LECentral.shared.connectToWatchHash(watchHash: remote!.peripheral.identifier.uuidString.hashValue) { connState in
                    if (!cont) {
                        cont = true
                        continuation.resume(returning: connState)
                    }
                }
            }
            XCTAssertTrue(connected, "Failed to connect to remote device")
        }
    }
}
