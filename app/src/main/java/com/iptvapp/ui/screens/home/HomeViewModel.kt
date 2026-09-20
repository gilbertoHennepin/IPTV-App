package com.iptvapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.local.entity.CategoryEntity
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 *
 * Exposes reactive [StateFlow]s for the channel list and search results,
 * and tracks the ID of the last focused card so the grid can restore
 * focus after navigating back from the detail screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ChannelRepository
) : ViewModel() {

    /** All channels from the local database, observed reactively. */
    val channels: StateFlow<List<ChannelEntity>> = repository.getAllChannels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** All categories from the local database. */
    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    /** Current search query text. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Search results (empty if no query). */
    private val _searchResults = MutableStateFlow<List<ChannelEntity>>(emptyList())
    val searchResults: StateFlow<List<ChannelEntity>> = _searchResults.asStateFlow()

    /**
     * The ID of the last card that held D-Pad focus.
     *
     * Persisted across configuration changes via the ViewModel lifecycle.
     * The HomeScreen reads this after a [popBackStack] to re-focus the
     * correct card via [FocusRequester.requestFocus].
     */
    private val _lastFocusedChannelId = MutableStateFlow<Long?>(null)
    val lastFocusedChannelId: StateFlow<Long?> = _lastFocusedChannelId.asStateFlow()

    /** Called when a card gains D-Pad focus. */
    fun onChannelFocused(channelId: Long) {
        _lastFocusedChannelId.value = channelId
    }

    /** Clears the stored focus target (e.g. after successful restore). */
    fun clearFocusTarget() {
        _lastFocusedChannelId.value = null
    }
}
