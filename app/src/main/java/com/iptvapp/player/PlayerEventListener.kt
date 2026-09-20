package com.iptvapp.player

import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

/**
 * Reusable [Player.Listener] that routes ExoPlayer callbacks to
 * lambda handlers, making it easy for ViewModels and Composables
 * to react to playback state changes without implementing the full
 * interface.
 *
 * Usage:
 * ```
 * val listener = PlayerEventListener(
 *     onStateChanged = { state -> /* update UI */ },
 *     onError = { error -> /* show snackbar */ }
 * )
 * playerManager.playerListener = listener
 * ```
 */
@OptIn(UnstableApi::class)
class PlayerEventListener(
    private val onStateChanged: ((PlaybackState) -> Unit)? = null,
    private val onError: ((PlayerError) -> Unit)? = null,
    private val onIsPlayingChanged: ((Boolean) -> Unit)? = null
) : Player.Listener {

    companion object {
        private const val TAG = "PlayerEventListener"
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        val state = when (playbackState) {
            Player.STATE_IDLE -> PlaybackState.IDLE
            Player.STATE_BUFFERING -> PlaybackState.BUFFERING
            Player.STATE_READY -> PlaybackState.READY
            Player.STATE_ENDED -> PlaybackState.ENDED
            else -> PlaybackState.IDLE
        }
        Log.d(TAG, "Playback state: $state")
        onStateChanged?.invoke(state)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(TAG, "isPlaying: $isPlaying")
        onIsPlayingChanged?.invoke(isPlaying)
    }

    override fun onPlayerError(error: PlaybackException) {
        val playerError = PlayerError(
            message = error.message ?: "Unknown playback error",
            errorCode = error.errorCode,
            cause = error.cause,
            isRecoverable = error.errorCode != PlaybackException.ERROR_CODE_DECODER_INIT_FAILED
                && error.errorCode != PlaybackException.ERROR_CODE_DRM_SYSTEM_ERROR
        )
        Log.e(TAG, "Player error [${playerError.errorCode}]: ${playerError.message}", error)
        onError?.invoke(playerError)
    }
}

/**
 * Simplified playback state exposed to the UI layer.
 */
enum class PlaybackState {
    IDLE,
    BUFFERING,
    READY,
    ENDED
}

/**
 * Structured error type for player failures.
 *
 * @property isRecoverable Hint to the UI: `true` if a retry might succeed
 *           (e.g. network timeout), `false` if the error is terminal
 *           (e.g. decoder init failure).
 */
data class PlayerError(
    val message: String,
    val errorCode: Int,
    val cause: Throwable?,
    val isRecoverable: Boolean
)
