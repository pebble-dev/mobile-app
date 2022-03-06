//
//  AppInstallUtils.swift
//  Runner
//
//  Created by crc32 on 27/02/2022.
//

import Foundation
import libpebblecommon
import SwiftZip

enum PbwSpecError: LocalizedError {
    
    case manifestMissing(platform: WatchType, app: String)
    case blobMissing(blob: String, app: String)
    case appInfoMissing(app: String)
    
    var localizedDescription: String? {
        switch self {
        case .manifestMissing(let platform, let app):
            return "Manifest \(platform) missing from app \(app)"
            
        case .blobMissing(let blob, let app):
            return "Blob \(blob) missing from app \(app)"
            
        case .appInfoMissing(let app):
            return "AppInfo missing from app \(app)"
        }
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
        return try SerializationUtil.shared.deserializeManifest(jsonString: manifestString)
    } catch {
        return nil
    }
}

func requirePbwManifest(pbwFile: URL, watchType: WatchType) throws -> PbwManifest {
    if let manifest = getPbwManifest(pbwFile: pbwFile, watchType: watchType) {
        return manifest
    }else {
        throw PbwSpecError.manifestMissing(platform: watchType, app: pbwFile.lastPathComponent)
    }
}

func requirePbwBinaryBlob(pbwFile: URL, watchType: WatchType, blobName: String) throws -> Data {
    let path = platformPath(watchType: watchType, fileName: blobName)
    let zip = try ZipArchive(url: pbwFile)
    do {
        let entry = try zip.locate(filename: path)
        return try entry.data()
    } catch {
        zip.discard()
        throw PbwSpecError.blobMissing(blob: blobName, app: pbwFile.lastPathComponent)
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
        return try SerializationUtil.shared.deserializeAppInfo(jsonString: appInfoString)
    } catch {
        throw PbwSpecError.appInfoMissing(app: pbwFile.lastPathComponent)
    }
}

private func platformPath(watchType: WatchType, fileName: String) -> String {
    if (watchType == WatchType.aplite) {
        return "aplite/\(fileName)"
    }else {
        return "\(watchType.codename)/\(fileName)"
    }
}
