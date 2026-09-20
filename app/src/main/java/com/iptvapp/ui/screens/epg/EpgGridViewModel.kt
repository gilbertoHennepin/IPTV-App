package com.iptvapp.ui.screens.epg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.remote.dto.EpgListingDto
import com.iptvapp.data.repository.ChannelRepository
import com.iptvapp.data.repository.EpgRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EpgGridViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val epgRepository: EpgRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EpgGridUiState())
    val uiState: StateFlow<EpgGridUiState> = _uiState.asStateFlow()

    init {
        // Load all channels for the grid
        viewModelScope.launch {
            channelRepository.getAllChannels().collect { channels ->
                _uiState.update { it.copy(channels = channels) }
            }
        }
    }

    /**
     * Called by the UI when a channel row becomes visible.
     * Fetches the EPG for that channel and updates the map.
     */
    fun loadEpgForChannel(streamId: Int?) {
        if (streamId == null) return
        
        // Skip if already loaded or currently loading
        if (_uiState.value.epgMap.containsKey(streamId)) return

        // Mark as loading (empty list placeholder)
        _uiState.update {
            val newMap = it.epgMap.toMutableMap()
            newMap[streamId] = emptyList() // Empty list signifies loading or no data
            it.copy(epgMap = newMap)
        }

        viewModelScope.launch {
            val result = epgRepository.getEpgForStream(streamId)
            result.onSuccess { listings ->
                _uiState.update { state ->
                    val newMap = state.epgMap.toMutableMap()
                    newMap[streamId] = listings
                    state.copy(epgMap = newMap)
                }
            }
        }
    }
}

data class EpgGridUiState(
    val channels: List<ChannelEntity> = emptyList(),
    // Map of Stream ID to list of EPG listings
    val epgMap: Map<Int, List<EpgListingDto>> = emptyMap()
)
