//
//  PutBytesController.swift
//  Runner
//
//  Created by crc32 on 27/02/2022.
//

import Foundation
import UIKit
import libpebblecommon
import CocoaLumberjackSwift
import SwiftZip
import PromiseKit

class PutBytesController {
    enum State {
        case Idle
        case Sending
    }
    enum PutBytesError: CobbleError {
        var message: String {
            switch self {
            case .putBytesBusy(let string):
                return string
            case .packetSendFail(let string):
                return string
            case .ioException(let string):
                return string
            case .timeout(let string):
                return string
            case .checksumException(let string):
                return string
            }
        }
        case putBytesBusy(String)
        case packetSendFail(String)
        case ioException(String)
        case timeout(String)
        case checksumException(String)
    }
    
    class Status {
        let state: State
        let progress: Double
        init(state: State, progress: Double = 0.0) {
            self.state = state
            self.progress = progress
        }
    }
    
    private var _status: Status = Status(state: .Idle)
    
    public var status: Status {
        get {
            return _status
        }
    }
    
    private let syncQueue = DispatchQueue(label: "PutBytesSyncQueue")
    private let dataQueue = DispatchQueue(label: "PutBytesDataQueue", qos: .userInitiated)
    
    private let putBytesService: PutBytesService
    
    private var lastCookie: UInt32? = nil
    
    init(putBytesService: PutBytesService) {
        self.putBytesService = putBytesService
    }
    
    func startAppInstall(appId: UInt, pbwFile: URL, watchType: WatchType) throws -> Promise<Void> {
        return launchNewPutBytesSession { seal in
            let manifest = try requirePbwManifest(pbwFile: pbwFile, watchType: watchType)
            let totalSize = manifest.application.size + (manifest.resources?.size ?? 0) + (manifest.worker?.size ?? 0)
            let progressMultiplier = 1 / Double(totalSize)
            
            firstly {
                self.sendAppPart(appID: appId,
                                 pbwFile: pbwFile,
                                 watchType: watchType,
                                 manifestEntry: manifest.application,
                                 type: ObjectType.appExecutable,
                                 progressMultiplier: progressMultiplier)
            }.then { () -> Promise<Void> in
                if let resources = manifest.resources {
                    return self.sendAppPart(appID: appId,
                                     pbwFile: pbwFile,
                                     watchType: watchType,
                                     manifestEntry: resources,
                                     type: ObjectType.appResource,
                                     progressMultiplier: progressMultiplier)
                }else {
                    return Promise<Void> {$0.fulfill(())}
                }
            }.then { () -> Promise<Void> in
                if let worker = manifest.worker {
                    return self.sendAppPart(appID: appId,
                                     pbwFile: pbwFile,
                                     watchType: watchType,
                                     manifestEntry: worker,
                                     type: ObjectType.worker,
                                     progressMultiplier: progressMultiplier)
                }else {
                    return Promise<Void> {$0.fulfill(())}
                }
            }.done {
                self._status = Status(state: .Idle)
                seal.fulfill(())
            }.catch { error in
                seal.reject(error)
            }
        }
    }
    
    private func sendAppPart(appID: UInt, pbwFile: URL, watchType: WatchType, manifestEntry: PbwBlob, type: ObjectType, progressMultiplier: Double) -> Promise<Void> {
        return Promise { seal in
            let reader = try requirePbwBinaryBlob(pbwFile: pbwFile, watchType: watchType, blobName: manifestEntry.name)
            
            firstly { () -> Promise<Void> in
                DDLogDebug("Send app part \(watchType) \(appID) \(manifestEntry) \(type) \(type.value) \(progressMultiplier)")
                return putBytesService.sendPromise(packet: PutBytesAppInit(objectSize: UInt32(manifestEntry.size), objectType: type, appId: UInt32(appID))).asVoid()
            }.then {
                self.awaitCookieAndPut(reader: reader, expectedCrc: manifestEntry.crc?.int64Value, progressMultiplier: progressMultiplier)
            }.then { cookie -> Promise<Void> in
                DDLogDebug("Sending install")
                return self.putBytesService.sendPromise(packet: PutBytesInstall(cookie: cookie)).asVoid()
            }.then {
                self.awaitAck().asVoid()
            }.done {
                DDLogDebug("Install completed")
                seal.fulfill(())
            }.catch { error in
                seal.reject(error)
            }.finally {
                reader.close()
            }
        }
    }
    
    private func awaitCookieAndPut(reader: ZipEntryReader, expectedCrc: Int64?, progressMultiplier: Double) -> Promise<UInt32> {
        return Promise { seal in
            firstly {
                awaitAck()
            }.then(on: dataQueue) { res -> Promise<Void> in
                let cookie = res.cookie.get()!.uint32Value
                self.lastCookie = cookie
                
                let maxDataSize = PacketSizeKt.getPutBytesMaximumDataSize(watchVersion: WatchMetadataStore.shared.lastConnectedWatchMetadata)
                var buf = Data(capacity: Int(maxDataSize))
                let crcCalculator = Crc32Calculator()
                var totalBytes = 0
                while (true) {
                    let readBytes = try buf.withUnsafeMutableBytes {
                        return try reader.read(buf: $0)
                    }
                    if (readBytes <= 0) {
                        break
                    }
                    
                    var payload = Data(capacity: readBytes)
                    payload.withUnsafeMutableBytes { pUnsafe in
                        buf.copyBytes(to: pUnsafe)
                        return
                    }
                    let payloadKt = KUtil.shared.byteArrayAsUByteArray(arr: KUtil.shared.byteArrayFromNative(arr: buf))
                    crcCalculator.addBytes(bytes: payloadKt)
                    
                    try firstly {
                        self.putBytesService.sendPromise(packet: PutBytesPut(cookie: cookie, payload: payloadKt)).asVoid()
                    }.then(on: self.dataQueue) {
                        self.awaitAck().asVoid()
                    }.done(on: self.dataQueue) {
                        let newProgress = self.status.progress + progressMultiplier * Double(readBytes)
                        DDLogDebug("Progress \(newProgress)")
                        self._status = Status(state: .Sending, progress: newProgress)
                        totalBytes += readBytes
                    }.wait()
                    
                }
                
                let calculatedCrc = crcCalculator.finalize()
                guard expectedCrc == nil || calculatedCrc == UInt32(expectedCrc!) else {
                    throw PutBytesError.checksumException("Sending fail: Crc mismatch (\(String(describing: calculatedCrc)) != \(String(describing: expectedCrc)))")
                }
                
                DDLogDebug("Sending commit")
                return self.putBytesService.sendPromise(packet: PutBytesCommit(cookie: cookie, objectCrc: calculatedCrc)).asVoid()
            }.then {_ in
                self.awaitAck().asVoid()
            }.done {
                seal.fulfill(self.lastCookie!)
            }.catch { error in
                seal.reject(error)
            }
        }
    }
    
    private func awaitAck() -> Promise<PutBytesResponse> {
        return Promise { seal in
            let timeout = after(seconds: 10).then( { Promise<PutBytesResponse> { seal in seal.reject(PutBytesError.timeout("Timeout awaiting ack")) } })
            race(getResponse(), timeout)
                .done { resp in
                    DDLogDebug("PutBytesResponse received")
                    let result = resp.result.get()
                    if (result?.uint8Value != PutBytesResult.ack.value) {
                        seal.reject(PutBytesError.ioException("Watch responded with NACK (\(String(describing: result?.uint8Value))). Aborting transfer"))
                    }else {
                        seal.fulfill(resp)
                    }
                }.catch { error in
                    seal.reject(error)
                }
        }
    }
    
    private func getResponse() -> Promise<PutBytesResponse> {
        return Promise { seal in
            let it = putBytesService.receivedMessages.iterator()
            firstly {
                it.hasNextPromise()
            }.done { hasNext in
                guard hasNext.boolValue else {
                    seal.reject(PutBytesError.ioException("Received messages channel is closed"))
                    return
                }
                
                self.putBytesService.receivedMessages.receive {resp, e in
                    guard let resp = resp else {
                        seal.reject(PutBytesError.ioException("Error receiving packet: \(String(describing: e?.localizedDescription))"))
                        return
                    }
                    //FIXME: check if this actually works
                    seal.fulfill(resp as! PutBytesResponse)
                }
            }.catch { error in
                seal.reject(PutBytesError.ioException("Error receiving packet: \(error)"))
            }
        }
    }
    
    private func launchNewPutBytesSession(block: @escaping (Resolver<()>) throws -> ()) -> Promise<Void> {
        return Promise { seal in
            do {
                try syncQueue.sync {
                    guard _status.state == .Idle else {
                        throw PutBytesError.putBytesBusy("PutBytes busy: state != Idle")
                    }
                    _status = Status(state: .Sending)
                }
            }catch {
                seal.reject(error)
            }
            
            dataQueue.async {
                firstly {
                    return Promise<()>(resolver: block)
                }.done{
                    seal.fulfill(())
                }.catch { error in
                    DDLogError("PutBytes error")
                    seal.reject(error)
                    
                    if let cookie = self.lastCookie {
                        self.putBytesService.sendPromise(packet: PutBytesAbort(cookie: cookie)).catch { error in
                            DDLogError("Error sending PutBytes abort: \(error.localizedDescription)")
                        }
                    }
                }.finally {
                    self.lastCookie = nil
                    self._status = Status(state: .Idle)
                }
            }
        }
    }
}
