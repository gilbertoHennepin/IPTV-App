package com.iptvapp.data.repository

import android.util.LruCache
import com.iptvapp.data.remote.XtreamApiService
import com.iptvapp.data.remote.dto.EpgListingDto
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository handling the fetching and caching of Electronic Program Guide (EPG) data.
 */
@Singleton
class EpgRepository @Inject constructor(
    private val xtreamApi: XtreamApiService,
    private val authManager: AuthManager
) {
    // Cache up to 100 channels' EPG data to prevent memory bloat
    private val epgCache = LruCache<Int, List<EpgListingDto>>(100)

    /**
     * Fetches the short EPG for a specific stream.
     * Returns cached data if available, otherwise fetches from network.
     */
    suspend fun getEpgForStream(streamId: Int, forceRefresh: Boolean = false): Result<List<EpgListingDto>> {
        if (!forceRefresh) {
            val cached = epgCache.get(streamId)
            if (cached != null) {
                return Result.success(cached)
            }
        }

        return runCatching {
            val host = authManager.hostUrlFlow.firstOrNull() ?: error("Host URL not set")
            val username = authManager.usernameFlow.firstOrNull() ?: error("Username not set")
            val password = authManager.passwordFlow.firstOrNull() ?: error("Password not set")

            // We limit to 20 programs per channel to keep the payload small
            val response = xtreamApi.getShortEpg(
                url = host,
                username = username,
                password = password,
                streamId = streamId,
                limit = 20
            )

            if (!response.isSuccessful) {
                error("Failed to fetch EPG: HTTP ${response.code()}")
            }

            val listings = response.body()?.epgListings ?: emptyList()
            epgCache.put(streamId, listings)
            listings
        }
    }

    /**
     * Clears the in-memory EPG cache.
     */
    fun clearCache() {
        epgCache.evictAll()
    }
}
