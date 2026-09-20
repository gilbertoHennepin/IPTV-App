package com.iptvapp.data.remote

import retrofit2.http.GET
import retrofit2.http.Url

/**
 * Retrofit API service for fetching remote IPTV data.
 * Uses @Url for dynamic endpoint resolution.
 */
interface ApiService {

    @GET
    suspend fun fetchPlaylist(@Url url: String): retrofit2.Response<String>
}
