package com.iptvapp.ui.screens.home

import android.util.Log
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ChannelRepository,
    private val authManager: AuthManager
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    /** All live channels from the local database, observed reactively. */
    val channels: StateFlow<List<ChannelEntity>> = repository.getLiveChannels()
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

    private val _lastFocusedChannelId = MutableStateFlow<Long?>(null)
    val lastFocusedChannelId: StateFlow<Long?> = _lastFocusedChannelId.asStateFlow()

    fun onChannelFocused(channelId: Long) {
        _lastFocusedChannelId.value = channelId
    }

    fun clearFocusTarget() {
        _lastFocusedChannelId.value = null
    }

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    init {
        syncData()
    }

    /**
     * Syncs live TV, VOD, and series data from the Xtream server.
     * Each sync step is independent — if VOD fails, series still runs.
     * Errors are logged but don't crash the app.
     */
    fun syncData() {
        viewModelScope.launch {
            if (_isSyncing.value) return@launch

            val host = authManager.hostUrlFlow.firstOrNull() ?: return@launch
            val user = authManager.usernameFlow.firstOrNull() ?: return@launch
            val pass = authManager.passwordFlow.firstOrNull() ?: return@launch

            _isSyncing.value = true

            try {
                // Step 1: Live TV
                _syncMessage.value = "Syncing Live TV..."
                val liveResult = repository.syncLiveStreams(host, user, pass)
                if (liveResult.isFailure) {
                    Log.e(TAG, "Live TV sync failed", liveResult.exceptionOrNull())
                    _syncMessage.value = "Live TV sync error"
                }

                // Step 2: VOD (always attempt, even if live failed)
                _syncMessage.value = "Syncing Movies..."
                val vodResult = repository.syncVodStreams(host, user, pass)
                if (vodResult.isFailure) {
                    Log.e(TAG, "VOD sync failed", vodResult.exceptionOrNull())
                    _syncMessage.value = "Movies sync error"
                }

                // Step 3: Series (always attempt)
                _syncMessage.value = "Syncing TV Series..."
                val seriesResult = repository.syncSeries(host, user, pass)
                if (seriesResult.isFailure) {
                    Log.e(TAG, "Series sync failed", seriesResult.exceptionOrNull())
                    _syncMessage.value = "Series sync error"
                }

                _syncMessage.value = "Sync Complete!"
            } catch (e: Exception) {
                Log.e(TAG, "Sync failed with exception", e)
                _syncMessage.value = "Sync failed"
            } finally {
                kotlinx.coroutines.delay(2000)
                _isSyncing.value = false
                _syncMessage.value = null
            }
        }
    }

    fun toggleFavorite(channel: ChannelEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }
}
