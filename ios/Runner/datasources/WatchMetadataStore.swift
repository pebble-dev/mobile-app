//
//  WatchMetadataStore.swift
//  Runner
//
//  Created by crc32 on 27/02/2022.
//

import Foundation
import libpebblecommon

class WatchMetadataStore {
    static var shared: WatchMetadataStore!
    var lastConnectedWatchMetadata: WatchVersion.WatchVersionResponse? = nil
    var lastConnectedWatchModel: Int? = nil
}
