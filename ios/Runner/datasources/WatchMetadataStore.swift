//
//  WatchMetadataStore.swift
//  Runner
//
//  Created by crc32 on 27/02/2022.
//

import Foundation
import libpebblecommon
//TODO: plumb in watch version req
class WatchMetadataStore {
    static let shared = WatchMetadataStore()
    var lastConnectedWatchMetadata: WatchVersion.WatchVersionResponse? = nil
    var lastConnectedWatchModel: Int? = nil
}
