package com.iptvapp.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.repository.ChannelRepository
import com.iptvapp.player.PlayerManager
import com.iptvapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Live Video Player screen.
 */
data class PlayerUiState(
    val currentChannel: ChannelEntity? = null,
    val channelList: List<ChannelEntity> = emptyList(),
    val currentIndex: Int = -1,
    val isOsdVisible: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * ViewModel managing live video playback, channel zapping (Up/Down),
 * and auto-hiding OSD overlay state.
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ChannelRepository,
    val playerManager: PlayerManager
) : ViewModel() {

    private val targetChannelId: Long =
        checkNotNull(savedStateHandle[Screen.ARG_CHANNEL_ID]) {
            "channelId parameter was not provided to PlayerViewModel"
        }

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var osdHideJob: Job? = null

    init {
        loadChannelsAndPlayInitial()
        scheduleOsdTimeout()
    }

    private fun loadChannelsAndPlayInitial() {
        viewModelScope.launch {
            repository.getAllChannels().collectLatest { channels ->
                val initialIndex = channels.indexOfFirst { it.id == targetChannelId }
                val currentChannel = if (initialIndex != -1) {
                    channels[initialIndex]
                } else {
                    channels.firstOrNull()
                }

                _uiState.update {
                    it.copy(
                        channelList = channels,
                        currentIndex = if (initialIndex != -1) initialIndex else 0,
                        currentChannel = currentChannel,
                        isLoading = false
                    )
                }

                currentChannel?.let { playChannel(it) }
            }
        }
    }

    /**
     * Prepares and starts playback for the target channel via [PlayerManager].
     */
    fun playChannel(channel: ChannelEntity) {
        _uiState.update { it.copy(currentChannel = channel, isLoading = true, error = null) }
        playerManager.play(channel.url, channel.name)
        _uiState.update { it.copy(isLoading = false) }
        showOsd()
    }

    /**
     * Zap to the next channel in the catalog list (D-Pad DOWN).
     */
    fun zapNext() {
        val state = _uiState.value
        if (state.channelList.isEmpty()) return

        val nextIndex = (state.currentIndex + 1) % state.channelList.size
        val nextChannel = state.channelList[nextIndex]
        _uiState.update { it.copy(currentIndex = nextIndex) }
        playChannel(nextChannel)
    }

    /**
     * Zap to the previous channel in the catalog list (D-Pad UP).
     */
    fun zapPrevious() {
        val state = _uiState.value
        if (state.channelList.isEmpty()) return

        val prevIndex = if (state.currentIndex <= 0) {
            state.channelList.size - 1
        } else {
            state.currentIndex - 1
        }
        val prevChannel = state.channelList[prevIndex]
        _uiState.update { it.copy(currentIndex = prevIndex) }
        playChannel(prevChannel)
    }

    /**
     * Show the OSD overlay and reset the 4-second auto-hide timer.
     */
    fun showOsd() {
        _uiState.update { it.copy(isOsdVisible = true) }
        scheduleOsdTimeout()
    }

    /**
     * Toggle OSD visibility manually (e.g. D-Pad Center press).
     */
    fun toggleOsd() {
        if (_uiState.value.isOsdVisible) {
            _uiState.update { it.copy(isOsdVisible = false) }
            osdHideJob?.cancel()
        } else {
            showOsd()
        }
    }

    private fun scheduleOsdTimeout() {
        osdHideJob?.cancel()
        osdHideJob = viewModelScope.launch {
            delay(4000)
            _uiState.update { it.copy(isOsdVisible = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        osdHideJob?.cancel()
        playerManager.release()
    }
}
