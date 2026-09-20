package com.iptvapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents a live stream entry returned by the Xtream Codes API.
 * Endpoint: player_api.php?action=get_live_streams
 */
data class LiveStreamDto(
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

    @SerializedName("epg_channel_id")
    val epgChannelId: String?,

    @SerializedName("added")
    val added: String?,

    @SerializedName("category_id")
    val categoryId: String?,

    @SerializedName("custom_sid")
    val customSid: String?,

    @SerializedName("tv_archive")
    val tvArchive: Int = 0,

    @SerializedName("direct_source")
    val directSource: String?,

    @SerializedName("tv_archive_duration")
    val tvArchiveDuration: Int = 0
)
