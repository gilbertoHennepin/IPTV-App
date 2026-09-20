package com.iptvapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single IPTV channel stored in the local database.
 */
@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val group: String? = null,
    val isFavorite: Boolean = false
)
