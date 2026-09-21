package com.iptvapp.ui.screens.multiview

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.iptvapp.data.local.entity.ChannelEntity
import com.iptvapp.data.repository.ChannelRepository
import com.iptvapp.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MultiStreamLayout {
    TWO_STREAMS,
    THREE_STREAMS,
    FOUR_STREAMS
}

data class StreamPaneState(
    val id: Int,
    val player: ExoPlayer?,
    val currentChannel: ChannelEntity? = null,
    val isFocused: Boolean = false
)

@OptIn(UnstableApi::class)
@HiltViewModel
class MultiStreamViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ChannelRepository,
    private val playerManager: PlayerManager // used just for the factory methods if needed
) : ViewModel() {

    private val _layout = MutableStateFlow(MultiStreamLayout.TWO_STREAMS)
    val layout: StateFlow<MultiStreamLayout> = _layout.asStateFlow()

    private val _panes = MutableStateFlow<List<StreamPaneState>>(emptyList())
    val panes: StateFlow<List<StreamPaneState>> = _panes.asStateFlow()

    private val _liveChannels = MutableStateFlow<List<ChannelEntity>>(emptyList())
    val liveChannels: StateFlow<List<ChannelEntity>> = _liveChannels.asStateFlow()

    private val _isSelectingChannelForPane = MutableStateFlow<Int?>(null)
    val isSelectingChannelForPane: StateFlow<Int?> = _isSelectingChannelForPane.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getLiveChannels().collect { channels ->
                _liveChannels.value = channels
            }
        }
        setLayout(MultiStreamLayout.TWO_STREAMS)
    }

    fun setLayout(newLayout: MultiStreamLayout) {
        val requiredPanes = when (newLayout) {
            MultiStreamLayout.TWO_STREAMS -> 2
            MultiStreamLayout.THREE_STREAMS -> 3
            MultiStreamLayout.FOUR_STREAMS -> 4
        }

        val currentPanes = _panes.value.toMutableList()

        // Remove extra panes
        while (currentPanes.size > requiredPanes) {
            val removed = currentPanes.removeLast()
            removed.player?.release()
        }

        // Add new panes
        while (currentPanes.size < requiredPanes) {
            val newId = currentPanes.size
            currentPanes.add(StreamPaneState(id = newId, player = createPlayer()))
        }

        // Ensure exactly one pane is focused (for audio)
        val hasFocus = currentPanes.any { it.isFocused }
        if (!hasFocus && currentPanes.isNotEmpty()) {
            currentPanes[0] = currentPanes[0].copy(isFocused = true)
            currentPanes[0].player?.volume = 1f
        }

        _layout.value = newLayout
        _panes.value = currentPanes
        updateAudioState()
    }

    private fun createPlayer(): ExoPlayer? {
        return try {
            val dataSourceFactory = playerManager.createHttpDataSourceFactory()
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(dataSourceFactory)
            
            // Constrained buffer for multi-stream to avoid OOM
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(15000, 15000, 1500, 3000)
                .build()

            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build().apply {
                    volume = 0f // Mute by default
                }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            null
        }
    }

    fun onPaneFocused(paneId: Int) {
        _panes.update { current ->
            current.map { pane ->
                pane.copy(isFocused = pane.id == paneId)
            }
        }
        updateAudioState()
    }

    private fun updateAudioState() {
        _panes.value.forEach { pane ->
            pane.player?.volume = if (pane.isFocused) 1f else 0f
        }
    }

    fun showChannelSelector(paneId: Int) {
        _isSelectingChannelForPane.value = paneId
    }

    fun hideChannelSelector() {
        _isSelectingChannelForPane.value = null
    }

    fun selectChannelForPane(channel: ChannelEntity) {
        val paneId = _isSelectingChannelForPane.value ?: return
        
        _panes.update { current ->
            current.map { pane ->
                if (pane.id == paneId) {
                    pane.player?.apply {
                        setMediaItem(MediaItem.fromUri(channel.url))
                        prepare()
                        playWhenReady = true
                    }
                    pane.copy(currentChannel = channel)
                } else {
                    pane
                }
            }
        }
        hideChannelSelector()
    }

    override fun onCleared() {
        super.onCleared()
        _panes.value.forEach { it.player?.release() }
    }
}
