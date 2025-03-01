package io.rebble.cobble.shared.domain.store

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JsFrame<T>(
    val methodName: String,
    val callbackId: Int,
    val data: T,
)

@Serializable
data class LoadAppToDeviceAndLocker(
    val id: String,
    val uuid: String,
    val title: String,
    @SerialName("list_image")
    val listImage: String,
    @SerialName("icon_image")
    val iconImage: String,
    @SerialName("screenshot_image")
    val screenshotImage: String,
    val type: String,
    @SerialName("pbw_file")
    val pbwFile: String,
    val links: AppLinks,
)

@Serializable
data class AppLinks(
    val add: String,
    val remove: String,
    val share: String,
    @SerialName("add_flag")
    val addFlag: String? = null,
    @SerialName("add_heart")
    val addHeart: String? = null,
    @SerialName("remove_flag")
    val removeFlag: String? = null,
    @SerialName("remove_heart")
    val removeHeart: String? = null,
)

@Serializable
data class SetNavBarTitle(
    val title: String,
    val browserTitle: String? = null,
    @SerialName("show_search")
    val showSearch: Boolean = true,
    @SerialName("show_share")
    val showShare: Boolean? = null,
)

@Serializable
data class OpenURL(
    val url: String,
)
