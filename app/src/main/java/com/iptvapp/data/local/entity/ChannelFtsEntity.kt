package com.iptvapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

/**
 * FTS4 virtual table that mirrors [ChannelEntity] for full-text search.
 *
 * Room generates a shadow table that indexes [name], [categoryName],
 * and [epgChannelId] — enabling sub-millisecond MATCH queries even
 * on tens of thousands of channels.
 *
 * **Important:** This table is NOT populated automatically. After every
 * insert/update/delete on the [ChannelEntity] table, the FTS index
 * must be rebuilt (see [ChannelDao.rebuildFtsIndex]).
 */
@Fts4(contentEntity = ChannelEntity::class)
@Entity(tableName = "channels_fts")
data class ChannelFtsEntity(
    /** Channel display name — primary search target. */
    @ColumnInfo(name = "name")
    val name: String,

    /** Category / group name — secondary search target. */
    @ColumnInfo(name = "category_name")
    val categoryName: String?,

    /** EPG channel identifier — searchable for advanced users. */
    @ColumnInfo(name = "epg_channel_id")
    val epgChannelId: String?
)
