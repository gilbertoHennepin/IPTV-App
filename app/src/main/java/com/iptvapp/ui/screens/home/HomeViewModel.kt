package com.iptvapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.local.entity.CategoryEntity
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.repository.AuthManager
import com.iptvapp.data.repository.ChannelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val repository: ChannelRepository,
    private val authManager: AuthManager
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

    /** True while a sync operation is in progress. */
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    /** Message representing sync progress. */
    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        // Automatically sync data when the ViewModel is created (if channels are empty)
        syncData()
    }

    /**
     * Authenticates via AuthManager and synchronizes live TV and VOD channels.
     */
    fun syncData() {
        viewModelScope.launch {
            if (_isSyncing.value) return@launch

            val host = authManager.hostUrlFlow.firstOrNull() ?: return@launch
            val user = authManager.usernameFlow.firstOrNull() ?: return@launch
            val pass = authManager.passwordFlow.firstOrNull() ?: return@launch

            _isSyncing.value = true
            _syncMessage.value = "Syncing Live TV..."

            try {
                // Since replaceAllChannelsAndRebuildIndex deletes all channels, we must sync Live streams first.
                val liveResult = repository.syncLiveStreams(host, user, pass)
                if (liveResult.isSuccess) {
                    _syncMessage.value = "Syncing Movies (VOD)..."
                    val vodResult = repository.syncVodStreams(host, user, pass)
                    if (vodResult.isSuccess) {
                        _syncMessage.value = "Syncing TV Series..."
                        val seriesResult = repository.syncSeries(host, user, pass)
                        if (seriesResult.isSuccess) {
                            _syncMessage.value = "Sync Complete!"
                        } else {
                            _syncMessage.value = "Error syncing Series: ${seriesResult.exceptionOrNull()?.message}"
                        }
                    } else {
                        _syncMessage.value = "Error syncing VOD: ${vodResult.exceptionOrNull()?.message}"
                    }
                } else {
                    _syncMessage.value = "Error syncing Live TV: ${liveResult.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _syncMessage.value = "Sync failed: ${e.message}"
            } finally {
                kotlinx.coroutines.delay(2000) // Keep message visible for a moment
                _isSyncing.value = false
                _syncMessage.value = null
            }
        }
    }
}
