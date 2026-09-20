package com.iptvapp.ui.screens.series

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.data.remote.dto.EpisodeDto
import com.iptvapp.data.remote.dto.SeriesInfoResponse
import com.iptvapp.data.repository.AuthManager
import com.iptvapp.data.repository.ChannelRepository
import com.iptvapp.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesDetailUiState(
    val isLoading: Boolean = true,
    val seriesInfo: SeriesInfoResponse? = null,
    val error: String? = null
)

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ChannelRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val seriesId: Long =
        checkNotNull(savedStateHandle[Screen.ARG_CHANNEL_ID]) {
            "channelId parameter was not provided to SeriesDetailViewModel"
        }

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    init {
        fetchSeriesInfo()
    }

    private fun fetchSeriesInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val host = authManager.hostUrlFlow.firstOrNull() ?: return@launch
            val user = authManager.usernameFlow.firstOrNull() ?: return@launch
            val pass = authManager.passwordFlow.firstOrNull() ?: return@launch

            val result = repository.getSeriesInfo(host, user, pass, seriesId.toInt())
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        seriesInfo = result.getOrNull()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    /**
     * Creates a temporary database entry for the episode so the PlayerScreen
     * can load it, and returns the generated local ID.
     */
    fun createPlayableEpisode(episode: EpisodeDto, onComplete: (Long) -> Unit) {
        viewModelScope.launch {
            val host = authManager.hostUrlFlow.firstOrNull() ?: return@launch
            val user = authManager.usernameFlow.firstOrNull() ?: return@launch
            val pass = authManager.passwordFlow.firstOrNull() ?: return@launch

            val generatedId = repository.createEpisodeChannel(host, user, pass, episode)
            onComplete(generatedId)
        }
    }
}
