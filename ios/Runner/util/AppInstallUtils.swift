//
//  AppInstallUtils.swift
//  Runner
//
//  Created by crc32 on 27/02/2022.
//

import Foundation
import libpebblecommon
import SwiftZip

class PbwSpecError: Error {
    private let description: String
    init(description: String) {
        self.description = description
    }
    
    public var localizedDescription: String {
        return description
    }
}

func getAppPbwFile(appUuid: String) throws -> URL {
    let appsDir = try FileManager.default.url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: false)
        .appendingPathComponent(Bundle.main.bundleIdentifier!, isDirectory: true)
        .appendingPathComponent("apps", isDirectory: true)
    try FileManager.default.createDirectory(at: appsDir, withIntermediateDirectories: true, attributes: nil)
    
    let targetFileName = appsDir.appendingPathComponent(appUuid+".pbw", isDirectory: false)
    return targetFileName
}

func getPbwManifest(pbwFile: URL, watchType: WatchType) -> PbwManifest? {
    do {
        let zip = try ZipArchive(url: pbwFile)
        defer {
            zip.discard()
        }
        let manifestEntry = try zip.locate(filename: platformPath(watchType: watchType, fileName: "manifest.json"))
        let manifestData = try manifestEntry.data()
        let manifestString = String(data: manifestData, encoding: .utf8)!
        return SerializationUtil.shared.deserializeManifest(jsonString: manifestString)
    } catch {
        return nil
    }
}

func requirePbwManifest(pbwFile: URL, watchType: WatchType) throws -> PbwManifest {
    if let manifest = getPbwManifest(pbwFile: pbwFile, watchType: watchType) {
        return manifest
    }else {
        throw PbwSpecError(description: "Manifest \(watchType) missing from app \(pbwFile.lastPathComponent)")
    }
}

func requirePbwBinaryBlob(pbwFile: URL, watchType: WatchType, blobName: String) throws -> ZipEntryReader {
    let path = platformPath(watchType: watchType, fileName: blobName)
    let zip = try ZipArchive(url: pbwFile)
    do {
        let entry = try zip.locate(filename: path)
        let entryReader = try entry.open()
        return entryReader
    } catch {
        zip.discard()
        throw PbwSpecError(description: "Blob \(blobName) missing from app \(pbwFile.lastPathComponent)")
    }
}

func requirePbwAppInfo(pbwFile: URL) throws -> libpebblecommon.PbwAppInfo {
    do {
        let zip = try ZipArchive(url: pbwFile)
        defer {
            zip.discard()
        }
        let appInfoEntry = try zip.locate(filename: "appinfo.json")
        let appInfoData = try appInfoEntry.data()
        let appInfoString = String(data: appInfoData, encoding: .utf8)!
        return SerializationUtil.shared.deserializeAppInfo(jsonString: appInfoString)
    } catch {
        throw PbwSpecError(description: "AppInfo missing from app \(pbwFile.lastPathComponent)")
    }
}

private func platformPath(watchType: WatchType, fileName: String) -> String {
    if (watchType == WatchType.aplite) {
        return "aplite/\(fileName)"
    }else {
        return "\(watchType.codename)/\(fileName)"
    }
}
