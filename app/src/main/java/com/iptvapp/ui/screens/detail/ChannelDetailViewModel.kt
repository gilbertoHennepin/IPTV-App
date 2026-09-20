package com.iptvapp.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.remote.dto.EpgListingDto
import com.iptvapp.data.repository.ChannelRepository
import com.iptvapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Channel Detail / Player screen.
 *
 * Reads the channel ID from the navigation argument via [SavedStateHandle]
 * and loads the channel entity from the local database.
 */
@HiltViewModel
class ChannelDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ChannelRepository
) : ViewModel() {

    private val channelId: Long = savedStateHandle.get<Long>(Screen.ARG_CHANNEL_ID) ?: -1L

    private val _channel = MutableStateFlow<ChannelEntity?>(null)
    val channel: StateFlow<ChannelEntity?> = _channel.asStateFlow()

    private val _epgListings = MutableStateFlow<List<EpgListingDto>>(emptyList())
    val epgListings: StateFlow<List<EpgListingDto>> = _epgListings.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadChannel()
    }

    private fun loadChannel() {
        viewModelScope.launch {
            _isLoading.value = true
            _channel.value = repository.getChannelById(channelId)
            _isLoading.value = false
        }
    }
}
