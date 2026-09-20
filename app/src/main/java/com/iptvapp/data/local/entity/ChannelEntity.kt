package com.iptvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Primary entity representing a single IPTV channel in the local database.
 *
 * Fields are modeled to align with the Xtream Codes live stream API response
 * so that network-to-database mapping is straightforward.
 */
@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Display name of the channel. */
    @ColumnInfo(name = "name")
    val name: String,

    /** Stream URL for playback. */
    @ColumnInfo(name = "url")
    val url: String,

    /** URL to the channel logo / icon. */
    @ColumnInfo(name = "logo_url")
    val logoUrl: String? = null,

    /** Category / group the channel belongs to. */
    @ColumnInfo(name = "category_name")
    val categoryName: String? = null,

    /** Xtream Codes category ID for filtering. */
    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,

    /** Xtream Codes stream ID for EPG lookups. */
    @ColumnInfo(name = "stream_id")
    val streamId: Int? = null,

    /** EPG channel ID for guide data matching. */
    @ColumnInfo(name = "epg_channel_id")
    val epgChannelId: String? = null,

    /** Stream type: "live", "movie", "radio", etc. */
    @ColumnInfo(name = "stream_type")
    val streamType: String? = null,

    /** Whether the channel supports catch-up / time-shift. */
    @ColumnInfo(name = "tv_archive")
    val tvArchive: Boolean = false,

    /** User has marked this channel as a favorite. */
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    /** Timestamp (epoch seconds) when the channel was added upstream. */
    @ColumnInfo(name = "added_timestamp")
    val addedTimestamp: Long? = null
)
