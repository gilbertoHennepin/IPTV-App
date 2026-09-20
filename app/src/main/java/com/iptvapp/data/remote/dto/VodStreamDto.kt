package com.iptvapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents a VOD (Video On Demand) entry returned by the Xtream Codes API.
 * Endpoint: player_api.php?action=get_vod_streams
 */
data class VodStreamDto(
    @SerializedName("num")
    val num: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("stream_type")
    val streamType: String?,

    @SerializedName("stream_id")
    val streamId: Int,

    @SerializedName("stream_icon")
    val streamIcon: String?,

    @SerializedName("rating")
    val rating: String?,

    @SerializedName("rating_5based")
    val rating5Based: Double = 0.0,

    @SerializedName("added")
    val added: String?,

    @SerializedName("category_id")
    val categoryId: String?,

    @SerializedName("container_extension")
    val containerExtension: String?,

    @SerializedName("custom_sid")
    val customSid: String?,

    @SerializedName("direct_source")
    val directSource: String?
)
