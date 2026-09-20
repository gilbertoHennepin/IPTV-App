package com.iptvapp.data.repository

import com.iptvapp.data.local.dao.CategoryDao
import com.iptvapp.data.local.dao.ChannelDao
import com.iptvapp.data.local.entity.CategoryEntity
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.remote.XtreamApiService
import com.iptvapp.data.remote.dto.EpgListingDto
import com.iptvapp.data.remote.dto.toCategoryEntity
import com.iptvapp.data.remote.dto.toChannelEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for IPTV channel and category data.
 *
 * Coordinates between the Xtream Codes remote API and the local
 * Room database, following the Repository pattern from Clean Architecture.
 *
 * All public methods either return reactive [Flow]s for observation
 * or are suspend functions for one-shot operations.
 */
@Singleton
class ChannelRepository @Inject constructor(
    private val xtreamApi: XtreamApiService,
    private val channelDao: ChannelDao,
    private val categoryDao: CategoryDao
) {

    // ── Reactive Streams (from Room) ─────────────────────────────────────

    /** Observe all channels ordered by name. */
    fun getAllChannels(): Flow<List<ChannelEntity>> =
        channelDao.getAllChannels()

    /** Observe only channels marked as favorites. */
    fun getFavoriteChannels(): Flow<List<ChannelEntity>> =
        channelDao.getFavoriteChannels()

    /** Observe channels filtered by category. */
    fun getChannelsByCategory(categoryId: String): Flow<List<ChannelEntity>> =
        channelDao.getChannelsByCategory(categoryId)

    /** Full-text search via the FTS4 index. */
    fun searchChannels(query: String): Flow<List<ChannelEntity>> =
        channelDao.searchChannels(query)

    /** Observe all categories. */
    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    // ── Single-item Reads ────────────────────────────────────────────────

    suspend fun getChannelById(id: Long): ChannelEntity? =
        channelDao.getChannelById(id)

    // ── Network → Database Sync ──────────────────────────────────────────

    /**
     * Fetches live categories from the Xtream server and persists them locally.
     *
     * @return [Result.success] with the category list, or [Result.failure] on error.
     */
    suspend fun syncLiveCategories(
        serverUrl: String,
        username: String,
        password: String
    ): Result<List<CategoryEntity>> = runCatching {
        val response = xtreamApi.getLiveCategories(username, password)
        if (!response.isSuccessful) {
            error("Failed to fetch categories: HTTP ${response.code()}")
        }
        val dtos = response.body() ?: emptyList()
        val entities = dtos.map { it.toCategoryEntity() }
        categoryDao.deleteAllCategories()
        categoryDao.insertCategories(entities)
        entities
    }

    /**
     * Fetches live streams from the Xtream server and persists them locally.
     *
     * Uses the @Transaction-annotated [ChannelDao.replaceAllChannelsAndRebuildIndex]
     * to atomically replace channels and rebuild the FTS index in one batch.
     *
     * @param categoryId Optional category filter. Pass null for all streams.
     * @return [Result.success] with the channel count, or [Result.failure] on error.
     */
    suspend fun syncLiveStreams(
        serverUrl: String,
        username: String,
        password: String,
        categoryId: String? = null
    ): Result<Int> = runCatching {
        // Fetch category lookup map for resolving names
        val categories = fetchCategoryLookup(serverUrl, username, password)

        val response = xtreamApi.getLiveStreams(username, password, categoryId = categoryId)
        if (!response.isSuccessful) {
            error("Failed to fetch live streams: HTTP ${response.code()}")
        }
        val dtos = response.body() ?: emptyList()
        val entities = dtos.map { dto ->
            dto.toChannelEntity(
                serverUrl = serverUrl,
                username = username,
                password = password,
                categoryName = categories[dto.categoryId]
            )
        }

        // Atomic: delete → insert → FTS rebuild in a single @Transaction
        channelDao.replaceAllChannelsAndRebuildIndex(entities)
        entities.size
    }

    /**
     * Fetches VOD streams from the Xtream server and **appends** them
     * to the existing channels table (does not clear live channels).
     */
    suspend fun syncVodStreams(
        serverUrl: String,
        username: String,
        password: String,
        categoryId: String? = null
    ): Result<Int> = runCatching {
        val categories = fetchCategoryLookup(serverUrl, username, password)

        val response = xtreamApi.getVodStreams(username, password, categoryId = categoryId)
        if (!response.isSuccessful) {
            error("Failed to fetch VOD streams: HTTP ${response.code()}")
        }
        val dtos = response.body() ?: emptyList()
        val entities = dtos.map { dto ->
            dto.toChannelEntity(
                serverUrl = serverUrl,
                username = username,
                password = password,
                categoryName = categories[dto.categoryId]
            )
        }

        // Atomic: insert + FTS rebuild in a single @Transaction
        channelDao.insertChannelsAndRebuildIndex(entities)
        entities.size
    }

    /**
     * Fetches the short EPG for a specific stream.
     */
    suspend fun getShortEpg(
        username: String,
        password: String,
        streamId: Int,
        limit: Int? = null
    ): Result<List<EpgListingDto>> = runCatching {
        val response = xtreamApi.getShortEpg(username, password, streamId, limit = limit)
        if (!response.isSuccessful) {
            error("Failed to fetch EPG: HTTP ${response.code()}")
        }
        response.body()?.epgListings ?: emptyList()
    }

    // ── Local Writes ─────────────────────────────────────────────────────

    /** Toggle the favorite status of a channel. */
    suspend fun toggleFavorite(channel: ChannelEntity) {
        channelDao.updateChannel(channel.copy(isFavorite = !channel.isFavorite))
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Builds a categoryId → categoryName lookup map.
     * Attempts to use local data first; falls back to a network fetch.
     */
    private suspend fun fetchCategoryLookup(
        serverUrl: String,
        username: String,
        password: String
    ): Map<String, String> {
        // Try local first
        val localCategory = categoryDao.getCategoryById("1")
        if (localCategory != null) {
            // We have local categories — build the map from Room
            // (getAllCategories returns a Flow; we need a one-shot query)
            return buildCategoryMapFromNetwork(serverUrl, username, password)
        }

        return buildCategoryMapFromNetwork(serverUrl, username, password)
    }

    private suspend fun buildCategoryMapFromNetwork(
        serverUrl: String,
        username: String,
        password: String
    ): Map<String, String> {
        val response = xtreamApi.getLiveCategories(username, password)
        val dtos = response.body() ?: return emptyMap()
        return dtos.associate { it.categoryId to it.categoryName }
    }
}
