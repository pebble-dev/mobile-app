//
//  extensions.swift
//  Runner
//
//  Created by crc32 on 27/02/2022.
//

import Foundation
import libpebblecommon
import PromiseKit

private func kotlinSuspendResolver<T>(seal: Resolver<T>) -> ((T?, Error?) -> Void) {
    return { res, error in
        seal.resolve(res, error)
    }
}

//MARK: - Coroutines extensions

extension Kotlinx_coroutines_coreChannel {
    func receivePromise() -> Promise<Any> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.receive(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
}

extension Kotlinx_coroutines_coreChannelIterator {
    func hasNextPromise() -> Promise<KotlinBoolean> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.hasNext(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
}

//MARK: - Protocol service extensions

extension PutBytesService {
    func sendPromise(packet: PutBytesOutgoingPacket) -> Promise<KotlinUnit> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.send(packet: packet, completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
    
    func sendAppPartPromise(appId: UInt32, blob: Data,
                            watchType: WatchType, watchVersion: WatchVersion.WatchVersionResponse,
                            manifestEntry: PbwBlob, type: ObjectType) -> Promise<Void> {
        return Promise {seal in
            DispatchQueue.main.async {
                self.sendAppPart(appId: appId,
                                 blob: KUtil.shared.byteArrayFromNative(arr: blob),
                                 watchType: watchType,
                                 watchVersion: watchVersion,
                                 manifestEntry: manifestEntry,
                                 type: type,
                                 completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.asVoid()
    }
}

extension SystemService {
    func requestWatchModelPromise() -> Promise<Int> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.requestWatchModel(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { $0.intValue }
    }
    
    func requestWatchVersionPromise() -> Promise<WatchVersion.WatchVersionResponse> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.requestWatchVersion(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
    
    func sendPromise(packet: SystemPacket, priority: PacketPriority) -> Promise<Void> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.send(packet: packet, priority: priority, completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { _ in}
    }
}

extension BlobDBService {
    func sendPromise(packet: BlobCommand, priority: PacketPriority) -> Promise<BlobResponse> {
        return Promise<BlobResponse> { seal in
            DispatchQueue.main.async {
                self.send(packet: packet, priority: priority, completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
}

extension AppReorderService {
    func sendPromise(packet: AppReorderOutgoingPacket) -> Promise<AppReorderResult> {
        return Promise<KotlinUnit> { seal in
            DispatchQueue.main.async {
                self.send(packet: packet, completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.then {_ in
            return self.receivedMessages.receivePromise()
        }.map {res in res as! AppReorderResult}
    }
}

extension AppRunStateService {
    func sendPromise(packet: AppRunStateMessage) -> Promise<Void> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.send(packet: packet, completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map {_ in}
    }
}

//MARK: - Pigeon serializers

extension libpebblecommon.PbwAppInfo {
    func toPigeon() -> Pigeon_PbwAppInfo {
        return Pigeon_PbwAppInfo.make(withIsValid: true,
                                      uuid: uuid,
                                      shortName: shortName,
                                      longName: longName,
                                      companyName: companyName,
                                      versionCode: NSNumber(value: versionCode),
                                      versionLabel: versionLabel,
                                      appKeys: appKeys,
                                      capabilities: capabilities,
                                      resources: resources.media.map { $0.toPigeon() },
                                      sdkVersion: sdkVersion,
                                      targetPlatforms: targetPlatforms,
                                      watchapp: watchapp.toPigeon())
    }
}

extension libpebblecommon.Media {
    func toPigeon() -> WatchResource {
        return WatchResource.make(withFile: resourceFile,
                                  menuIcon: NSNumber(value: menuIcon.value),
                                  name: name,
                                  type: type)
    }
}

extension libpebblecommon.Watchapp {
    func toPigeon() -> WatchappInfo {
        return WatchappInfo.make(withWatchface: NSNumber(value: watchface),
                                 hiddenApp: NSNumber(value: hiddenApp),
                                 onlyShownOnCommunication: NSNumber(value: onlyShownOnCommunication))
    }
}

extension WatchVersion.WatchVersionResponse {
    func toPigeon(device: BluePebbleDevice?, model: Int?) -> PebbleDevicePigeon {
        return PebbleDevicePigeon.make(withName: device?.peripheral.name ?? "",
                                       address: device?.peripheral.identifier.uuidString ?? "",
                                       runningFirmware: running.toPigeon(),
                                       recoveryFirmware: recovery.toPigeon(),
                                       model: NSNumber(value: model ?? 0),
                                       bootloaderTimestamp: NSNumber(value: bootloaderTimestamp.get()?.uintValue ?? 0),
                                       board: board.get()! as String,
                                       serial: serial.get()! as String,
                                       language: language.get()! as String,
                                       languageVersion: NSNumber(value: languageVersion.get()?.uint16Value ?? 0),
                                       isUnfaithful: NSNumber(value: isUnfaithful.get()?.boolValue ?? false))
    }
}
private func blankWatchFirwmareVersion() -> PebbleFirmwarePigeon {
    return PebbleFirmwarePigeon.make(withTimestamp: 0,
                                     version: "",
                                     gitHash: "",
                                     isRecovery: NSNumber(value: false),
                                     hardwarePlatform: 0,
                                     metadataVersion: 0)
}

extension WatchFirmwareVersion {
    func toPigeon() -> PebbleFirmwarePigeon {
        return PebbleFirmwarePigeon.make(withTimestamp: NSNumber(value: timestamp.get()!.uintValue),
                                         version: versionTag.get()! as String,
                                         gitHash: gitHash.get()! as String,
                                         isRecovery: NSNumber(value: isRecovery.get()!.boolValue),
                                         hardwarePlatform: NSNumber(value: hardwarePlatform.get()!.uint8Value),
                                         metadataVersion: NSNumber(value: metadataVersion.get()!.uint8Value))
    }
}

//MARK: - Misc. extensions

extension ProtocolHandlerImpl {
    func openProtocolPromise() -> Promise<Void> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.openProtocol(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { _ in }
    }
    
    func closeProtocolPromise() -> Promise<Void> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.closeProtocol(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }.map { _ in }
    }
    
    func waitForNextPacketPromise() -> Promise<ProtocolHandlerPendingPacket> {
        return Promise { seal in
            DispatchQueue.main.async {
                self.waitForNextPacket(completionHandler: kotlinSuspendResolver(seal: seal))
            }
        }
    }
}

extension UuidUuid {
    public static func fromString(_ string: String) -> UuidUuid? {
        guard let uuid = UUID.init(uuidString: string) else {
            return nil
        }
        var bTup = uuid.uuid
        let size = MemoryLayout.size(ofValue: bTup)
        let bArr: [UInt8] = withUnsafePointer(to: &bTup.0) { point in
            return [UInt8](UnsafeBufferPointer(start: point, count: size))
        }
        return UuidUuid.init(uuidBytes: KUtil.shared.byteArrayFromNative(arr: Data(bArr)))
    }
}
