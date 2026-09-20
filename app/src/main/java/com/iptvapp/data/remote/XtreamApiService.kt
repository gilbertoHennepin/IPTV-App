package com.iptvapp.data.remote

import com.iptvapp.data.remote.dto.LiveCategoryDto
import com.iptvapp.data.remote.dto.LiveStreamDto
import com.iptvapp.data.remote.dto.ShortEpgResponse
import com.iptvapp.data.remote.dto.VodStreamDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface targeting the Xtream Codes **player_api.php** endpoint.
 *
 * Every request requires `username` and `password` query parameters
 * for authentication against the Xtream Codes server.
 */
interface XtreamApiService {

    /**
     * Fetches all available live stream categories.
     *
     * GET player_api.php?username=…&password=…&action=get_live_categories
     */
    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): Response<List<LiveCategoryDto>>

    /**
     * Fetches all live streams. Optionally filter by [categoryId].
     *
     * GET player_api.php?username=…&password=…&action=get_live_streams[&category_id=…]
     */
    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<LiveStreamDto>>

    /**
     * Fetches all VOD (Video On Demand) streams. Optionally filter by [categoryId].
     *
     * GET player_api.php?username=…&password=…&action=get_vod_streams[&category_id=…]
     */
    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams",
        @Query("category_id") categoryId: String? = null
    ): Response<List<VodStreamDto>>

    /**
     * Fetches the short EPG (Electronic Program Guide) for a given [streamId].
     * Optionally limit the number of results.
     *
     * GET player_api.php?username=…&password=…&action=get_short_epg&stream_id=…[&limit=…]
     */
    @GET("player_api.php")
    suspend fun getShortEpg(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("stream_id") streamId: Int,
        @Query("action") action: String = "get_short_epg",
        @Query("limit") limit: Int? = null
    ): Response<ShortEpgResponse>
}
