package com.iptvapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iptvapp.data.local.dao.ChannelDao
import com.iptvapp.data.local.entity.ChannelEntity

/**
 * Room database for the IPTV application.
 * Add entities and DAOs as the app grows.
 */
@Database(
    entities = [ChannelEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
}
