package com.iptvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iptvapp.data.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [ChannelEntity].
 * All queries return suspend functions or Flows for
 * seamless coroutine integration.
 */
@Dao
interface ChannelDao {

    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE `group` = :group ORDER BY name ASC")
    fun getChannelsByGroup(group: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity)

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
}
