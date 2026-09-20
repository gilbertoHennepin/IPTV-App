package com.iptvapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iptvapp.data.local.dao.CategoryDao
import com.iptvapp.data.local.dao.ChannelDao
import com.iptvapp.data.local.entity.CategoryEntity
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.local.entity.ChannelFtsEntity

/**
 * Room database for the IPTV application.
 *
 * Registers:
 * - [ChannelEntity] — primary channels table
 * - [ChannelFtsEntity] — FTS4 virtual table for full-text search
 * - [CategoryEntity] — channel categories/groups
 *
 * **Schema version history:**
 * - v2: Added FTS4 index, category table, expanded channel fields
 */
@Database(
    entities = [
        ChannelEntity::class,
        ChannelFtsEntity::class,
        CategoryEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun categoryDao(): CategoryDao
}
