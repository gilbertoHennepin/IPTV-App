package com.iptvapp.data.remote.dto

import com.iptvapp.data.local.entity.CategoryEntity
import com.iptvapp.data.local.entity.ChannelEntity

/**
 * Extension functions that map network DTOs to local Room entities.
 *
 * These live in the data layer (not domain) because they are tightly
 * coupled to the Xtream Codes API response shape and the Room schema.
 */

/**
 * Maps a [LiveStreamDto] to a [ChannelEntity].
 *
 * @param serverUrl  The Xtream server base URL (e.g. "http://myserver.com:8080")
 * @param username   Xtream Codes username for stream URL construction
 * @param password   Xtream Codes password for stream URL construction
 * @param categoryName  Resolved category name (looked up from category ID)
 */
fun LiveStreamDto.toChannelEntity(
    serverUrl: String,
    username: String,
    password: String,
    categoryName: String? = null
): ChannelEntity = ChannelEntity(
    name = name,
    url = "$serverUrl/live/$username/$password/$streamId.ts",
    logoUrl = streamIcon,
    categoryName = categoryName,
    categoryId = categoryId,
    streamId = streamId,
    epgChannelId = epgChannelId,
    streamType = streamType ?: "live",
    tvArchive = tvArchive == 1,
    addedTimestamp = added?.toLongOrNull()
)

/**
 * Maps a [VodStreamDto] to a [ChannelEntity].
 */
fun VodStreamDto.toChannelEntity(
    serverUrl: String,
    username: String,
    password: String,
    categoryName: String? = null
): ChannelEntity = ChannelEntity(
    name = name,
    url = "$serverUrl/movie/$username/$password/$streamId.${containerExtension ?: "mp4"}",
    logoUrl = streamIcon,
    categoryName = categoryName,
    categoryId = categoryId,
    streamId = streamId,
    streamType = streamType ?: "movie",
    addedTimestamp = added?.toLongOrNull()
)

/**
 * Maps a [LiveCategoryDto] to a [CategoryEntity].
 */
fun LiveCategoryDto.toCategoryEntity(): CategoryEntity = CategoryEntity(
    categoryId = categoryId,
    categoryName = categoryName,
    parentId = parentId
)
