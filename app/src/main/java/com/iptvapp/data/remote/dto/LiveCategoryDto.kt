package com.iptvapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Represents a live stream category returned by the Xtream Codes API.
 * Endpoint: player_api.php?action=get_live_categories
 */
data class LiveCategoryDto(
    @SerializedName("category_id")
    val categoryId: String,

    @SerializedName("category_name")
    val categoryName: String,

    @SerializedName("parent_id")
    val parentId: Int = 0
)
