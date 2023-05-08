package com.idance.hocnhayonline.customView.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.MediaLibraryInfo
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.RepeatModeUtil.RepeatToggleModes
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.DefaultTrackNameProvider
import androidx.media3.ui.R
import androidx.media3.ui.TimeBar
import androidx.media3.ui.TimeBar.OnScrubListener
import androidx.media3.ui.TrackNameProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.collect.ImmutableList
import java.util.Formatter
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min

@SuppressLint("PrivateResource")
@UnstableApi
class PlayerControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? =  /* attrs= */null,
    defStyleAttr: Int =  /* defStyleAttr= */0,
    playbackAttrs: AttributeSet? = attrs
) : FrameLayout(context, attrs, defStyleAttr) {

    @Deprecated(
        """Register a {@link PlayerView.ControllerVisibilityListener} via {@link
     * PlayerView#setControllerVisibilityListener(PlayerView.ControllerVisibilityListener)}
      instead. Using {@link PlayerControlView} as a standalone class without {@link PlayerView}
      is deprecated."""
    )
    interface VisibilityListener {
        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either [View.VISIBLE] or [View.GONE].
         */
        fun onVisibilityChange(visibility: Int)
    }

    /**
     * Listener to be notified when progress has been updated.
     */
    interface ProgressUpdateListener {
        /**
         * Called when progress needs to be updated.
         *
         * @param position         The current position.
         * @param bufferedPosition The current buffered position.
         */
        fun onProgressUpdate(position: Long, bufferedPosition: Long)
    }

    @Deprecated(
        """Register a {@link PlayerView.FullscreenButtonClickListener} via {@link
     * PlayerView#setFullscreenButtonClickListener(PlayerView.FullscreenButtonClickListener)}
      instead. Using {@link PlayerControlView} as a standalone class without {@link PlayerView}
      is deprecated."""
    )
    interface OnFullScreenModeChangedListener {
        /**
         * Called to indicate a fullscreen mode change.
         *
         * @param isFullScreen `true` if the video rendering surface should be fullscreen `false` otherwise.
         */
        fun onFullScreenModeChanged(isFullScreen: Boolean)
    }

    private val controlViewLayoutManager: PlayerControlViewLayoutManager
    private val resources: Resources
    private val componentListener: ComponentListener

    // Using the deprecated type for now.
    private val visibilityListeners: CopyOnWriteArrayList<VisibilityListener>
    private val settingsView: RecyclerView
    private val settingsAdapter: SettingsAdapter
    private val playbackSpeedAdapter: PlaybackSpeedAdapter
    private val textTrackSelectionAdapter: TextTrackSelectionAdapter
    private val audioTrackSelectionAdapter: AudioTrackSelectionAdapter

    // TODO(insun): Add setTrackNameProvider to use customized track name provider.
    private val trackNameProvider: TrackNameProvider
    private val settingsWindow: PopupWindow
    private val settingsWindowMargin: Int
    private val previousButton: View?
    private val nextButton: View?
    private val playPauseButton: View?
    private val fastForwardButton: View?
    private val rewindButton: View?
    private val fastForwardButtonTextView: TextView?
    private val rewindButtonTextView: TextView?
    private val repeatToggleButton: ImageView?
    private val shuffleButton: ImageView?
    private val vrButton: View?
    private val subtitleButton: ImageView?
    private val fullScreenButton: ImageView?
    private val minimalFullScreenButton: ImageView?
    private val settingsButton: View?
    private val playbackSpeedButton: View?
    private val audioTrackButton: View?
    private val durationView: TextView?
    private val positionView: TextView?
    private var timeBar: TimeBar? = null
    private val formatBuilder: StringBuilder
    private val formatter: Formatter
    private val period: Timeline.Period
    private val window: Timeline.Window
    private val updateProgressAction: Runnable
    private val repeatOffButtonDrawable: Drawable
    private val repeatOneButtonDrawable: Drawable
    private val repeatAllButtonDrawable: Drawable
    private val repeatOffButtonContentDescription: String
    private val repeatOneButtonContentDescription: String
    private val repeatAllButtonContentDescription: String
    private val shuffleOnButtonDrawable: Drawable
    private val shuffleOffButtonDrawable: Drawable
    private val buttonAlphaEnabled: Float
    private val buttonAlphaDisabled: Float
    private val shuffleOnContentDescription: String
    private val shuffleOffContentDescription: String
    private val subtitleOnButtonDrawable: Drawable
    private val subtitleOffButtonDrawable: Drawable
    private val subtitleOnContentDescription: String
    private val subtitleOffContentDescription: String
    private val fullScreenExitDrawable: Drawable
    private val fullScreenEnterDrawable: Drawable
    private val fullScreenExitContentDescription: String
    private val fullScreenEnterContentDescription: String
    private var player: Player? = null
    private var progressUpdateListener: ProgressUpdateListener? = null
    private var onFullScreenModeChangedListener: OnFullScreenModeChangedListener? = null
    private var isFullScreen = false
    private var isAttachedToWindow = false
    private var showMultiWindowTimeBar = false
    private var multiWindowTimeBar = false
    private var scrubbing = false
    private var showTimeoutMs: Int
    private var timeBarMinUpdateIntervalMs: Int
    private var repeatToggleModes: @RepeatToggleModes Int
    private var adGroupTimesMs: LongArray
    private var playedAdGroups: BooleanArray
    private var extraAdGroupTimesMs: LongArray
    private var extraPlayedAdGroups: BooleanArray
    private var currentWindowOffset: Long = 0
    private var needToHideBars: Boolean
    var customFromKotlinControllerLayout: View? = null

    init {
        var controllerLayoutId = R.layout.exo_player_control_view
        showTimeoutMs = DEFAULT_SHOW_TIMEOUT_MS
        repeatToggleModes = DEFAULT_REPEAT_TOGGLE_MODES
        timeBarMinUpdateIntervalMs = DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS
        var showRewindButton = true
        var showFastForwardButton = true
        var showPreviousButton = true
        var showNextButton = true
        var showShuffleButton = false
        var showSubtitleButton = false
        var animationEnabled = true
        var showVrButton = false
        if (playbackAttrs != null) {
            val a = context.theme.obtainStyledAttributes(
                playbackAttrs,
                R.styleable.PlayerControlView,
                defStyleAttr,  /* defStyleRes= */
                0
            )
            try {
                controllerLayoutId = a.getResourceId(
                    R.styleable.PlayerControlView_controller_layout_id,
                    controllerLayoutId
                )
                showTimeoutMs = a.getInt(R.styleable.PlayerControlView_show_timeout, showTimeoutMs)
                repeatToggleModes = getRepeatToggleModes(a, repeatToggleModes)
                showRewindButton =
                    a.getBoolean(R.styleable.PlayerControlView_show_rewind_button, showRewindButton)
                showFastForwardButton = a.getBoolean(
                    R.styleable.PlayerControlView_show_fastforward_button,
                    showFastForwardButton
                )
                showPreviousButton = a.getBoolean(
                    R.styleable.PlayerControlView_show_previous_button,
                    showPreviousButton
                )
                showNextButton =
                    a.getBoolean(R.styleable.PlayerControlView_show_next_button, showNextButton)
                showShuffleButton = a.getBoolean(
                    R.styleable.PlayerControlView_show_shuffle_button,
                    showShuffleButton
                )
                showSubtitleButton = a.getBoolean(
                    R.styleable.PlayerControlView_show_subtitle_button,
                    showSubtitleButton
                )
                showVrButton =
                    a.getBoolean(R.styleable.PlayerControlView_show_vr_button, showVrButton)
                setTimeBarMinUpdateInterval(
                    a.getInt(
                        R.styleable.PlayerControlView_time_bar_min_update_interval,
                        timeBarMinUpdateIntervalMs
                    )
                )
                animationEnabled =
                    a.getBoolean(R.styleable.PlayerControlView_animation_enabled, animationEnabled)
            } finally {
                a.recycle()
            }
        }
        LayoutInflater.from(context).inflate(controllerLayoutId,  /* root= */this)
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        componentListener = ComponentListener()
        visibilityListeners = CopyOnWriteArrayList()
        period = Timeline.Period()
        window = Timeline.Window()
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        adGroupTimesMs = LongArray(0)
        playedAdGroups = BooleanArray(0)
        extraAdGroupTimesMs = LongArray(0)
        extraPlayedAdGroups = BooleanArray(0)
        updateProgressAction = Runnable { updateProgress() }
        durationView = findViewById(R.id.exo_duration)
        positionView = findViewById(R.id.exo_position)
        subtitleButton = findViewById(R.id.exo_subtitle)
        subtitleButton?.setOnClickListener(componentListener)
        fullScreenButton = findViewById(R.id.exo_fullscreen)
        initializeFullScreenButton(fullScreenButton) { v: View -> onFullScreenButtonClicked(v) }
        minimalFullScreenButton = findViewById(R.id.exo_minimal_fullscreen)
        initializeFullScreenButton(minimalFullScreenButton) { v: View -> onFullScreenButtonClicked(v) }
        settingsButton = findViewById(R.id.exo_settings)
        settingsButton?.setOnClickListener(componentListener)
        playbackSpeedButton = findViewById(R.id.exo_playback_speed)
        playbackSpeedButton?.setOnClickListener(componentListener)
        audioTrackButton = findViewById(R.id.exo_audio_track)
        audioTrackButton?.setOnClickListener(componentListener)
        val customTimeBar: TimeBar? = findViewById(R.id.exo_progress)
        val timeBarPlaceholder = findViewById<View>(R.id.exo_progress_placeholder)
        if (customTimeBar != null) {
            timeBar = customTimeBar
        } else if (timeBarPlaceholder != null) {
            // Propagate playbackAttrs as timebarAttrs so that DefaultTimeBar's custom attributes are
            // transferred, but standard attributes (e.g. background) are not.
            val defaultTimeBar =
                DefaultTimeBar(context, null, 0, playbackAttrs, R.style.ExoStyledControls_TimeBar)
            defaultTimeBar.id = R.id.exo_progress
            defaultTimeBar.layoutParams = timeBarPlaceholder.layoutParams
            val parent = timeBarPlaceholder.parent as ViewGroup
            val timeBarIndex = parent.indexOfChild(timeBarPlaceholder)
            parent.removeView(timeBarPlaceholder)
            parent.addView(defaultTimeBar, timeBarIndex)
            timeBar = defaultTimeBar
        } else {
            timeBar = null
        }
        if (timeBar != null) {
            timeBar?.addListener(componentListener)
        }
        playPauseButton = findViewById(R.id.exo_play_pause)
        playPauseButton?.setOnClickListener(componentListener)
        previousButton = findViewById(R.id.exo_prev)
        previousButton?.setOnClickListener(componentListener)
        nextButton = findViewById(R.id.exo_next)
        nextButton?.setOnClickListener(componentListener)
        val typeface = ResourcesCompat.getFont(context, R.font.roboto_medium_numbers)
        val rewButton = findViewById<View>(R.id.exo_rew)
        rewindButtonTextView =
            if (rewButton == null) findViewById(R.id.exo_rew_with_amount) else null
        rewindButtonTextView?.typeface = typeface
        rewindButton = rewButton ?: rewindButtonTextView
        rewindButton?.setOnClickListener(componentListener)
        val ffwdButton = findViewById<View>(R.id.exo_ffwd)
        fastForwardButtonTextView =
            if (ffwdButton == null) findViewById(R.id.exo_ffwd_with_amount) else null
        fastForwardButtonTextView?.typeface = typeface
        fastForwardButton = ffwdButton ?: fastForwardButtonTextView
        fastForwardButton?.setOnClickListener(componentListener)
        repeatToggleButton = findViewById(R.id.exo_repeat_toggle)
        repeatToggleButton?.setOnClickListener(componentListener)
        shuffleButton = findViewById(R.id.exo_shuffle)
        shuffleButton?.setOnClickListener(componentListener)
        resources = context.resources
        buttonAlphaEnabled =
            resources.getInteger(R.integer.exo_media_button_opacity_percentage_enabled)
                .toFloat() / 100
        buttonAlphaDisabled =
            resources.getInteger(R.integer.exo_media_button_opacity_percentage_disabled)
                .toFloat() / 100
        vrButton = findViewById(R.id.exo_vr)
        if (vrButton != null) {
            updateButton( /* enabled= */false, vrButton)
        }
        controlViewLayoutManager = PlayerControlViewLayoutManager(this)
        controlViewLayoutManager.isAnimationEnabled = animationEnabled
        val settingTexts = arrayOfNulls<String>(2)
        val settingIcons = arrayOfNulls<Drawable>(2)
        settingTexts[SETTINGS_PLAYBACK_SPEED_POSITION] =
            resources.getString(R.string.exo_controls_playback_speed)
        settingIcons[SETTINGS_PLAYBACK_SPEED_POSITION] =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_speed)
        settingTexts[SETTINGS_AUDIO_TRACK_SELECTION_POSITION] =
            resources.getString(R.string.exo_track_selection_title_audio)
        settingIcons[SETTINGS_AUDIO_TRACK_SELECTION_POSITION] =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_audiotrack)
        settingsAdapter = SettingsAdapter(settingTexts, settingIcons)
        settingsWindowMargin = resources.getDimensionPixelSize(R.dimen.exo_settings_offset)
        settingsView = LayoutInflater.from(context)
            .inflate(R.layout.exo_styled_settings_list,  /* root= */null) as RecyclerView
        settingsView.adapter = settingsAdapter
        settingsView.layoutManager = LinearLayoutManager(getContext())
        settingsWindow =
            PopupWindow(settingsView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true)
        if (Util.SDK_INT < 23) {
            // Work around issue where tapping outside of the menu area or pressing the back button
            // doesn't dismiss the menu as expected. See: https://github.com/google/ExoPlayer/issues/8272.
            settingsWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        settingsWindow.setOnDismissListener(componentListener)
        needToHideBars = true
        trackNameProvider = DefaultTrackNameProvider(getResources())
        subtitleOnButtonDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_subtitle_on)
        subtitleOffButtonDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_subtitle_off)
        subtitleOnContentDescription =
            resources.getString(R.string.exo_controls_cc_enabled_description)
        subtitleOffContentDescription =
            resources.getString(R.string.exo_controls_cc_disabled_description)
        textTrackSelectionAdapter = TextTrackSelectionAdapter()
        audioTrackSelectionAdapter = AudioTrackSelectionAdapter()
        playbackSpeedAdapter = PlaybackSpeedAdapter(
            resources.getStringArray(R.array.exo_controls_playback_speeds),
            PLAYBACK_SPEEDS
        )
        fullScreenExitDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_fullscreen_exit)
        fullScreenEnterDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_fullscreen_enter)
        repeatOffButtonDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_repeat_off)
        repeatOneButtonDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_repeat_one)
        repeatAllButtonDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_repeat_all)
        shuffleOnButtonDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_shuffle_on)
        shuffleOffButtonDrawable =
            Util.getDrawable(context, resources, R.drawable.exo_styled_controls_shuffle_off)
        fullScreenExitContentDescription =
            resources.getString(R.string.exo_controls_fullscreen_exit_description)
        fullScreenEnterContentDescription =
            resources.getString(R.string.exo_controls_fullscreen_enter_description)
        repeatOffButtonContentDescription =
            resources.getString(R.string.exo_controls_repeat_off_description)
        repeatOneButtonContentDescription =
            resources.getString(R.string.exo_controls_repeat_one_description)
        repeatAllButtonContentDescription =
            resources.getString(R.string.exo_controls_repeat_all_description)
        shuffleOnContentDescription =
            resources.getString(R.string.exo_controls_shuffle_on_description)
        shuffleOffContentDescription =
            resources.getString(R.string.exo_controls_shuffle_off_description)

        // TODO(insun) : Make showing bottomBar configurable. (ex. show_bottom_bar attribute).
        val bottomBar = findViewById<ViewGroup>(R.id.exo_bottom_bar)
        controlViewLayoutManager.setShowButton(bottomBar, true)
        controlViewLayoutManager.setShowButton(fastForwardButton, showFastForwardButton)
        controlViewLayoutManager.setShowButton(rewindButton, showRewindButton)
        controlViewLayoutManager.setShowButton(previousButton, showPreviousButton)
        controlViewLayoutManager.setShowButton(nextButton, showNextButton)
        controlViewLayoutManager.setShowButton(shuffleButton, showShuffleButton)
        controlViewLayoutManager.setShowButton(subtitleButton, showSubtitleButton)
        controlViewLayoutManager.setShowButton(vrButton, showVrButton)
        controlViewLayoutManager.setShowButton(
            repeatToggleButton,
            repeatToggleModes != RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
        )
        addOnLayoutChangeListener { v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            onLayoutChange(
                v,
                left,
                top,
                right,
                bottom,
                oldLeft,
                oldTop,
                oldRight,
                oldBottom
            )
        }
    }

    /**
     * Returns the [Player] currently being controlled by this view, or null if no player is
     * set.
     */
    fun getPlayer(): Player? {
        return player
    }

    /**
     * Sets the [Player] to control.
     *
     * @param player The [Player] to control, or `null` to detach the current player. Only
     * players which are accessed on the main thread are supported (`player.getApplicationLooper() == Looper.getMainLooper()`).
     */
    fun setPlayer(player: Player?) {
        Assertions.checkState(Looper.myLooper() == Looper.getMainLooper())
        Assertions.checkArgument(player == null || player.applicationLooper == Looper.getMainLooper())
        if (this.player === player) {
            return
        }
        if (this.player != null) {
            this.player!!.removeListener(componentListener)
        }
        this.player = player
        player?.addListener(componentListener)
        updateAll()
    }

    /**
     * Sets whether the time bar should show all windows, as opposed to just the current one. If the
     * timeline has a period with unknown duration or more than [ ][.MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR] windows the time bar will fall back to showing a single
     * window.
     *
     * @param showMultiWindowTimeBar Whether the time bar should show all windows.
     */
    fun setShowMultiWindowTimeBar(showMultiWindowTimeBar: Boolean) {
        this.showMultiWindowTimeBar = showMultiWindowTimeBar
        updateTimeline()
    }

    /**
     * Sets the millisecond positions of extra ad markers relative to the start of the window (or
     * timeline, if in multi-window mode) and whether each extra ad has been played or not. The
     * markers are shown in addition to any ad markers for ads in the player's timeline.
     *
     * @param extraAdGroupTimesMs The millisecond timestamps of the extra ad markers to show, or
     * `null` to show no extra ad markers.
     * @param extraPlayedAdGroups Whether each ad has been played. Must be the same length as `extraAdGroupTimesMs`, or `null` if `extraAdGroupTimesMs` is `null`.
     */
    fun setExtraAdGroupMarkers(
        extraAdGroupTimesMs: LongArray?,
        extraPlayedAdGroups: BooleanArray?
    ) {
        var extraPlayedAdGroups = extraPlayedAdGroups
        if (extraAdGroupTimesMs == null) {
            this.extraAdGroupTimesMs = LongArray(0)
            this.extraPlayedAdGroups = BooleanArray(0)
        } else {
            extraPlayedAdGroups = Assertions.checkNotNull(extraPlayedAdGroups)
            Assertions.checkArgument(extraAdGroupTimesMs.size == extraPlayedAdGroups.size)
            this.extraAdGroupTimesMs = extraAdGroupTimesMs
            this.extraPlayedAdGroups = extraPlayedAdGroups
        }
        updateTimeline()
    }

    @Deprecated(
        """Register a {@link PlayerView.ControllerVisibilityListener} via {@link
     * PlayerView#setControllerVisibilityListener(PlayerView.ControllerVisibilityListener)}
      instead. Using {@link PlayerControlView} as a standalone class without {@link PlayerView}
      is deprecated."""
    )
    fun addVisibilityListener(listener: VisibilityListener) {
        Assertions.checkNotNull(listener)
        visibilityListeners.add(listener)
    }

    @Deprecated(
        """Register a {@link PlayerView.ControllerVisibilityListener} via {@link
     * PlayerView#setControllerVisibilityListener(PlayerView.ControllerVisibilityListener)}
      instead. Using {@link PlayerControlView} as a standalone class without {@link PlayerView}
      is deprecated."""
    )
    fun removeVisibilityListener(listener: VisibilityListener) {
        visibilityListeners.remove(listener)
    }

    /**
     * Sets the [PlayerControlView.ProgressUpdateListener].
     *
     * @param listener The listener to be notified about when progress is updated.
     */
    fun setProgressUpdateListener(listener: ProgressUpdateListener?) {
        progressUpdateListener = listener
    }

    /**
     * Sets whether the rewind button is shown.
     *
     * @param showRewindButton Whether the rewind button is shown.
     */
    fun setShowRewindButton(showRewindButton: Boolean) {
        controlViewLayoutManager.setShowButton(rewindButton, showRewindButton)
        updateNavigation()
    }

    /**
     * Sets whether the fast forward button is shown.
     *
     * @param showFastForwardButton Whether the fast forward button is shown.
     */
    fun setShowFastForwardButton(showFastForwardButton: Boolean) {
        controlViewLayoutManager.setShowButton(fastForwardButton, showFastForwardButton)
        updateNavigation()
    }

    /**
     * Sets whether the previous button is shown.
     *
     * @param showPreviousButton Whether the previous button is shown.
     */
    fun setShowPreviousButton(showPreviousButton: Boolean) {
        controlViewLayoutManager.setShowButton(previousButton, showPreviousButton)
        updateNavigation()
    }

    /**
     * Sets whether the next button is shown.
     *
     * @param showNextButton Whether the next button is shown.
     */
    fun setShowNextButton(showNextButton: Boolean) {
        controlViewLayoutManager.setShowButton(nextButton, showNextButton)
        updateNavigation()
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input.
     *
     * @return The duration in milliseconds. A non-positive value indicates that the controls will
     * remain visible indefinitely.
     */
    fun getShowTimeoutMs(): Int {
        return showTimeoutMs
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input.
     *
     * @param showTimeoutMs The duration in milliseconds. A non-positive value will cause the controls
     * to remain visible indefinitely.
     */
    fun setShowTimeoutMs(showTimeoutMs: Int) {
        this.showTimeoutMs = showTimeoutMs
        if (isFullyVisible) {
            controlViewLayoutManager.resetHideCallbacks()
        }
    }

    /**
     * Returns which repeat toggle modes are enabled.
     *
     * @return The currently enabled [RepeatModeUtil.RepeatToggleModes].
     */
    fun getRepeatToggleModes(): @RepeatToggleModes Int {
        return repeatToggleModes
    }

    /**
     * Sets which repeat toggle modes are enabled.
     *
     * @param repeatToggleModes A set of [RepeatModeUtil.RepeatToggleModes].
     */
    fun setRepeatToggleModes(repeatToggleModes: @RepeatToggleModes Int) {
        this.repeatToggleModes = repeatToggleModes
        if (player != null && player!!.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
            val currentMode: @Player.RepeatMode Int = player!!.repeatMode
            if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE && currentMode != Player.REPEAT_MODE_OFF) {
                player!!.repeatMode = Player.REPEAT_MODE_OFF
            } else if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE && currentMode == Player.REPEAT_MODE_ALL) {
                player!!.repeatMode = Player.REPEAT_MODE_ONE
            } else if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL && currentMode == Player.REPEAT_MODE_ONE) {
                player!!.repeatMode = Player.REPEAT_MODE_ALL
            }
        }
        controlViewLayoutManager.setShowButton(
            repeatToggleButton,
            repeatToggleModes != RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE
        )
        updateRepeatModeButton()
    }
    /**
     * Returns whether the shuffle button is shown.
     */
    /**
     * Sets whether the shuffle button is shown.
     *
     * @param showShuffleButton Whether the shuffle button is shown.
     */
    var showShuffleButton: Boolean
        get() = controlViewLayoutManager.getShowButton(shuffleButton)
        set(showShuffleButton) {
            controlViewLayoutManager.setShowButton(shuffleButton, showShuffleButton)
            updateShuffleButton()
        }
    /**
     * Returns whether the subtitle button is shown.
     */
    /**
     * Sets whether the subtitle button is shown.
     *
     * @param showSubtitleButton Whether the subtitle button is shown.
     */
    var showSubtitleButton: Boolean
        get() = controlViewLayoutManager.getShowButton(subtitleButton)
        set(showSubtitleButton) {
            controlViewLayoutManager.setShowButton(subtitleButton, showSubtitleButton)
        }
    /**
     * Returns whether the VR button is shown.
     */
    /**
     * Sets whether the VR button is shown.
     *
     * @param showVrButton Whether the VR button is shown.
     */
    var showVrButton: Boolean
        get() = controlViewLayoutManager.getShowButton(vrButton)
        set(showVrButton) {
            controlViewLayoutManager.setShowButton(vrButton, showVrButton)
        }

    /**
     * Sets listener for the VR button.
     *
     * @param onClickListener Listener for the VR button, or null to clear the listener.
     */
    fun setVrButtonListener(onClickListener: OnClickListener?) {
        if (vrButton != null) {
            vrButton.setOnClickListener(onClickListener)
            updateButton(onClickListener != null, vrButton)
        }
    }
    /**
     * Returns whether an animation is used to show and hide the playback controls.
     */
    /**
     * Sets whether an animation is used to show and hide the playback controls.
     *
     * @param animationEnabled Whether an animation is applied to show and hide playback controls.
     */
    var isAnimationEnabled: Boolean
        get() = controlViewLayoutManager.isAnimationEnabled
        set(animationEnabled) {
            controlViewLayoutManager.isAnimationEnabled = animationEnabled
        }

    /**
     * Sets the minimum interval between time bar position updates.
     *
     *
     * Note that smaller intervals, e.g. 33ms, will result in a smooth movement but will use more
     * CPU resources while the time bar is visible, whereas larger intervals, e.g. 200ms, will result
     * in a step-wise update with less CPU usage.
     *
     * @param minUpdateIntervalMs The minimum interval between time bar position updates, in
     * milliseconds.
     */
    fun setTimeBarMinUpdateInterval(minUpdateIntervalMs: Int) {
        // Do not accept values below 16ms (60fps) and larger than the maximum update interval.
        timeBarMinUpdateIntervalMs =
            Util.constrainValue(minUpdateIntervalMs, 16, MAX_UPDATE_INTERVAL_MS)
    }

    @Deprecated(
        """Register a {@link PlayerView.FullscreenButtonClickListener} via {@link
     * PlayerView#setFullscreenButtonClickListener(PlayerView.FullscreenButtonClickListener)}
      instead. Using {@link PlayerControlView} as a standalone class without {@link PlayerView}
      is deprecated."""
    )
    fun setOnFullScreenModeChangedListener(listener: OnFullScreenModeChangedListener?) {
        onFullScreenModeChangedListener = listener
        updateFullScreenButtonVisibility(fullScreenButton, listener != null)
        updateFullScreenButtonVisibility(minimalFullScreenButton, listener != null)
    }

    /**
     * Shows the playback controls. If [.getShowTimeoutMs] is positive then the controls will
     * be automatically hidden after this duration of time has elapsed without user input.
     */
    fun show() {
        controlViewLayoutManager.show()
    }

    /**
     * Hides the controller.
     */
    fun hide() {
        controlViewLayoutManager.hide()
    }

    /**
     * Hides the controller without any animation.
     */
    fun hideImmediately() {
        controlViewLayoutManager.hideImmediately()
    }

    /**
     * Returns whether the controller is fully visible, which means all UI controls are visible.
     */
    val isFullyVisible: Boolean
        get() = controlViewLayoutManager.isFullyVisible

    /**
     * Returns whether the controller is currently visible.
     */
    val isVisible: Boolean
        get() = visibility == VISIBLE

    fun  // Calling the deprecated listener for now.
            /* package */notifyOnVisibilityChange() {
        for (visibilityListener in visibilityListeners) {
            visibilityListener.onVisibilityChange(visibility)
        }
    }

    /* package */
    fun updateAll() {
        updatePlayPauseButton()
        updateNavigation()
        updateRepeatModeButton()
        updateShuffleButton()
        updateTrackLists()
        updatePlaybackSpeedList()
        updateTimeline()
    }

    private fun updatePlayPauseButton() {
        if (!isVisible || !isAttachedToWindow) {
            return
        }
        if (playPauseButton != null) {
            val shouldShowPauseButton = shouldShowPauseButton()
            @DrawableRes val drawableRes =
                if (shouldShowPauseButton) R.drawable.exo_styled_controls_pause else R.drawable.exo_styled_controls_play
            @StringRes val stringRes =
                if (shouldShowPauseButton) R.string.exo_controls_pause_description else R.string.exo_controls_play_description
            (playPauseButton as ImageView).setImageDrawable(
                Util.getDrawable(
                    context, resources, drawableRes
                )
            )
            playPauseButton.setContentDescription(resources.getString(stringRes))
            val enablePlayPause = shouldEnablePlayPauseButton()
            updateButton(enablePlayPause, playPauseButton)
        }
    }

    private fun updateNavigation() {
        if (!isVisible || !isAttachedToWindow) {
            return
        }
        val player = player
        var enableSeeking = false
        var enablePrevious = false
        var enableRewind = false
        var enableFastForward = false
        var enableNext = false
        if (player != null) {
            enableSeeking = if (showMultiWindowTimeBar && canShowMultiWindowTimeBar(
                    player,
                    window
                )
            ) player.isCommandAvailable(Player.COMMAND_SEEK_TO_MEDIA_ITEM) else player.isCommandAvailable(
                Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM
            )
            enablePrevious = player.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS)
            enableRewind = player.isCommandAvailable(Player.COMMAND_SEEK_BACK)
            enableFastForward = player.isCommandAvailable(Player.COMMAND_SEEK_FORWARD)
            enableNext = player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)
        }
        if (enableRewind) {
            updateRewindButton()
        }
        if (enableFastForward) {
            updateFastForwardButton()
        }
        updateButton(enablePrevious, previousButton)
        updateButton(enableRewind, rewindButton)
        updateButton(enableFastForward, fastForwardButton)
        updateButton(enableNext, nextButton)
        if (timeBar != null) {
            timeBar?.setEnabled(enableSeeking)
        }
    }

    private fun updateRewindButton() {
        val rewindMs =
            if (player != null) player!!.seekBackIncrement else C.DEFAULT_SEEK_BACK_INCREMENT_MS
        val rewindSec = (rewindMs / 1000).toInt()
        if (rewindButtonTextView != null) {
            rewindButtonTextView.text = rewindSec.toString()
        }
        if (rewindButton != null) {
            rewindButton.contentDescription = resources.getQuantityString(
                R.plurals.exo_controls_rewind_by_amount_description,
                rewindSec,
                rewindSec
            )
        }
    }

    private fun updateFastForwardButton() {
        val fastForwardMs =
            if (player != null) player!!.seekForwardIncrement else C.DEFAULT_SEEK_FORWARD_INCREMENT_MS
        val fastForwardSec = (fastForwardMs / 1000).toInt()
        if (fastForwardButtonTextView != null) {
            fastForwardButtonTextView.text = fastForwardSec.toString()
        }
        if (fastForwardButton != null) {
            fastForwardButton.contentDescription = resources.getQuantityString(
                R.plurals.exo_controls_fastforward_by_amount_description,
                fastForwardSec,
                fastForwardSec
            )
        }
    }

    private fun updateRepeatModeButton() {
        if (!isVisible || !isAttachedToWindow || repeatToggleButton == null) {
            return
        }
        if (repeatToggleModes == RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE) {
            updateButton( /* enabled= */false, repeatToggleButton)
            return
        }
        val player = player
        if (player == null || !player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
            updateButton( /* enabled= */false, repeatToggleButton)
            repeatToggleButton.setImageDrawable(repeatOffButtonDrawable)
            repeatToggleButton.contentDescription = repeatOffButtonContentDescription
            return
        }
        updateButton( /* enabled= */true, repeatToggleButton)
        when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> {
                repeatToggleButton.setImageDrawable(repeatOffButtonDrawable)
                repeatToggleButton.contentDescription = repeatOffButtonContentDescription
            }

            Player.REPEAT_MODE_ONE -> {
                repeatToggleButton.setImageDrawable(repeatOneButtonDrawable)
                repeatToggleButton.contentDescription = repeatOneButtonContentDescription
            }

            Player.REPEAT_MODE_ALL -> {
                repeatToggleButton.setImageDrawable(repeatAllButtonDrawable)
                repeatToggleButton.contentDescription = repeatAllButtonContentDescription
            }

            else -> {}
        }
    }

    private fun updateShuffleButton() {
        if (!isVisible || !isAttachedToWindow || shuffleButton == null) {
            return
        }
        val player = player
        if (!controlViewLayoutManager.getShowButton(shuffleButton)) {
            updateButton( /* enabled= */false, shuffleButton)
        } else if (player == null || !player.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE)) {
            updateButton( /* enabled= */false, shuffleButton)
            shuffleButton.setImageDrawable(shuffleOffButtonDrawable)
            shuffleButton.contentDescription = shuffleOffContentDescription
        } else {
            updateButton( /* enabled= */true, shuffleButton)
            shuffleButton.setImageDrawable(if (player.shuffleModeEnabled) shuffleOnButtonDrawable else shuffleOffButtonDrawable)
            shuffleButton.contentDescription =
                if (player.shuffleModeEnabled) shuffleOnContentDescription else shuffleOffContentDescription
        }
    }

    private fun updateTrackLists() {
        initTrackSelectionAdapter()
        updateButton(textTrackSelectionAdapter.itemCount > 0, subtitleButton)
        updateSettingsButton()
    }

    private fun initTrackSelectionAdapter() {
        textTrackSelectionAdapter.clear()
        audioTrackSelectionAdapter.clear()
        if (player == null || !player!!.isCommandAvailable(Player.COMMAND_GET_TRACKS) || !player!!.isCommandAvailable(
                Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS
            )
        ) {
            return
        }
        val tracks = player!!.currentTracks
        audioTrackSelectionAdapter.init(gatherSupportedTrackInfosOfType(tracks, C.TRACK_TYPE_AUDIO))
        if (controlViewLayoutManager.getShowButton(subtitleButton)) {
            textTrackSelectionAdapter.init(
                gatherSupportedTrackInfosOfType(
                    tracks,
                    C.TRACK_TYPE_TEXT
                )
            )
        } else {
            textTrackSelectionAdapter.init(ImmutableList.of())
        }
    }

    private fun gatherSupportedTrackInfosOfType(
        tracks: Tracks,
        trackType: @TrackType Int
    ): ImmutableList<TrackInformation> {
        val trackInfos = ImmutableList.Builder<TrackInformation>()
        val trackGroups: List<Tracks.Group> = tracks.groups
        for (trackGroupIndex in trackGroups.indices) {
            val trackGroup = trackGroups[trackGroupIndex]
            if (trackGroup.type != trackType) {
                continue
            }
            for (trackIndex in 0 until trackGroup.length) {
                if (!trackGroup.isTrackSupported(trackIndex)) {
                    continue
                }
                val trackFormat = trackGroup.getTrackFormat(trackIndex)
                if (trackFormat.selectionFlags and C.SELECTION_FLAG_FORCED != 0) {
                    continue
                }
                val trackName = trackNameProvider.getTrackName(trackFormat)
                trackInfos.add(TrackInformation(tracks, trackGroupIndex, trackIndex, trackName))
            }
        }
        return trackInfos.build()
    }

    private fun updateTimeline() {
        val player = player ?: return
        multiWindowTimeBar = showMultiWindowTimeBar && canShowMultiWindowTimeBar(player, window)
        currentWindowOffset = 0
        var durationUs: Long = 0
        var adGroupCount = 0
        val timeline =
            if (player.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) player.currentTimeline else Timeline.EMPTY
        if (!timeline.isEmpty) {
            val currentWindowIndex = player.currentMediaItemIndex
            val firstWindowIndex = if (multiWindowTimeBar) 0 else currentWindowIndex
            val lastWindowIndex =
                if (multiWindowTimeBar) timeline.windowCount - 1 else currentWindowIndex
            for (i in firstWindowIndex..lastWindowIndex) {
                if (i == currentWindowIndex) {
                    currentWindowOffset = Util.usToMs(durationUs)
                }
                timeline.getWindow(i, window)
                if (window.durationUs == C.TIME_UNSET) {
                    Assertions.checkState(!multiWindowTimeBar)
                    break
                }
                for (j in window.firstPeriodIndex..window.lastPeriodIndex) {
                    timeline.getPeriod(j, period)
                    val removedGroups = period.removedAdGroupCount
                    val totalGroups = period.adGroupCount
                    for (adGroupIndex in removedGroups until totalGroups) {
                        var adGroupTimeInPeriodUs = period.getAdGroupTimeUs(adGroupIndex)
                        if (adGroupTimeInPeriodUs == C.TIME_END_OF_SOURCE) {
                            if (period.durationUs == C.TIME_UNSET) {
                                // Don't show ad markers for postrolls in periods with unknown duration.
                                continue
                            }
                            adGroupTimeInPeriodUs = period.durationUs
                        }
                        val adGroupTimeInWindowUs =
                            adGroupTimeInPeriodUs + period.getPositionInWindowUs()
                        if (adGroupTimeInWindowUs >= 0) {
                            if (adGroupCount == adGroupTimesMs.size) {
                                val newLength =
                                    if (adGroupTimesMs.isEmpty()) 1 else adGroupTimesMs.size * 2
                                adGroupTimesMs = adGroupTimesMs.copyOf(newLength)
                                playedAdGroups = playedAdGroups.copyOf(newLength)
                            }
                            adGroupTimesMs[adGroupCount] =
                                Util.usToMs(durationUs + adGroupTimeInWindowUs)
                            playedAdGroups[adGroupCount] = period.hasPlayedAdGroup(adGroupIndex)
                            adGroupCount++
                        }
                    }
                }
                durationUs += window.durationUs
            }
        } else if (player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            val playerDurationMs = player.contentDuration
            if (playerDurationMs != C.TIME_UNSET) {
                durationUs = Util.msToUs(playerDurationMs)
            }
        }
        val durationMs = Util.usToMs(durationUs)
        if (durationView != null) {
            durationView.text =
                Util.getStringForTime(formatBuilder, formatter, durationMs)
        }
        if (timeBar != null) {
            timeBar?.setDuration(durationMs)
            val extraAdGroupCount = extraAdGroupTimesMs.size
            val totalAdGroupCount = adGroupCount + extraAdGroupCount
            if (totalAdGroupCount > adGroupTimesMs.size) {
                adGroupTimesMs = adGroupTimesMs.copyOf(totalAdGroupCount)
                playedAdGroups = playedAdGroups.copyOf(totalAdGroupCount)
            }
            System.arraycopy(
                extraAdGroupTimesMs,
                0,
                adGroupTimesMs,
                adGroupCount,
                extraAdGroupCount
            )
            System.arraycopy(
                extraPlayedAdGroups,
                0,
                playedAdGroups,
                adGroupCount,
                extraAdGroupCount
            )
            timeBar?.setAdGroupTimesMs(adGroupTimesMs, playedAdGroups, totalAdGroupCount)
        }
        updateProgress()
    }

    private fun updateProgress() {
        if (!isVisible || !isAttachedToWindow) {
            return
        }
        val player = player
        var position: Long = 0
        var bufferedPosition: Long = 0
        if (player != null && player.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM)) {
            position = currentWindowOffset + player.contentPosition
            bufferedPosition = currentWindowOffset + player.contentBufferedPosition
        }
        if (positionView != null && !scrubbing) {
            positionView.text =
                Util.getStringForTime(formatBuilder, formatter, position)
        }
        if (timeBar != null) {
            timeBar?.setPosition(position)
            timeBar?.setBufferedPosition(bufferedPosition)
        }
        if (progressUpdateListener != null) {
            progressUpdateListener!!.onProgressUpdate(position, bufferedPosition)
        }

        // Cancel any pending updates and schedule a new one if necessary.
        removeCallbacks(updateProgressAction)
        val playbackState = player?.playbackState ?: Player.STATE_IDLE
        if (player != null && player.isPlaying) {
            var mediaTimeDelayMs =
                if (timeBar != null) timeBar?.preferredUpdateDelay else MAX_UPDATE_INTERVAL_MS.toLong()

            // Limit delay to the start of the next full second to ensure position display is smooth.
            val mediaTimeUntilNextFullSecondMs = 1000 - position % 1000
            mediaTimeDelayMs = mediaTimeDelayMs?.let { min(it, mediaTimeUntilNextFullSecondMs) }

            // Calculate the delay until the next update in real time, taking playback speed into account.
            val playbackSpeed = player.playbackParameters.speed
            var delayMs =
                if (playbackSpeed > 0) (mediaTimeDelayMs?.div(playbackSpeed))?.toLong() else MAX_UPDATE_INTERVAL_MS.toLong()

            // Constrain the delay to avoid too frequent / infrequent updates.
            delayMs = delayMs?.let {
                Util.constrainValue(
                    it,
                    timeBarMinUpdateIntervalMs.toLong(),
                    MAX_UPDATE_INTERVAL_MS.toLong()
                )
            }
            if (delayMs != null) {
                postDelayed(updateProgressAction, delayMs)
            }
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS.toLong())
        }
    }

    private fun updatePlaybackSpeedList() {
        if (player == null) {
            return
        }
        playbackSpeedAdapter.updateSelectedIndex(player!!.playbackParameters.speed)
        settingsAdapter.setSubTextAtPosition(
            SETTINGS_PLAYBACK_SPEED_POSITION,
            playbackSpeedAdapter.selectedText
        )
        updateSettingsButton()
    }

    private fun updateSettingsButton() {
        updateButton(settingsAdapter.hasSettingsToShow(), settingsButton)
    }

    private fun updateSettingsWindowSize() {
        settingsView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val maxWidth = width - settingsWindowMargin * 2
        val itemWidth = settingsView.measuredWidth
        val width = min(itemWidth, maxWidth)
        settingsWindow.width = width
        val maxHeight = height - settingsWindowMargin * 2
        val totalHeight = settingsView.measuredHeight
        val height = min(maxHeight, totalHeight)
        settingsWindow.height = height
    }

    private fun displaySettingsWindow(adapter: RecyclerView.Adapter<*>, anchorView: View?) {
        settingsView.adapter = adapter
        updateSettingsWindowSize()
        needToHideBars = false
        settingsWindow.dismiss()
        needToHideBars = true
        val xoff = width - settingsWindow.width - settingsWindowMargin
        val yoff = -settingsWindow.height - settingsWindowMargin
        settingsWindow.showAsDropDown(anchorView, xoff, yoff)
    }

    private fun setPlaybackSpeed(speed: Float) {
        if (player == null || !player!!.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH)) {
            return
        }
        player!!.playbackParameters = player!!.playbackParameters.withSpeed(speed)
    }

    /* package */
    fun requestPlayPauseFocus() {
        playPauseButton?.requestFocus()
    }

    private fun updateButton(enabled: Boolean, view: View?) {
        if (view == null) {
            return
        }
        view.isEnabled = enabled
        view.alpha = if (enabled) buttonAlphaEnabled else buttonAlphaDisabled
    }

    private fun seekToTimeBarPosition(player: Player, positionMs: Long) {
        var positionMs = positionMs
        if (multiWindowTimeBar) {
            if (player.isCommandAvailable(Player.COMMAND_GET_TIMELINE) && player.isCommandAvailable(
                    Player.COMMAND_SEEK_TO_MEDIA_ITEM
                )
            ) {
                val timeline = player.currentTimeline
                val windowCount = timeline.windowCount
                var windowIndex = 0
                while (true) {
                    val windowDurationMs = timeline.getWindow(windowIndex, window).durationMs
                    if (positionMs < windowDurationMs) {
                        break
                    } else if (windowIndex == windowCount - 1) {
                        // Seeking past the end of the last window should seek to the end of the timeline.
                        positionMs = windowDurationMs
                        break
                    }
                    positionMs -= windowDurationMs
                    windowIndex++
                }
                player.seekTo(windowIndex, positionMs)
            }
        } else if (player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) {
            player.seekTo(positionMs)
        }
        updateProgress()
    }

    private fun onFullScreenButtonClicked(v: View) {
        if (onFullScreenModeChangedListener == null) {
            return
        }
        isFullScreen = !isFullScreen
        updateFullScreenButtonForState(fullScreenButton, isFullScreen)
        updateFullScreenButtonForState(minimalFullScreenButton, isFullScreen)
        if (onFullScreenModeChangedListener != null) {
            onFullScreenModeChangedListener!!.onFullScreenModeChanged(isFullScreen)
        }
    }

    private fun updateFullScreenButtonForState(
        fullScreenButton: ImageView?,
        isFullScreen: Boolean
    ) {
        if (fullScreenButton == null) {
            return
        }
        if (isFullScreen) {
            fullScreenButton.setImageDrawable(fullScreenExitDrawable)
            fullScreenButton.contentDescription = fullScreenExitContentDescription
        } else {
            fullScreenButton.setImageDrawable(fullScreenEnterDrawable)
            fullScreenButton.contentDescription = fullScreenEnterContentDescription
        }
    }

    private fun onSettingViewClicked(position: Int) {
        if (position == SETTINGS_PLAYBACK_SPEED_POSITION) {
            displaySettingsWindow(playbackSpeedAdapter, Assertions.checkNotNull(settingsButton))
        } else if (position == SETTINGS_AUDIO_TRACK_SELECTION_POSITION) {
            displaySettingsWindow(
                audioTrackSelectionAdapter,
                Assertions.checkNotNull(settingsButton)
            )
        } else {
            settingsWindow.dismiss()
        }
    }

    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        controlViewLayoutManager.onAttachedToWindow()
        isAttachedToWindow = true
        if (isFullyVisible) {
            controlViewLayoutManager.resetHideCallbacks()
        }
        updateAll()
    }

    public override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        controlViewLayoutManager.onDetachedFromWindow()
        isAttachedToWindow = false
        removeCallbacks(updateProgressAction)
        controlViewLayoutManager.removeHideCallbacks()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event)
    }

    /**
     * Called to process media key events. Any [KeyEvent] can be passed but only media key
     * events will be handled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    fun dispatchMediaKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val player = player
        if (player == null || !isHandledMediaKey(keyCode)) {
            return false
        }
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                if (player.playbackState != Player.STATE_ENDED && player.isCommandAvailable(Player.COMMAND_SEEK_FORWARD)) {
                    player.seekForward()
                }
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND && player.isCommandAvailable(Player.COMMAND_SEEK_BACK)) {
                player.seekBack()
            } else if (event.repeatCount == 0) {
                when (keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_HEADSETHOOK -> dispatchPlayPause(
                        player
                    )

                    KeyEvent.KEYCODE_MEDIA_PLAY -> dispatchPlay(player)
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> dispatchPause(player)
                    KeyEvent.KEYCODE_MEDIA_NEXT -> if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                        player.seekToNext()
                    }

                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS)) {
                        player.seekToPrevious()
                    }

                    else -> {}
                }
            }
        }
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        controlViewLayoutManager.onLayout(changed, left, top, right, bottom)
    }

    private fun onLayoutChange(
        v: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        val width = right - left
        val height = bottom - top
        val oldWidth = oldRight - oldLeft
        val oldHeight = oldBottom - oldTop
        if ((width != oldWidth || height != oldHeight) && settingsWindow.isShowing) {
            updateSettingsWindowSize()
            val xOffset = getWidth() - settingsWindow.width - settingsWindowMargin
            val yOffset = -settingsWindow.height - settingsWindowMargin
            settingsWindow.update(v, xOffset, yOffset, -1, -1)
        }
    }

    private fun shouldEnablePlayPauseButton(): Boolean {
        return player != null && player!!.isCommandAvailable(Player.COMMAND_PLAY_PAUSE) && (!player!!.isCommandAvailable(
            Player.COMMAND_GET_TIMELINE
        ) || !player!!.currentTimeline.isEmpty)
    }

    private fun shouldShowPauseButton(): Boolean {
        return player != null && player!!.playbackState != Player.STATE_ENDED && player!!.playbackState != Player.STATE_IDLE && player!!.playWhenReady
    }

    private fun dispatchPlayPause(player: Player) {
        val state: @Player.State Int = player.playbackState
        if (state == Player.STATE_IDLE || state == Player.STATE_ENDED || !player.playWhenReady) {
            dispatchPlay(player)
        } else {
            dispatchPause(player)
        }
    }

    private fun dispatchPlay(player: Player) {
        val state: @Player.State Int = player.playbackState
        if (state == Player.STATE_IDLE && player.isCommandAvailable(Player.COMMAND_PREPARE)) {
            player.prepare()
        } else if (state == Player.STATE_ENDED && player.isCommandAvailable(Player.COMMAND_SEEK_TO_DEFAULT_POSITION)) {
            player.seekToDefaultPosition()
        }
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            player.play()
        }
    }

    private fun dispatchPause(player: Player) {
        if (player.isCommandAvailable(Player.COMMAND_PLAY_PAUSE)) {
            player.pause()
        }
    }

    private inner class ComponentListener : Player.Listener, OnScrubListener, OnClickListener,
        PopupWindow.OnDismissListener {
        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updatePlayPauseButton()
            }
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_STATE_CHANGED,
                    Player.EVENT_PLAY_WHEN_READY_CHANGED,
                    Player.EVENT_IS_PLAYING_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateProgress()
            }
            if (events.containsAny(
                    Player.EVENT_REPEAT_MODE_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateRepeatModeButton()
            }
            if (events.containsAny(
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateShuffleButton()
            }
            if (events.containsAny(
                    Player.EVENT_REPEAT_MODE_CHANGED,
                    Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_SEEK_BACK_INCREMENT_CHANGED,
                    Player.EVENT_SEEK_FORWARD_INCREMENT_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateNavigation()
            }
            if (events.containsAny(
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_TIMELINE_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateTimeline()
            }
            if (events.containsAny(
                    Player.EVENT_PLAYBACK_PARAMETERS_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updatePlaybackSpeedList()
            }
            if (events.containsAny(
                    Player.EVENT_TRACKS_CHANGED,
                    Player.EVENT_AVAILABLE_COMMANDS_CHANGED
                )
            ) {
                updateTrackLists()
            }
        }

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            scrubbing = true
            if (positionView != null) {
                positionView.text = Util.getStringForTime(formatBuilder, formatter, position)
            }
            controlViewLayoutManager.removeHideCallbacks()
        }

        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            if (positionView != null) {
                positionView.text =
                    Util.getStringForTime(formatBuilder, formatter, position)
            }
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            scrubbing = false
            if (!canceled && player != null) {
                seekToTimeBarPosition(player!!, position)
            }
            controlViewLayoutManager.resetHideCallbacks()
        }

        override fun onDismiss() {
            if (needToHideBars) {
                controlViewLayoutManager.resetHideCallbacks()
            }
        }

        override fun onClick(view: View) {
            val player = player ?: return
            controlViewLayoutManager.resetHideCallbacks()
            if (nextButton === view) {
                if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_NEXT)) {
                    player.seekToNext()
                }
            } else if (previousButton === view) {
                if (player.isCommandAvailable(Player.COMMAND_SEEK_TO_PREVIOUS)) {
                    player.seekToPrevious()
                }
            } else if (fastForwardButton === view) {
                if (player.playbackState != Player.STATE_ENDED && player.isCommandAvailable(Player.COMMAND_SEEK_FORWARD)) {
                    player.seekForward()
                }
            } else if (rewindButton === view) {
                if (player.isCommandAvailable(Player.COMMAND_SEEK_BACK)) {
                    player.seekBack()
                }
            } else if (playPauseButton === view) {
                dispatchPlayPause(player)
            } else if (repeatToggleButton === view) {
                if (player.isCommandAvailable(Player.COMMAND_SET_REPEAT_MODE)) {
                    player.repeatMode =
                        RepeatModeUtil.getNextRepeatMode(player.repeatMode, repeatToggleModes)
                }
            } else if (shuffleButton === view) {
                if (player.isCommandAvailable(Player.COMMAND_SET_SHUFFLE_MODE)) {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                }
            } else if (settingsButton === view) {
                controlViewLayoutManager.removeHideCallbacks()
                displaySettingsWindow(settingsAdapter, settingsButton)
            } else if (playbackSpeedButton === view) {
                controlViewLayoutManager.removeHideCallbacks()
                displaySettingsWindow(playbackSpeedAdapter, playbackSpeedButton)
            } else if (audioTrackButton === view) {
                controlViewLayoutManager.removeHideCallbacks()
                displaySettingsWindow(audioTrackSelectionAdapter, audioTrackButton)
            } else if (subtitleButton === view) {
                controlViewLayoutManager.removeHideCallbacks()
                displaySettingsWindow(textTrackSelectionAdapter, subtitleButton)
            }
        }
    }

    private inner class SettingsAdapter(
        private val mainTexts: Array<String?>,
        iconIds: Array<Drawable?>
    ) : RecyclerView.Adapter<SettingViewHolder>() {
        private val subTexts: Array<String?>
        private val iconIds: Array<Drawable?>

        init {
            subTexts = arrayOfNulls(mainTexts.size)
            this.iconIds = iconIds
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
            val v = LayoutInflater.from(context)
                .inflate(R.layout.exo_styled_settings_list_item, parent,  /* attachToRoot= */false)
            return SettingViewHolder(v)
        }

        override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
            if (shouldShowSetting(position)) {
                holder.itemView.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            } else {
                holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            }
            holder.mainTextView.text = mainTexts[position]
            if (subTexts[position] == null) {
                holder.subTextView.visibility = GONE
            } else {
                holder.subTextView.text = subTexts[position]
            }
            if (iconIds[position] == null) {
                holder.iconView.visibility = GONE
            } else {
                holder.iconView.setImageDrawable(iconIds[position])
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return mainTexts.size
        }

        fun setSubTextAtPosition(position: Int, subText: String?) {
            subTexts[position] = subText
        }

        fun hasSettingsToShow(): Boolean {
            return shouldShowSetting(SETTINGS_AUDIO_TRACK_SELECTION_POSITION) || shouldShowSetting(
                SETTINGS_PLAYBACK_SPEED_POSITION
            )
        }

        private fun shouldShowSetting(position: Int): Boolean {
            return if (player == null) {
                false
            } else when (position) {
                SETTINGS_AUDIO_TRACK_SELECTION_POSITION -> player!!.isCommandAvailable(
                    Player.COMMAND_GET_TRACKS
                ) && player!!.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)

                SETTINGS_PLAYBACK_SPEED_POSITION -> player!!.isCommandAvailable(
                    Player.COMMAND_SET_SPEED_AND_PITCH
                )

                else -> true
            }
        }
    }

    private inner class SettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainTextView: TextView
        val subTextView: TextView
        val iconView: ImageView

        init {
            if (Util.SDK_INT < 26) {
                // Workaround for https://github.com/google/ExoPlayer/issues/9061.
                itemView.isFocusable = true
            }
            mainTextView = itemView.findViewById(R.id.exo_main_text)
            subTextView = itemView.findViewById(R.id.exo_sub_text)
            iconView = itemView.findViewById(R.id.exo_icon)
            itemView.setOnClickListener { v: View? ->
                onSettingViewClicked(
                    adapterPosition
                )
            }
        }
    }

    private inner class PlaybackSpeedAdapter(
        private val playbackSpeedTexts: Array<String>,
        private val playbackSpeeds: FloatArray
    ) : RecyclerView.Adapter<SubSettingViewHolder>() {
        private var selectedIndex = 0
        fun updateSelectedIndex(playbackSpeed: Float) {
            var closestMatchIndex = 0
            var closestMatchDifference = Float.MAX_VALUE
            for (i in playbackSpeeds.indices) {
                val difference = Math.abs(playbackSpeed - playbackSpeeds[i])
                if (difference < closestMatchDifference) {
                    closestMatchIndex = i
                    closestMatchDifference = difference
                }
            }
            selectedIndex = closestMatchIndex
        }

        val selectedText: String
            get() = playbackSpeedTexts[selectedIndex]

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubSettingViewHolder {
            val v = LayoutInflater.from(context).inflate(
                R.layout.exo_styled_sub_settings_list_item,
                parent,  /* attachToRoot= */
                false
            )
            return SubSettingViewHolder(v)
        }

        override fun onBindViewHolder(holder: SubSettingViewHolder, position: Int) {
            if (position < playbackSpeedTexts.size) {
                holder.textView.text = playbackSpeedTexts[position]
            }
            if (position == selectedIndex) {
                holder.itemView.isSelected = true
                holder.checkView.visibility = VISIBLE
            } else {
                holder.itemView.isSelected = false
                holder.checkView.visibility = INVISIBLE
            }
            holder.itemView.setOnClickListener { v: View? ->
                if (position != selectedIndex) {
                    setPlaybackSpeed(playbackSpeeds[position])
                }
                settingsWindow.dismiss()
            }
        }

        override fun getItemCount(): Int {
            return playbackSpeedTexts.size
        }
    }

    private class TrackInformation(
        tracks: Tracks,
        trackGroupIndex: Int,
        trackIndex: Int,
        trackName: String
    ) {
        val trackGroup: Tracks.Group
        val trackIndex: Int
        val trackName: String

        init {
            trackGroup = tracks.groups[trackGroupIndex]
            this.trackIndex = trackIndex
            this.trackName = trackName
        }

        val isSelected: Boolean
            get() = trackGroup.isTrackSelected(trackIndex)
    }

    private inner class TextTrackSelectionAdapter : TrackSelectionAdapter() {
        override fun init(trackInformations: List<TrackInformation>) {
            var subtitleIsOn = false
            for (i in trackInformations.indices) {
                if (trackInformations[i].isSelected) {
                    subtitleIsOn = true
                    break
                }
            }
            if (subtitleButton != null) {
                subtitleButton.setImageDrawable(if (subtitleIsOn) subtitleOnButtonDrawable else subtitleOffButtonDrawable)
                subtitleButton.contentDescription =
                    if (subtitleIsOn) subtitleOnContentDescription else subtitleOffContentDescription
            }
            tracks = trackInformations
        }

        public override fun onBindViewHolderAtZeroPosition(holder: SubSettingViewHolder) {
            // CC options include "Off" at the first position, which disables text rendering.
            holder.textView.setText(R.string.exo_track_selection_none)
            var isTrackSelectionOff = true
            for (i in tracks.indices) {
                if (tracks[i].isSelected) {
                    isTrackSelectionOff = false
                    break
                }
            }
            holder.checkView.visibility = if (isTrackSelectionOff) VISIBLE else INVISIBLE
            holder.itemView.setOnClickListener { v: View? ->
                if (player != null && player!!.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)) {
                    val trackSelectionParameters = player!!.trackSelectionParameters
                    player!!.trackSelectionParameters =
                        trackSelectionParameters.buildUpon().clearOverridesOfType(
                            C.TRACK_TYPE_TEXT
                        ).setIgnoredTextSelectionFlags(C.SELECTION_FLAG_FORCED.inv()).build()
                    settingsWindow.dismiss()
                }
            }
        }

        override fun onBindViewHolder(holder: SubSettingViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            if (position > 0) {
                val track = tracks[position - 1]
                holder.checkView.visibility =
                    if (track.isSelected) VISIBLE else INVISIBLE
            }
        }

        public override fun onTrackSelection(subtext: String?) {
            // No-op
        }
    }

    private inner class AudioTrackSelectionAdapter : TrackSelectionAdapter() {
        public override fun onBindViewHolderAtZeroPosition(holder: SubSettingViewHolder) {
            // Audio track selection option includes "Auto" at the top.
            holder.textView.setText(R.string.exo_track_selection_auto)
            // hasSelectionOverride is true means there is an explicit track selection, not "Auto".
            val parameters = Assertions.checkNotNull(player).trackSelectionParameters
            val hasSelectionOverride = hasSelectionOverride(parameters)
            holder.checkView.visibility = if (hasSelectionOverride) INVISIBLE else VISIBLE
            holder.itemView.setOnClickListener { v: View? ->
                if (player == null || !player!!.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)) {
                    return@setOnClickListener
                }
                val trackSelectionParameters = player!!.trackSelectionParameters
                Util.castNonNull(player).trackSelectionParameters =
                    trackSelectionParameters.buildUpon().clearOverridesOfType(
                        C.TRACK_TYPE_AUDIO
                    ).setTrackTypeDisabled(C.TRACK_TYPE_AUDIO,  /* disabled= */false).build()
                settingsAdapter.setSubTextAtPosition(
                    SETTINGS_AUDIO_TRACK_SELECTION_POSITION,
                    getResources().getString(R.string.exo_track_selection_auto)
                )
                settingsWindow.dismiss()
            }
        }

        private fun hasSelectionOverride(trackSelectionParameters: TrackSelectionParameters): Boolean {
            for (i in tracks.indices) {
                val trackGroup = tracks[i].trackGroup.mediaTrackGroup
                if (trackSelectionParameters.overrides.containsKey(trackGroup)) {
                    return true
                }
            }
            return false
        }

        public override fun onTrackSelection(subtext: String?) {
            settingsAdapter.setSubTextAtPosition(SETTINGS_AUDIO_TRACK_SELECTION_POSITION, subtext)
        }

        override fun init(trackInformations: List<TrackInformation>) {
            tracks = trackInformations
            // Update subtext in settings menu with current audio track selection.
            val params = Assertions.checkNotNull(player).trackSelectionParameters
            if (trackInformations.isEmpty()) {
                settingsAdapter.setSubTextAtPosition(
                    SETTINGS_AUDIO_TRACK_SELECTION_POSITION,
                    getResources().getString(R.string.exo_track_selection_none)
                )
                // TODO(insun) : Make the audio item in main settings (settingsAdapater)
                //  to be non-clickable.
            } else if (!hasSelectionOverride(params)) {
                settingsAdapter.setSubTextAtPosition(
                    SETTINGS_AUDIO_TRACK_SELECTION_POSITION,
                    getResources().getString(R.string.exo_track_selection_auto)
                )
            } else {
                for (i in trackInformations.indices) {
                    val track = trackInformations[i]
                    if (track.isSelected) {
                        settingsAdapter.setSubTextAtPosition(
                            SETTINGS_AUDIO_TRACK_SELECTION_POSITION,
                            track.trackName
                        )
                        break
                    }
                }
            }
        }
    }

    private abstract inner class TrackSelectionAdapter protected constructor() :
        RecyclerView.Adapter<SubSettingViewHolder>() {
        protected var tracks: List<TrackInformation>

        init {
            tracks = ArrayList()
        }

        abstract fun init(trackInformations: List<TrackInformation>)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubSettingViewHolder {
            val v = LayoutInflater.from(context).inflate(
                R.layout.exo_styled_sub_settings_list_item,
                parent,  /* attachToRoot= */
                false
            )
            return SubSettingViewHolder(v)
        }

        protected abstract fun onBindViewHolderAtZeroPosition(holder: SubSettingViewHolder)
        protected abstract fun onTrackSelection(subtext: String?)
        override fun onBindViewHolder(holder: SubSettingViewHolder, position: Int) {
            val player = player ?: return
            if (position == 0) {
                onBindViewHolderAtZeroPosition(holder)
            } else {
                val track = tracks[position - 1]
                val mediaTrackGroup = track.trackGroup.mediaTrackGroup
                val params = player.trackSelectionParameters
                val explicitlySelected =
                    params.overrides[mediaTrackGroup] != null && track.isSelected
                holder.textView.text = track.trackName
                holder.checkView.visibility = if (explicitlySelected) VISIBLE else INVISIBLE
                holder.itemView.setOnClickListener { v: View? ->
                    if (!player.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)) {
                        return@setOnClickListener
                    }
                    val trackSelectionParameters = player.trackSelectionParameters
                    player.trackSelectionParameters = trackSelectionParameters.buildUpon()
                        .setOverrideForType(
                            TrackSelectionOverride(
                                mediaTrackGroup,
                                ImmutableList.of(track.trackIndex)
                            )
                        ).setTrackTypeDisabled(track.trackGroup.type,  /* disabled= */false).build()
                    onTrackSelection(track.trackName)
                    settingsWindow.dismiss()
                }
            }
        }

        override fun getItemCount(): Int {
            return if (tracks.isEmpty()) 0 else tracks.size + 1
        }

        fun clear() {
            tracks = emptyList()
        }
    }

    private class SubSettingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView
        val checkView: View

        init {
            if (Util.SDK_INT < 26) {
                // Workaround for https://github.com/google/ExoPlayer/issues/9061.
                itemView.isFocusable = true
            }
            textView = itemView.findViewById(R.id.exo_text)
            checkView = itemView.findViewById(R.id.exo_check)
        }
    }

    companion object {
        init {
            MediaLibraryInfo.registerModule("media3.ui")
        }

        /**
         * The default show timeout, in milliseconds.
         */
        const val DEFAULT_SHOW_TIMEOUT_MS = 5000

        /**
         * The default repeat toggle modes.
         */
        const val DEFAULT_REPEAT_TOGGLE_MODES: @RepeatToggleModes Int =
            RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE

        /**
         * The default minimum interval between time bar position updates.
         */
        const val DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS = 200

        /**
         * The maximum number of windows that can be shown in a multi-window time bar.
         */
        const val MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR = 100

        /**
         * The maximum interval between time bar position updates.
         */
        private const val MAX_UPDATE_INTERVAL_MS = 1000

        // LINT.IfChange(playback_speeds)
        private val PLAYBACK_SPEEDS = floatArrayOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
        private const val SETTINGS_PLAYBACK_SPEED_POSITION = 0
        private const val SETTINGS_AUDIO_TRACK_SELECTION_POSITION = 1

        @SuppressLint("InlinedApi")
        private fun isHandledMediaKey(keyCode: Int): Boolean {
            return keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD || keyCode == KeyEvent.KEYCODE_MEDIA_REWIND || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE || keyCode == KeyEvent.KEYCODE_MEDIA_NEXT || keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS
        }

        /**
         * Returns whether the specified `player` can be shown on a multi-window time bar.
         *
         * @param player The [Player] to check.
         * @param window A scratch [Timeline.Window] instance.
         * @return Whether the specified timeline can be shown on a multi-window time bar.
         */
        private fun canShowMultiWindowTimeBar(player: Player, window: Timeline.Window): Boolean {
            if (!player.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) {
                return false
            }
            val timeline = player.currentTimeline
            val windowCount = timeline.windowCount
            if (windowCount <= 1 || windowCount > MAX_WINDOWS_FOR_MULTI_WINDOW_TIME_BAR) {
                return false
            }
            for (i in 0 until windowCount) {
                if (timeline.getWindow(i, window).durationUs == C.TIME_UNSET) {
                    return false
                }
            }
            return true
        }

        private fun initializeFullScreenButton(fullScreenButton: View?, listener: OnClickListener) {
            if (fullScreenButton == null) {
                return
            }
            fullScreenButton.visibility = GONE
            fullScreenButton.setOnClickListener(listener)
        }

        private fun updateFullScreenButtonVisibility(fullScreenButton: View?, visible: Boolean) {
            if (fullScreenButton == null) {
                return
            }
            if (visible) {
                fullScreenButton.visibility = VISIBLE
            } else {
                fullScreenButton.visibility = GONE
            }
        }

        private fun getRepeatToggleModes(
            a: TypedArray,
            defaultValue: @RepeatToggleModes Int
        ): @RepeatToggleModes Int {
            return a.getInt(R.styleable.PlayerControlView_repeat_toggle_modes, defaultValue)
        }
    }
}