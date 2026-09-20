package com.iptvapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.iptvapp.data.local.entity.ChannelEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [ChannelEntity] and its FTS4 shadow table.
 *
 * Read queries return [Flow] for reactive, lifecycle-aware observation.
 * Write operations use suspend functions for structured concurrency.
 */
@Dao
interface ChannelDao {

    // ── Reactive Reads ───────────────────────────────────────────────────

    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE is_favorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE category_id = :categoryId ORDER BY name ASC")
    fun getChannelsByCategory(categoryId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE stream_type = :streamType ORDER BY name ASC")
    fun getChannelsByStreamType(streamType: String): Flow<List<ChannelEntity>>

    /**
     * Full-text search via the FTS4 virtual table.
     *
     * Joins `channels_fts` (the FTS index) back to `channels` (the content table)
     * using the implicit `rowid` ↔ `id` mapping that Room generates for
     * `@Fts4(contentEntity = ChannelEntity::class)`.
     *
     * The MATCH expression appends a wildcard (`*`) to the user's query so that
     * partial prefixes return results (e.g. "ESP" matches "ESPN", "ESPN2").
     *
     * @param query Raw user search text. Caller should sanitize special FTS chars.
     * @return Reactive [Flow] of matching [ChannelEntity] rows.
     */
    @Query(
        """
        SELECT channels.* FROM channels
        JOIN channels_fts ON channels.id = channels_fts.rowid
        WHERE channels_fts MATCH :query || '*'
        ORDER BY channels.name ASC
        """
    )
    fun searchChannels(query: String): Flow<List<ChannelEntity>>

    // ── Single-item Reads ────────────────────────────────────────────────

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Query("SELECT * FROM channels WHERE stream_id = :streamId LIMIT 1")
    suspend fun getChannelByStreamId(streamId: Int): ChannelEntity?

    @Query("SELECT stream_id FROM channels WHERE is_favorite = 1 AND stream_id IS NOT NULL")
    suspend fun getFavoriteStreamIds(): List<Int>

    // ── Writes ───────────────────────────────────────────────────────────

    @Query("UPDATE channels SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: ChannelEntity): Long

    @Update
    suspend fun updateChannel(channel: ChannelEntity)

    @Delete
    suspend fun deleteChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()

    // ── FTS Index Management ─────────────────────────────────────────────

    /**
     * Rebuilds the FTS4 index from the content table.
     * Must be called after bulk inserts/updates/deletes to keep
     * the FTS shadow table in sync with `channels`.
     */
    @Query("INSERT INTO channels_fts(channels_fts) VALUES('rebuild')")
    suspend fun rebuildFtsIndex()

    // ── Transactional Batch Operations ───────────────────────────────────

    /**
     * Atomically replaces all channels and rebuilds the FTS index.
     *
     * Designed for the network → database sync path: wraps the full
     * delete-insert-rebuild cycle in a single Room @Transaction so
     * observers see one consistent state change, and partial failures
     * are rolled back.
     */
    @Transaction
    suspend fun replaceAllChannelsAndRebuildIndex(channels: List<ChannelEntity>) {
        deleteAllChannels()
        insertChannels(channels)
        rebuildFtsIndex()
    }

    /**
     * Inserts a batch of channels and rebuilds the FTS index in one transaction.
     * Use when appending new channels without clearing existing ones.
     */
    @Transaction
    suspend fun insertChannelsAndRebuildIndex(channels: List<ChannelEntity>) {
        insertChannels(channels)
        rebuildFtsIndex()
    }
}
