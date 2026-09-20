package com.iptvapp.player

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central media playback engine for the IPTV application.
 *
 * Configures ExoPlayer with:
 * - **Custom HTTP headers** (User-Agent + Referer) to bypass provider WAF restrictions
 * - **Drip-feed buffer strategy** tuned for low-end streaming sticks (Fire Stick, etc.)
 * - **Graceful decoder exhaustion handling** for multi-view scenarios
 *
 * This class owns the [ExoPlayer] lifecycle. Callers must invoke [release] when
 * playback is no longer needed.
 */
@OptIn(UnstableApi::class)
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "PlayerManager"

        // ── Header Spoofing ──────────────────────────────────────────────
        private const val USER_AGENT = "VLC/3.0.18 LibVLC/3.0.18"
        private const val DEFAULT_REFERER = "https://www.google.com"

        // ── Drip-Feed Buffer Strategy ────────────────────────────────────
        // Constraining both min and max to 30s prevents ExoPlayer from
        // aggressively buffering ahead, which causes OOM / thermal
        // throttling on cheap ARM SoCs (Amlogic S905, MediaTek MT8695).
        private const val MIN_BUFFER_MS = 30_000
        private const val MAX_BUFFER_MS = 30_000
        private const val BUFFER_FOR_PLAYBACK_MS = 1_500
        private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 3_000
    }

    /** The active [ExoPlayer] instance, or `null` if not yet initialized / released. */
    var player: ExoPlayer? = null
        private set

    /** Listener for player events — set by the UI layer. */
    var playerListener: Player.Listener? = null

    // ── Data Source Factory ──────────────────────────────────────────────

    /**
     * Creates a [DataSource.Factory] that injects custom HTTP headers on
     * every request to bypass provider-side WAF / CDN restrictions.
     *
     * Headers set:
     * - `User-Agent` — spoofed as VLC to match allowed client fingerprints
     * - `Referer` — generic referer to satisfy origin checks
     *
     * @param userAgent  Custom User-Agent string (defaults to VLC 3.0.18)
     * @param referer    Custom Referer header (defaults to google.com)
     * @return Configured [DefaultHttpDataSource.Factory]
     */
    fun createHttpDataSourceFactory(
        userAgent: String = USER_AGENT,
        referer: String = DEFAULT_REFERER
    ): DataSource.Factory {
        return DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setDefaultRequestProperties(
                mapOf(
                    "User-Agent" to userAgent,
                    "Referer" to referer
                )
            )
            .setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
            .setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
            .setAllowCrossProtocolRedirects(true)
    }

    // ── Load Control (Drip-Feed Buffer) ──────────────────────────────────

    /**
     * Builds a [DefaultLoadControl] with a constrained "drip-feed" buffer strategy.
     *
     * By pinning [minBufferMs] and [maxBufferMs] to the same value (30 s),
     * ExoPlayer will not aggressively pre-buffer beyond what is needed,
     * keeping memory usage predictable on devices with ≤ 1 GB RAM.
     *
     * [bufferForPlaybackMs] is set low (1.5 s) so playback starts quickly
     * even on slow connections — the player will top up the buffer
     * incrementally rather than blocking until a large chunk is loaded.
     */
    private fun buildLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */             MIN_BUFFER_MS,
                /* maxBufferMs = */             MAX_BUFFER_MS,
                /* bufferForPlaybackMs = */     BUFFER_FOR_PLAYBACK_MS,
                /* bufferForPlaybackAfterRebufferMs = */ BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    // ── Player Initialization ────────────────────────────────────────────

    /**
     * Initializes (or re-initializes) the [ExoPlayer] instance.
     *
     * Wraps construction in a try-catch targeting [IllegalStateException] to
     * gracefully handle **hardware decoder exhaustion** — a common failure
     * mode on low-end streaming sticks when multiple player instances are
     * created without properly releasing previous ones (e.g. multi-view / PiP).
     *
     * @param userAgent  Optional custom User-Agent override
     * @param referer    Optional custom Referer override
     * @return The initialized [ExoPlayer], or `null` if decoder resources
     *         are exhausted and the player could not be created.
     */
    fun initialize(
        userAgent: String = USER_AGENT,
        referer: String = DEFAULT_REFERER
    ): ExoPlayer? {
        // Release any existing instance to free decoder resources
        release()

        return try {
            val httpDataSourceFactory = createHttpDataSourceFactory(userAgent, referer)
            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(httpDataSourceFactory)
            val loadControl = buildLoadControl()

            val exoPlayer = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .setHandleAudioBecomingNoisy(true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .build()
                .apply {
                    playWhenReady = true
                    // Attach external listener if one has been set
                    playerListener?.let { addListener(it) }
                }

            player = exoPlayer
            Log.d(TAG, "ExoPlayer initialized successfully")
            exoPlayer
        } catch (e: IllegalStateException) {
            // Hardware decoder exhaustion — all codec instances are in use.
            // This happens on devices with limited MediaCodec slots when
            // multiple ExoPlayer instances are created without releasing.
            Log.e(
                TAG,
                "Failed to initialize ExoPlayer: hardware decoder exhaustion. " +
                    "Release unused players before creating new instances.",
                e
            )
            player = null
            null
        }
    }

    // ── Playback Controls ────────────────────────────────────────────────

    /**
     * Prepares and starts playback of the given stream URL.
     *
     * @param streamUrl  The URL to play (HLS, DASH, RTMP, or progressive)
     * @param title      Optional title for the media item metadata
     */
    fun play(streamUrl: String, title: String? = null) {
        val currentPlayer = player ?: initialize() ?: run {
            Log.e(TAG, "Cannot play: player initialization failed (decoder exhaustion)")
            return
        }

        val mediaItem = MediaItem.Builder()
            .setUri(streamUrl)
            .apply { title?.let { setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(it)
                    .build()
            )}}
            .build()

        currentPlayer.setMediaItem(mediaItem)
        currentPlayer.prepare()
        currentPlayer.playWhenReady = true
    }

    /**
     * Stops playback and clears the media item, but keeps the player alive
     * for rapid re-use (avoids decoder re-acquisition cost).
     */
    fun stop() {
        player?.apply {
            stop()
            clearMediaItems()
        }
    }

    /** Pauses playback without releasing the player. */
    fun pause() {
        player?.pause()
    }

    /** Resumes playback if paused. */
    fun resume() {
        player?.play()
    }

    /**
     * Releases the [ExoPlayer] instance and all associated resources.
     *
     * Must be called when the player is no longer needed (e.g. Activity
     * destruction, navigation away from the player screen) to free
     * hardware decoder slots.
     */
    fun release() {
        player?.let { p ->
            playerListener?.let { p.removeListener(it) }
            p.stop()
            p.release()
            Log.d(TAG, "ExoPlayer released")
        }
        player = null
    }
}
