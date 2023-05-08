/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.idance.hocnhayonline.customView.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.opengl.GLSurfaceView
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.media3.common.AdOverlayInfo
import androidx.media3.common.AdViewProvider
import androidx.media3.common.C
import androidx.media3.common.ErrorMessageProvider
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.DiscontinuityReason
import androidx.media3.common.Player.PlayWhenReadyChangeReason
import androidx.media3.common.Player.PositionInfo
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.RepeatModeUtil.RepeatToggleModes
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.AspectRatioFrameLayout.AspectRatioListener
import androidx.media3.ui.AspectRatioFrameLayout.ResizeMode
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R
import androidx.media3.ui.SubtitleView
import com.google.common.collect.ImmutableList
import org.checkerframework.checker.nullness.qual.EnsuresNonNullIf
import org.checkerframework.checker.nullness.qual.RequiresNonNull

@UnstableApi
internal class PlayerView constructor(
    context: Context, attrs: AttributeSet?, defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr), AdViewProvider {
    /**
     * Listener to be notified about changes of the visibility of the UI controls.
     */
    interface ControllerVisibilityListener {
        /**
         * Called when the visibility changes.
         *
         * @param visibility The new visibility. Either [View.VISIBLE] or [View.GONE].
         */
        fun onVisibilityChanged(visibility: Int)
    }

    /**
     * Listener invoked when the fullscreen button is clicked. The implementation is responsible for
     * changing the UI layout.
     */
    interface FullscreenButtonClickListener {
        /**
         * Called when the fullscreen button is clicked.
         *
         * @param isFullScreen `true` if the video rendering surface should be fullscreen, `false` otherwise.
         */
        fun onFullscreenButtonClick(isFullScreen: Boolean)
    }

    /**
     * Determines when the buffering view is shown. One of [.SHOW_BUFFERING_NEVER], [ ][.SHOW_BUFFERING_WHEN_PLAYING] or [.SHOW_BUFFERING_ALWAYS].
     */
    @UnstableApi
    @MustBeDocumented
    @Retention(AnnotationRetention.SOURCE)
    @Target(AnnotationTarget.TYPE)
    @IntDef(*[PlayerView.SHOW_BUFFERING_NEVER, PlayerView.SHOW_BUFFERING_WHEN_PLAYING, PlayerView.SHOW_BUFFERING_ALWAYS])
    annotation class ShowBuffering

    private val componentListener: ComponentListener
    private var contentFrame: AspectRatioFrameLayout?
    private var shutterView: View?

    /**
     * Gets the view onto which video is rendered. This is a:
     *
     *
     *  * [SurfaceView] by default, or if the `surface_type` attribute is set to `surface_view`.
     *  * [TextureView] if `surface_type` is `texture_view`.
     *  * `SphericalGLSurfaceView` if `surface_type` is `spherical_gl_surface_view`.
     *  * `VideoDecoderGLSurfaceView` if `surface_type` is `video_decoder_gl_surface_view`.
     *  * `null` if `surface_type` is `none`.
     *
     *
     * @return The [SurfaceView], [TextureView], `SphericalGLSurfaceView`, `VideoDecoderGLSurfaceView` or `null`.
     */
    @get:UnstableApi
    var videoSurfaceView: View? = null
    private var surfaceViewIgnoresVideoAspectRatio: Boolean
    private var artworkView: ImageView?

    /**
     * Gets the [SubtitleView].
     *
     * @return The [SubtitleView], or `null` if the layout has been customized and the
     * subtitle view is not present.
     */
    @get:UnstableApi
    var subtitleView: SubtitleView?
    private var bufferingView: View?
    private var errorMessageView: TextView?
    var controller: PlayerControlView? = null
    private var adOverlayFrameLayout: FrameLayout?

    /**
     * Gets the overlay [FrameLayout], which can be populated with UI elements to show on top of
     * the player.
     *
     * @return The overlay [FrameLayout], or `null` if the layout has been customized and
     * the overlay is not present.
     */
    @get:UnstableApi
    var overlayFrameLayout: FrameLayout?
    private var player: Player? = null
    private var useController: Boolean

    // At most one of controllerVisibilityListener and legacyControllerVisibilityListener is non-null.
    private var controllerVisibilityListener: PlayerView.ControllerVisibilityListener? = null
    private var legacyControllerVisibilityListener: PlayerControlView.VisibilityListener? = null
    private var fullscreenButtonClickListener: PlayerView.FullscreenButtonClickListener? = null
    private var useArtwork: Boolean
    private var defaultArtwork: Drawable? = null
    private var showBuffering: @ShowBuffering Int
    private var keepContentOnPlayerReset = false
    private var errorMessageProvider: ErrorMessageProvider<in PlaybackException?>? = null
    private var customErrorMessage: CharSequence? = null
    private var controllerShowTimeoutMs: Int
    /**
     * Returns whether the playback controls are automatically shown when playback starts, pauses,
     * ends, or fails. If set to false, the playback controls can be manually operated with [ ][.showController] and [.hideController].
     */
    /**
     * Sets whether the playback controls are automatically shown when playback starts, pauses, ends,
     * or fails. If set to false, the playback controls can be manually operated with [ ][.showController] and [.hideController].
     *
     * @param controllerAutoShow Whether the playback controls are allowed to show automatically.
     */
    @get:UnstableApi
    @set:UnstableApi
    var controllerAutoShow: Boolean
    private var controllerHideDuringAds: Boolean
    private var controllerHideOnTouch: Boolean
    private var textureViewRotation = 0
    private val isTouching = false

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? =  /* attrs= */null) : this(
        context, attrs,  /* defStyleAttr= */
        0
    )

    init {
        componentListener = ComponentListener()
        kotlin.run {
            if (isInEditMode) {
                contentFrame = null
                shutterView = null
                videoSurfaceView = null
                surfaceViewIgnoresVideoAspectRatio = false
                artworkView = null
                subtitleView = null
                bufferingView = null
                errorMessageView = null
                controller = null
                adOverlayFrameLayout = null
                overlayFrameLayout = null
                val logo = ImageView(context)
                if (Util.SDK_INT >= 23) {
                    configureEditModeLogoV23(context, resources, logo)
                } else {
                    configureEditModeLogo(context, resources, logo)
                }
                addView(logo)
                return@run
            }
        }
        var shutterColorSet = false
        var shutterColor = 0
        var playerLayoutId = com.idance.hocnhayonline.R.layout.exo_player_view
        var useArtwork = true
        var defaultArtworkId = 0
        var useController = true
        var surfaceType = SURFACE_TYPE_SURFACE_VIEW
        var resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        var controllerShowTimeoutMs = PlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
        var controllerHideOnTouch = true
        var controllerAutoShow = true
        var controllerHideDuringAds = true
        var showBuffering = SHOW_BUFFERING_NEVER
        if (attrs != null) {
            val a = context.theme.obtainStyledAttributes(
                attrs, R.styleable.PlayerView, defStyleAttr,  /* defStyleRes= */0
            )
            try {
                shutterColorSet = a.hasValue(R.styleable.PlayerView_shutter_background_color)
                shutterColor =
                    a.getColor(R.styleable.PlayerView_shutter_background_color, shutterColor)
                playerLayoutId =
                    a.getResourceId(R.styleable.PlayerView_player_layout_id, playerLayoutId)
                useArtwork = a.getBoolean(R.styleable.PlayerView_use_artwork, useArtwork)
                defaultArtworkId =
                    a.getResourceId(R.styleable.PlayerView_default_artwork, defaultArtworkId)
                useController = a.getBoolean(R.styleable.PlayerView_use_controller, useController)
                surfaceType = a.getInt(R.styleable.PlayerView_surface_type, surfaceType)
                resizeMode = a.getInt(R.styleable.PlayerView_resize_mode, resizeMode)
                controllerShowTimeoutMs =
                    a.getInt(R.styleable.PlayerView_show_timeout, controllerShowTimeoutMs)
                controllerHideOnTouch =
                    a.getBoolean(R.styleable.PlayerView_hide_on_touch, controllerHideOnTouch)
                controllerAutoShow =
                    a.getBoolean(R.styleable.PlayerView_auto_show, controllerAutoShow)
                showBuffering = a.getInteger(R.styleable.PlayerView_show_buffering, showBuffering)
                keepContentOnPlayerReset = a.getBoolean(
                    R.styleable.PlayerView_keep_content_on_player_reset, keepContentOnPlayerReset
                )
                controllerHideDuringAds =
                    a.getBoolean(R.styleable.PlayerView_hide_during_ads, controllerHideDuringAds)
            } finally {
                a.recycle()
            }
        }
        LayoutInflater.from(context).inflate(playerLayoutId, this)
        descendantFocusability = FOCUS_AFTER_DESCENDANTS

        // Content frame.
        contentFrame = findViewById(R.id.exo_content_frame)
        if (contentFrame != null) {
            setResizeModeRaw(contentFrame!!, resizeMode)
        }

        // Shutter view.
        shutterView = findViewById(R.id.exo_shutter)
        if (shutterView != null && shutterColorSet) {
            shutterView!!.setBackgroundColor(shutterColor)
        }

        // Create a surface view and insert it into the content frame, if there is one.
        var surfaceViewIgnoresVideoAspectRatio = false
        if (contentFrame != null && surfaceType != SURFACE_TYPE_NONE) {
            val params = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            when (surfaceType) {
                SURFACE_TYPE_TEXTURE_VIEW -> videoSurfaceView = TextureView(context)
                SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW -> {
                    try {
                        val clazz =
                            Class.forName("androidx.media3.exoplayer.video.spherical.SphericalGLSurfaceView")
                        videoSurfaceView =
                            clazz.getConstructor(Context::class.java).newInstance(context) as View
                    } catch (e: Exception) {
                        throw IllegalStateException(
                            "spherical_gl_surface_view requires an ExoPlayer dependency", e
                        )
                    }
                    surfaceViewIgnoresVideoAspectRatio = true
                }

                SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW -> try {
                    val clazz =
                        Class.forName("androidx.media3.exoplayer.video.VideoDecoderGLSurfaceView")
                    videoSurfaceView =
                        clazz.getConstructor(Context::class.java).newInstance(context) as View
                } catch (e: Exception) {
                    throw IllegalStateException(
                        "video_decoder_gl_surface_view requires an ExoPlayer dependency", e
                    )
                }

                else -> videoSurfaceView = SurfaceView(context)
            }
            videoSurfaceView!!.layoutParams = params
            // We don't want surfaceView to be clickable separately to the PlayerView itself, but we
            // do want to register as an OnClickListener so that surfaceView implementations can propagate
            // click events up to the PlayerView by calling their own performClick method.
            videoSurfaceView!!.setOnClickListener(componentListener)
            videoSurfaceView!!.isClickable = false
            contentFrame?.addView(videoSurfaceView, 0)
        } else {
            videoSurfaceView = null
        }
        this.surfaceViewIgnoresVideoAspectRatio = surfaceViewIgnoresVideoAspectRatio

        // Ad overlay frame layout.
        adOverlayFrameLayout = findViewById(R.id.exo_ad_overlay)

        // Overlay frame layout.
        overlayFrameLayout = findViewById(R.id.exo_overlay)

        // Artwork view.
        artworkView = findViewById(R.id.exo_artwork)
        this.useArtwork = useArtwork && artworkView != null
        if (defaultArtworkId != 0) {
            defaultArtwork = ContextCompat.getDrawable(getContext(), defaultArtworkId)
        }

        // Subtitle view.
        subtitleView = findViewById(R.id.exo_subtitles)
        if (subtitleView != null) {
            subtitleView!!.setUserDefaultStyle()
            subtitleView!!.setUserDefaultTextSize()
        }

        // Buffering view.
        bufferingView = findViewById(R.id.exo_buffering)
        if (bufferingView != null) {
            bufferingView?.visibility = GONE
        }
        this.showBuffering = showBuffering

        // Error message view.
        errorMessageView = findViewById(R.id.exo_error_message)
        if (errorMessageView != null) {
            errorMessageView?.visibility = GONE
        }

        // Playback control view.
        val customController = findViewById<PlayerControlView>(R.id.exo_controller)
        val controllerPlaceholder = findViewById<View>(R.id.exo_controller_placeholder)
        if (customController != null) {
            controller = customController
        } else if (controllerPlaceholder != null) {
            // Propagate attrs as playbackAttrs so that PlayerControlView's custom attributes are
            // transferred, but standard attributes (e.g. background) are not.
            controller = PlayerControlView(context, null, 0, attrs)
            controller?.id = R.id.exo_controller
            controller?.layoutParams = controllerPlaceholder.layoutParams
            val parent = controllerPlaceholder.parent as ViewGroup
            val controllerIndex = parent.indexOfChild(controllerPlaceholder)
            parent.removeView(controllerPlaceholder)
            parent.addView(controller, controllerIndex)
        } else {
            controller = null
        }
        this.controllerShowTimeoutMs = if (controller != null) controllerShowTimeoutMs else 0
        this.controllerHideOnTouch = controllerHideOnTouch
        this.controllerAutoShow = controllerAutoShow
        this.controllerHideDuringAds = controllerHideDuringAds
        this.useController = useController && controller != null
        if (controller != null) {
            controller?.hideImmediately()
            controller?.addVisibilityListener( /* listener= */componentListener)
        }
        if (useController) {
            isClickable = true
        }
        updateContentDescription()
    }

    /**
     * Sets the [Player] to use.
     *
     *
     * To transition a [Player] from targeting one view to another, it's recommended to use
     * [.switchTargetView] rather than this method. If you do
     * wish to use this method directly, be sure to attach the player to the new view *before*
     * calling `setPlayer(null)` to detach it from the old one. This ordering is significantly
     * more efficient and may allow for more seamless transitions.
     *
     * @param player The [Player] to use, or `null` to detach the current player. Only
     * players which are accessed on the main thread are supported (`player.getApplicationLooper() == Looper.getMainLooper()`).
     */
    fun setPlayer(player: Player?) {
        Assertions.checkState(Looper.myLooper() == Looper.getMainLooper())
        Assertions.checkArgument(
            player == null || player.applicationLooper == Looper.getMainLooper()
        )
        if (this.player === player) {
            return
        }
        val oldPlayer = this.player
        if (oldPlayer != null) {
            oldPlayer.removeListener(componentListener)
            if (oldPlayer.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                if (videoSurfaceView is TextureView) {
                    oldPlayer.clearVideoTextureView(videoSurfaceView as TextureView?)
                } else if (videoSurfaceView is SurfaceView) {
                    oldPlayer.clearVideoSurfaceView(videoSurfaceView as SurfaceView?)
                }
            }
        }
        if (subtitleView != null) {
            subtitleView!!.setCues(null)
        }
        this.player = player
        if (useController()) {
            controller!!.setPlayer(player)
        }
        updateBuffering()
        updateErrorMessage()
        updateForCurrentTrackSelections( /* isNewPlayer= */true)
        if (player != null) {
            if (player.isCommandAvailable(Player.COMMAND_SET_VIDEO_SURFACE)) {
                if (videoSurfaceView is TextureView) {
                    player.setVideoTextureView(videoSurfaceView as TextureView?)
                } else if (videoSurfaceView is SurfaceView) {
                    player.setVideoSurfaceView(videoSurfaceView as SurfaceView?)
                }
                updateAspectRatio()
            }
            if (subtitleView != null && player.isCommandAvailable(Player.COMMAND_GET_TEXT)) {
                subtitleView!!.setCues(player.currentCues.cues)
            }
            player.addListener(componentListener)
            maybeShowController(false)
        } else {
            hideController()
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (videoSurfaceView is SurfaceView) {
            // Work around https://github.com/google/ExoPlayer/issues/3160.
            videoSurfaceView?.visibility = visibility
        }
    }
    /**
     * Returns the [ResizeMode].
     */
    /**
     * Sets the [ResizeMode].
     *
     * @param resizeMode The [ResizeMode].
     */
    @get:UnstableApi
    @set:UnstableApi
    var resizeMode: @ResizeMode Int
        get() {
            Assertions.checkStateNotNull(contentFrame)
            return contentFrame!!.resizeMode
        }
        set(resizeMode) {
            Assertions.checkStateNotNull(contentFrame)
            contentFrame!!.resizeMode = resizeMode
        }

    /**
     * Returns whether artwork is displayed if present in the media.
     */
    @UnstableApi
    fun getUseArtwork(): Boolean {
        return useArtwork
    }

    /**
     * Sets whether artwork is displayed if present in the media.
     *
     * @param useArtwork Whether artwork is displayed.
     */
    @UnstableApi
    fun setUseArtwork(useArtwork: Boolean) {
        Assertions.checkState(!useArtwork || artworkView != null)
        if (this.useArtwork != useArtwork) {
            this.useArtwork = useArtwork
            updateForCurrentTrackSelections( /* isNewPlayer= */false)
        }
    }

    /**
     * Returns the default artwork to display.
     */
    @UnstableApi
    fun getDefaultArtwork(): Drawable? {
        return defaultArtwork
    }

    /**
     * Sets the default artwork to display if `useArtwork` is `true` and no artwork is
     * present in the media.
     *
     * @param defaultArtwork the default artwork to display
     */
    @UnstableApi
    fun setDefaultArtwork(defaultArtwork: Drawable?) {
        if (this.defaultArtwork !== defaultArtwork) {
            this.defaultArtwork = defaultArtwork
            updateForCurrentTrackSelections( /* isNewPlayer= */false)
        }
    }

    /**
     * Returns whether the playback controls can be shown.
     */
    fun getUseController(): Boolean {
        return useController
    }

    /**
     * Sets whether the playback controls can be shown. If set to `false` the playback controls
     * are never visible and are disconnected from the player.
     *
     *
     * This call will update whether the view is clickable. After the call, the view will be
     * clickable if playback controls can be shown or if the view has a registered click listener.
     *
     * @param useController Whether the playback controls can be shown.
     */
    fun setUseController(useController: Boolean) {
        Assertions.checkState(!useController || controller != null)
        isClickable = useController || hasOnClickListeners()
        if (this.useController == useController) {
            return
        }
        this.useController = useController
        if (useController()) {
            controller!!.setPlayer(player)
        } else if (controller != null) {
            controller?.hide()
            controller?.setPlayer(null)
        }
        updateContentDescription()
    }

    /**
     * Sets the background color of the `exo_shutter` view.
     *
     * @param color The background color.
     */
    @UnstableApi
    fun setShutterBackgroundColor(@ColorInt color: Int) {
        if (shutterView != null) {
            shutterView?.setBackgroundColor(color)
        }
    }

    /**
     * Sets whether the currently displayed video frame or media artwork is kept visible when the
     * player is reset. A player reset is defined to mean the player being re-prepared with different
     * media, the player transitioning to unprepared media or an empty list of media items, or the
     * player being replaced or cleared by calling [.setPlayer].
     *
     *
     * If enabled, the currently displayed video frame or media artwork will be kept visible until
     * the player set on the view has been successfully prepared with new media and loaded enough of
     * it to have determined the available tracks. Hence enabling this option allows transitioning
     * from playing one piece of media to another, or from using one player instance to another,
     * without clearing the view's content.
     *
     *
     * If disabled, the currently displayed video frame or media artwork will be hidden as soon as
     * the player is reset. Note that the video frame is hidden by making `exo_shutter` visible.
     * Hence the video frame will not be hidden if using a custom layout that omits this view.
     *
     * @param keepContentOnPlayerReset Whether the currently displayed video frame or media artwork is
     * kept visible when the player is reset.
     */
    @UnstableApi
    fun setKeepContentOnPlayerReset(keepContentOnPlayerReset: Boolean) {
        if (this.keepContentOnPlayerReset != keepContentOnPlayerReset) {
            this.keepContentOnPlayerReset = keepContentOnPlayerReset
            updateForCurrentTrackSelections( /* isNewPlayer= */false)
        }
    }

    /**
     * Sets whether a buffering spinner is displayed when the player is in the buffering state. The
     * buffering spinner is not displayed by default.
     *
     * @param showBuffering The mode that defines when the buffering spinner is displayed. One of
     * [.SHOW_BUFFERING_NEVER], [.SHOW_BUFFERING_WHEN_PLAYING] and [                      ][.SHOW_BUFFERING_ALWAYS].
     */
    @UnstableApi
    fun setShowBuffering(showBuffering: @ShowBuffering Int) {
        if (this.showBuffering != showBuffering) {
            this.showBuffering = showBuffering
            updateBuffering()
        }
    }

    /**
     * Sets the optional [ErrorMessageProvider].
     *
     * @param errorMessageProvider The error message provider.
     */
    fun setErrorMessageProvider(
        errorMessageProvider: ErrorMessageProvider<in PlaybackException?>?
    ) {
        if (this.errorMessageProvider !== errorMessageProvider) {
            this.errorMessageProvider = errorMessageProvider
            updateErrorMessage()
        }
    }

    /**
     * Sets a custom error message to be displayed by the view. The error message will be displayed
     * permanently, unless it is cleared by passing `null` to this method.
     *
     * @param message The message to display, or `null` to clear a previously set message.
     */
    @UnstableApi
    fun setCustomErrorMessage(message: CharSequence?) {
        Assertions.checkState(errorMessageView != null)
        customErrorMessage = message
        updateErrorMessage()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (player != null && player!!.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) && player!!.isPlayingAd) {
            return super.dispatchKeyEvent(event)
        }
        val isDpadKey = isDpadKey(event.keyCode)
        var handled = false
        if (isDpadKey && useController() && !controller!!.isFullyVisible) {
            // Handle the key event by showing the controller.
            maybeShowController(true)
            handled = true
        } else if (dispatchMediaKeyEvent(event) || super.dispatchKeyEvent(event)) {
            // The key event was handled as a media key or by the super class. We should also show the
            // controller, or extend its show timeout if already visible.
            maybeShowController(true)
            handled = true
        } else if (isDpadKey && useController()) {
            // The key event wasn't handled, but we should extend the controller's show timeout.
            maybeShowController(true)
        }
        return handled
    }

    /**
     * Called to process media key events. Any [KeyEvent] can be passed but only media key
     * events will be handled. Does nothing if playback controls are disabled.
     *
     * @param event A key event.
     * @return Whether the key event was handled.
     */
    @UnstableApi
    fun dispatchMediaKeyEvent(event: KeyEvent?): Boolean {
        return useController() && controller!!.dispatchMediaKeyEvent(event!!)
    }

    /**
     * Returns whether the controller is currently fully visible.
     */
    @get:UnstableApi
    val isControllerFullyVisible: Boolean
        get() = controller != null && controller!!.isFullyVisible

    /**
     * Shows the playback controls. Does nothing if playback controls are disabled.
     *
     *
     * The playback controls are automatically hidden during playback after {[ ][.getControllerShowTimeoutMs]}. They are shown indefinitely when playback has not started yet,
     * is paused, has ended or failed.
     */
    @UnstableApi
    fun showController() {
        showController(shouldShowControllerIndefinitely())
    }

    /**
     * Hides the playback controls. Does nothing if playback controls are disabled.
     */
    @UnstableApi
    fun hideController() {
        if (controller != null) {
            controller?.hide()
        }
    }

    /**
     * Returns the playback controls timeout. The playback controls are automatically hidden after
     * this duration of time has elapsed without user input and with playback or buffering in
     * progress.
     *
     * @return The timeout in milliseconds. A non-positive value will cause the controller to remain
     * visible indefinitely.
     */
    @UnstableApi
    fun getControllerShowTimeoutMs(): Int {
        return controllerShowTimeoutMs
    }

    /**
     * Sets the playback controls timeout. The playback controls are automatically hidden after this
     * duration of time has elapsed without user input and with playback or buffering in progress.
     *
     * @param controllerShowTimeoutMs The timeout in milliseconds. A non-positive value will cause the
     * controller to remain visible indefinitely.
     */
    @UnstableApi
    fun setControllerShowTimeoutMs(controllerShowTimeoutMs: Int) {
        Assertions.checkStateNotNull(controller)
        this.controllerShowTimeoutMs = controllerShowTimeoutMs
        if (controller!!.isFullyVisible) {
            // Update the controller's timeout if necessary.
            showController()
        }
    }

    /**
     * Returns whether the playback controls are hidden by touch events.
     */
    @UnstableApi
    fun getControllerHideOnTouch(): Boolean {
        return controllerHideOnTouch
    }

    /**
     * Sets whether the playback controls are hidden by touch events.
     *
     * @param controllerHideOnTouch Whether the playback controls are hidden by touch events.
     */
    @UnstableApi
    fun setControllerHideOnTouch(controllerHideOnTouch: Boolean) {
        Assertions.checkStateNotNull(controller)
        this.controllerHideOnTouch = controllerHideOnTouch
        updateContentDescription()
    }

    /**
     * Sets whether the playback controls are hidden when ads are playing. Controls are always shown
     * during ads if they are enabled and the player is paused.
     *
     * @param controllerHideDuringAds Whether the playback controls are hidden when ads are playing.
     */
    @UnstableApi
    fun setControllerHideDuringAds(controllerHideDuringAds: Boolean) {
        this.controllerHideDuringAds = controllerHideDuringAds
    }

    /**
     * Sets the [PlayerControlView.VisibilityListener].
     *
     *
     * If `listener` is non-null then any listener set by [ ][.setControllerVisibilityListener] is removed.
     *
     * @param listener The listener to be notified about visibility changes, or null to remove the
     * current listener.
     */
    // Clearing the legacy listener.
    fun setControllerVisibilityListener(listener: PlayerView.ControllerVisibilityListener?) {
        controllerVisibilityListener = listener
        if (listener != null) {
            setControllerVisibilityListener(null as PlayerControlView.VisibilityListener?)
        }
    }

    /**
     * Sets the [PlayerControlView.VisibilityListener].
     *
     *
     * If `listener` is non-null then any listener set by [ ][.setControllerVisibilityListener] is removed.
     *
     */
    @UnstableApi
    @Deprecated("Use {@link #setControllerVisibilityListener(androidx.media3.ui.PlayerView.ControllerVisibilityListener)} instead.")
    fun setControllerVisibilityListener(
        listener: PlayerControlView.VisibilityListener?
    ) {
        Assertions.checkStateNotNull(controller)
        if (legacyControllerVisibilityListener === listener) {
            return
        }
        if (legacyControllerVisibilityListener != null) {
            controller!!.removeVisibilityListener(legacyControllerVisibilityListener!!)
        }
        legacyControllerVisibilityListener = listener
        if (listener != null) {
            controller!!.addVisibilityListener(listener)
            setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?)
        }
    }

    /**
     * Sets the [androidx.media3.ui.PlayerView.FullscreenButtonClickListener].
     *
     *
     * Clears any listener set by [ ][.setControllerOnFullScreenModeChangedListener].
     *
     * @param listener The listener to be notified when the fullscreen button is clicked, or null to
     * remove the current listener and hide the fullscreen button.
     */
    // Calling the deprecated method on PlayerControlView for now.
    fun setFullscreenButtonClickListener(listener: PlayerView.FullscreenButtonClickListener?) {
        Assertions.checkStateNotNull(controller)
        fullscreenButtonClickListener = listener
        controller!!.setOnFullScreenModeChangedListener(componentListener)
    }

    /**
     * Sets the [PlayerControlView.OnFullScreenModeChangedListener].
     *
     *
     * Clears any listener set by [ ][.setFullscreenButtonClickListener].
     *
     * @param listener The listener to be notified when the fullscreen button is clicked, or null to
     * remove the current listener and hide the fullscreen button.
     */
    @UnstableApi
    @Deprecated(
        """Use {@link #setFullscreenButtonClickListener(androidx.media3.ui.PlayerView.FullscreenButtonClickListener)}
      instead."""
    )
    fun setControllerOnFullScreenModeChangedListener(
        listener: PlayerControlView.OnFullScreenModeChangedListener?
    ) {
        Assertions.checkStateNotNull(controller)
        fullscreenButtonClickListener = null
        controller!!.setOnFullScreenModeChangedListener(listener)
    }

    /**
     * Sets whether the rewind button is shown.
     *
     * @param showRewindButton Whether the rewind button is shown.
     */
    @UnstableApi
    fun setShowRewindButton(showRewindButton: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.setShowRewindButton(showRewindButton)
    }

    /**
     * Sets whether the fast forward button is shown.
     *
     * @param showFastForwardButton Whether the fast forward button is shown.
     */
    @UnstableApi
    fun setShowFastForwardButton(showFastForwardButton: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.setShowFastForwardButton(showFastForwardButton)
    }

    /**
     * Sets whether the previous button is shown.
     *
     * @param showPreviousButton Whether the previous button is shown.
     */
    @UnstableApi
    fun setShowPreviousButton(showPreviousButton: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.setShowPreviousButton(showPreviousButton)
    }

    /**
     * Sets whether the next button is shown.
     *
     * @param showNextButton Whether the next button is shown.
     */
    @UnstableApi
    fun setShowNextButton(showNextButton: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.setShowNextButton(showNextButton)
    }

    /**
     * Sets which repeat toggle modes are enabled.
     *
     * @param repeatToggleModes A set of [RepeatModeUtil.RepeatToggleModes].
     */
    @UnstableApi
    fun setRepeatToggleModes(repeatToggleModes: @RepeatToggleModes Int) {
        Assertions.checkStateNotNull(controller)
        controller!!.setRepeatToggleModes(repeatToggleModes)
    }

    /**
     * Sets whether the shuffle button is shown.
     *
     * @param showShuffleButton Whether the shuffle button is shown.
     */
    @UnstableApi
    fun setShowShuffleButton(showShuffleButton: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.showShuffleButton = showShuffleButton
    }

    /**
     * Sets whether the subtitle button is shown.
     *
     * @param showSubtitleButton Whether the subtitle button is shown.
     */
    @UnstableApi
    fun setShowSubtitleButton(showSubtitleButton: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.showSubtitleButton = showSubtitleButton
    }

    /**
     * Sets whether the vr button is shown.
     *
     * @param showVrButton Whether the vr button is shown.
     */
    @UnstableApi
    fun setShowVrButton(showVrButton: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.showVrButton = showVrButton
    }

    /**
     * Sets whether the time bar should show all windows, as opposed to just the current one.
     *
     * @param showMultiWindowTimeBar Whether to show all windows.
     */
    @UnstableApi
    fun setShowMultiWindowTimeBar(showMultiWindowTimeBar: Boolean) {
        Assertions.checkStateNotNull(controller)
        controller!!.setShowMultiWindowTimeBar(showMultiWindowTimeBar)
    }

    /**
     * Sets the millisecond positions of extra ad markers relative to the start of the window (or
     * timeline, if in multi-window mode) and whether each extra ad has been played or not. The
     * markers are shown in addition to any ad markers for ads in the player's timeline.
     *
     * @param extraAdGroupTimesMs The millisecond timestamps of the extra ad markers to show, or
     * `null` to show no extra ad markers.
     * @param extraPlayedAdGroups Whether each ad has been played, or `null` to show no extra ad
     * markers.
     */
    @UnstableApi
    fun setExtraAdGroupMarkers(
        extraAdGroupTimesMs: LongArray?, extraPlayedAdGroups: BooleanArray?
    ) {
        Assertions.checkStateNotNull(controller)
        controller!!.setExtraAdGroupMarkers(extraAdGroupTimesMs, extraPlayedAdGroups)
    }

    /**
     * Sets the [AspectRatioFrameLayout.AspectRatioListener].
     *
     * @param listener The listener to be notified about aspect ratios changes of the video content or
     * the content frame.
     */
    @UnstableApi
    fun setAspectRatioListener(
        listener: AspectRatioListener?
    ) {
        Assertions.checkStateNotNull(contentFrame)
        contentFrame!!.setAspectRatioListener(listener)
    }

    override fun performClick(): Boolean {
        toggleControllerVisibility()
        return super.performClick()
    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        if (!useController() || player == null) {
            return false
        }
        maybeShowController(true)
        return true
    }

    /**
     * Should be called when the player is visible to the user, if the `surface_type` extends
     * [GLSurfaceView]. It is the counterpart to [.onPause].
     *
     *
     * This method should typically be called in `Activity.onStart()`, or `Activity.onResume()` for API versions &lt;= 23.
     */
    fun onResume() {
        if (videoSurfaceView is GLSurfaceView) {
            (videoSurfaceView as GLSurfaceView).onResume()
        }
    }

    /**
     * Should be called when the player is no longer visible to the user, if the `surface_type`
     * extends [GLSurfaceView]. It is the counterpart to [.onResume].
     *
     *
     * This method should typically be called in `Activity.onStop()`, or `Activity.onPause()` for API versions &lt;= 23.
     */
    fun onPause() {
        if (videoSurfaceView is GLSurfaceView) {
            (videoSurfaceView as GLSurfaceView).onPause()
        }
    }

    /**
     * Called when there's a change in the desired aspect ratio of the content frame. The default
     * implementation sets the aspect ratio of the content frame to the specified value.
     *
     * @param contentFrame The content frame, or `null`.
     * @param aspectRatio  The aspect ratio to apply.
     */
    @UnstableApi
    protected fun onContentAspectRatioChanged(
        contentFrame: AspectRatioFrameLayout?, aspectRatio: Float
    ) {
        contentFrame?.setAspectRatio(aspectRatio)
    }

    // AdsLoader.AdViewProvider implementation.
    override fun getAdViewGroup(): ViewGroup {
        return Assertions.checkStateNotNull(
            adOverlayFrameLayout, "exo_ad_overlay must be present for ad playback"
        )
    }

    override fun getAdOverlayInfos(): List<AdOverlayInfo> {
        val overlayViews: MutableList<AdOverlayInfo> = ArrayList()
        if (overlayFrameLayout != null) {
            overlayViews.add(
                AdOverlayInfo(
                    overlayFrameLayout!!, AdOverlayInfo.PURPOSE_NOT_VISIBLE,  /* detailedReason= */
                    "Transparent overlay does not impact viewability"
                )
            )
        }
        if (controller != null) {
            overlayViews.add(AdOverlayInfo(controller!!, AdOverlayInfo.PURPOSE_CONTROLS))
        }
        return ImmutableList.copyOf(overlayViews)
    }

    // Internal methods.
    @EnsuresNonNullIf(expression = ["controller"], result = true)
    private fun useController(): Boolean {
        if (useController) {
            Assertions.checkStateNotNull(controller)
            return true
        }
        return false
    }

    @EnsuresNonNullIf(expression = ["artworkView"], result = true)
    private fun useArtwork(): Boolean {
        if (useArtwork) {
            Assertions.checkStateNotNull(artworkView)
            return true
        }
        return false
    }

    private fun toggleControllerVisibility() {
        if (!useController() || player == null) {
            return
        }
        if (!controller!!.isFullyVisible) {
            maybeShowController(true)
        } else if (controllerHideOnTouch) {
            controller!!.hide()
        }
    }

    /**
     * Shows the playback controls, but only if forced or shown indefinitely.
     */
    private fun maybeShowController(isForced: Boolean) {
        if (isPlayingAd && controllerHideDuringAds) {
            return
        }
        if (useController()) {
            val wasShowingIndefinitely =
                controller!!.isFullyVisible && controller!!.getShowTimeoutMs() <= 0
            val shouldShowIndefinitely = shouldShowControllerIndefinitely()
            if (isForced || wasShowingIndefinitely || shouldShowIndefinitely) {
                showController(shouldShowIndefinitely)
            }
        }
    }

    private fun shouldShowControllerIndefinitely(): Boolean {
        if (player == null) {
            return true
        }
        val playbackState = player!!.playbackState
        return (controllerAutoShow && (!player!!.isCommandAvailable(Player.COMMAND_GET_TIMELINE) || !player!!.currentTimeline.isEmpty) && (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED || !Assertions.checkNotNull(
            player
        ).playWhenReady))
    }

    private fun showController(showIndefinitely: Boolean) {
        if (!useController()) {
            return
        }
        controller!!.setShowTimeoutMs(if (showIndefinitely) 0 else controllerShowTimeoutMs)
        controller!!.show()
    }

    private val isPlayingAd: Boolean
        get() = (player != null && player!!.isCommandAvailable(Player.COMMAND_GET_CURRENT_MEDIA_ITEM) && player!!.isPlayingAd && player!!.playWhenReady)

    private fun updateForCurrentTrackSelections(isNewPlayer: Boolean) {
        val player = player
        if (player == null || !player.isCommandAvailable(Player.COMMAND_GET_TRACKS) || player.currentTracks.isEmpty) {
            if (!keepContentOnPlayerReset) {
                hideArtwork()
                closeShutter()
            }
            return
        }
        if (isNewPlayer && !keepContentOnPlayerReset) {
            // Hide any video from the previous player.
            closeShutter()
        }
        if (player.currentTracks.isTypeSelected(C.TRACK_TYPE_VIDEO)) {
            // Video enabled, so artwork must be hidden. If the shutter is closed, it will be opened
            // in onRenderedFirstFrame().
            hideArtwork()
            return
        }

        // Video disabled so the shutter must be closed.
        closeShutter()
        // Display artwork if enabled and available, else hide it.
        if (useArtwork()) {
            if (setArtworkFromMediaMetadata(player)) {
                return
            }
            if (setDrawableArtwork(defaultArtwork)) {
                return
            }
        }
        // Artwork disabled or unavailable.
        hideArtwork()
    }

    @RequiresNonNull("artworkView")
    private fun setArtworkFromMediaMetadata(player: Player): Boolean {
        if (!player.isCommandAvailable(Player.COMMAND_GET_MEDIA_ITEMS_METADATA)) {
            return false
        }
        val mediaMetadata = player.mediaMetadata
        if (mediaMetadata.artworkData == null) {
            return false
        }
        val bitmap = BitmapFactory.decodeByteArray(
            mediaMetadata.artworkData,  /* offset= */0, mediaMetadata.artworkData!!.size
        )
        return setDrawableArtwork(BitmapDrawable(resources, bitmap))
    }

    @RequiresNonNull("artworkView")
    private fun setDrawableArtwork(drawable: Drawable?): Boolean {
        if (drawable != null) {
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight
            if (drawableWidth > 0 && drawableHeight > 0) {
                val artworkAspectRatio = drawableWidth.toFloat() / drawableHeight
                onContentAspectRatioChanged(contentFrame, artworkAspectRatio)
                artworkView!!.setImageDrawable(drawable)
                artworkView!!.visibility = VISIBLE
                return true
            }
        }
        return false
    }

    private fun hideArtwork() {
        if (artworkView != null) {
            artworkView?.setImageResource(android.R.color.transparent) // Clears any bitmap reference.
            artworkView?.visibility = INVISIBLE
        }
    }

    private fun closeShutter() {
        if (shutterView != null) {
            shutterView?.visibility = VISIBLE
        }
    }

    private fun updateBuffering() {
        if (bufferingView != null) {
            val showBufferingSpinner =
                player != null && player!!.playbackState == Player.STATE_BUFFERING && (showBuffering == SHOW_BUFFERING_ALWAYS || showBuffering == SHOW_BUFFERING_WHEN_PLAYING && player!!.playWhenReady)
            bufferingView?.visibility = if (showBufferingSpinner) VISIBLE else GONE
        }
    }

    private fun updateErrorMessage() {
        if (errorMessageView != null) {
            if (customErrorMessage != null) {
                errorMessageView?.text = customErrorMessage
                errorMessageView?.visibility = VISIBLE
                return
            }
            val error = if (player != null) player!!.playerError else null
            if (error != null && errorMessageProvider != null) {
                val errorMessage: CharSequence =
                    errorMessageProvider!!.getErrorMessage(error).second
                errorMessageView?.text = errorMessage
                errorMessageView?.visibility = VISIBLE
            } else {
                errorMessageView?.visibility = GONE
            }
        }
    }

    private fun updateContentDescription() {
        contentDescription = if (controller == null || !useController) {
            null
        } else if (controller?.isFullyVisible == true) {
            if (controllerHideOnTouch) resources.getString(R.string.exo_controls_hide) else null
        } else {
            resources.getString(R.string.exo_controls_show)
        }
    }

    private fun updateControllerVisibility() {
        if (isPlayingAd && controllerHideDuringAds) {
            hideController()
        } else {
            maybeShowController(false)
        }
    }

    private fun updateAspectRatio() {
        val videoSize = if (player != null) player!!.videoSize else VideoSize.UNKNOWN
        val width = videoSize.width
        val height = videoSize.height
        val unappliedRotationDegrees = videoSize.unappliedRotationDegrees
        var videoAspectRatio: Float =
            if (height == 0 || width == 0) 0F else width * videoSize.pixelWidthHeightRatio / height
        if (videoSurfaceView is TextureView) {
            // Try to apply rotation transformation when our surface is a TextureView.
            if (videoAspectRatio > 0 && (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270)) {
                // We will apply a rotation 90/270 degree to the output texture of the TextureView.
                // In this case, the output video's width and height will be swapped.
                videoAspectRatio = 1 / videoAspectRatio
            }
            if (textureViewRotation != 0) {
                videoSurfaceView?.removeOnLayoutChangeListener(componentListener)
            }
            textureViewRotation = unappliedRotationDegrees
            if (textureViewRotation != 0) {
                // The texture view's dimensions might be changed after layout step.
                // So add an OnLayoutChangeListener to apply rotation after layout step.
                videoSurfaceView?.addOnLayoutChangeListener(componentListener)
            }
            applyTextureViewRotation(
                videoSurfaceView as TextureView, textureViewRotation
            )
        }
        onContentAspectRatioChanged(
            contentFrame, if (surfaceViewIgnoresVideoAspectRatio) 0F else videoAspectRatio
        )
    }

    @SuppressLint("InlinedApi")
    private fun isDpadKey(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_UP_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_RIGHT || keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_DOWN_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
    }

    // Implementing the deprecated PlayerControlView.VisibilityListener and
    // PlayerControlView.OnFullScreenModeChangedListener for now.
    private inner class ComponentListener : Player.Listener, OnLayoutChangeListener,
        OnClickListener, PlayerControlView.VisibilityListener,
        PlayerControlView.OnFullScreenModeChangedListener {
        private val period: Timeline.Period = Timeline.Period()
        private var lastPeriodUidWithTracks: Any? = null

        // Player.Listener implementation
        override fun onCues(cueGroup: CueGroup) {
            if (subtitleView != null) {
                subtitleView!!.setCues(cueGroup.cues)
            }
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            updateAspectRatio()
        }

        override fun onRenderedFirstFrame() {
            if (shutterView != null) {
                shutterView?.visibility = INVISIBLE
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            // Suppress the update if transitioning to an unprepared period within the same window. This
            // is necessary to avoid closing the shutter when such a transition occurs. See:
            // https://github.com/google/ExoPlayer/issues/5507.
            val player = Assertions.checkNotNull(player)
            val timeline =
                if (player.isCommandAvailable(Player.COMMAND_GET_TIMELINE)) player.currentTimeline else Timeline.EMPTY
            if (timeline.isEmpty) {
                lastPeriodUidWithTracks = null
            } else if (player.isCommandAvailable(Player.COMMAND_GET_TRACKS) && !player.currentTracks.isEmpty) {
                lastPeriodUidWithTracks =
                    timeline.getPeriod(player.currentPeriodIndex, period,  /* setIds= */true).uid
            } else if (lastPeriodUidWithTracks != null) {
                val lastPeriodIndexWithTracks = timeline.getIndexOfPeriod(
                    lastPeriodUidWithTracks!!
                )
                if (lastPeriodIndexWithTracks != C.INDEX_UNSET) {
                    val lastWindowIndexWithTracks =
                        timeline.getPeriod(lastPeriodIndexWithTracks, period).windowIndex
                    if (player.currentMediaItemIndex == lastWindowIndexWithTracks) {
                        // We're in the same media item. Suppress the update.
                        return
                    }
                }
                lastPeriodUidWithTracks = null
            }
            updateForCurrentTrackSelections( /* isNewPlayer= */false)
        }

        override fun onPlaybackStateChanged(playbackState: @Player.State Int) {
            updateBuffering()
            updateErrorMessage()
            updateControllerVisibility()
        }

        override fun onPlayWhenReadyChanged(
            playWhenReady: Boolean, reason: @PlayWhenReadyChangeReason Int
        ) {
            updateBuffering()
            updateControllerVisibility()
        }

        override fun onPositionDiscontinuity(
            oldPosition: PositionInfo, newPosition: PositionInfo, reason: @DiscontinuityReason Int
        ) {
            if (isPlayingAd && controllerHideDuringAds) {
                hideController()
            }
        }

        // OnLayoutChangeListener implementation
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            applyTextureViewRotation(view as TextureView, textureViewRotation)
        }

        // OnClickListener implementation
        override fun onClick(view: View) {
            toggleControllerVisibility()
        }

        // PlayerControlView.VisibilityListener implementation
        override fun onVisibilityChange(visibility: Int) {
            updateContentDescription()
            if (controllerVisibilityListener != null) {
                controllerVisibilityListener!!.onVisibilityChanged(visibility)
            }
        }

        // PlayerControlView.OnFullScreenModeChangedListener implementation
        override fun onFullScreenModeChanged(isFullScreen: Boolean) {
            if (fullscreenButtonClickListener != null) {
                fullscreenButtonClickListener!!.onFullscreenButtonClick(isFullScreen)
            }
        }
    }

    companion object {
        /**
         * The buffering view is never shown.
         */
        @UnstableApi
        const val SHOW_BUFFERING_NEVER = 0

        /**
         * The buffering view is shown when the player is in the [buffering][Player.STATE_BUFFERING]
         * state and [playWhenReady][Player.getPlayWhenReady] is `true`.
         */
        @UnstableApi
        const val SHOW_BUFFERING_WHEN_PLAYING = 1

        /**
         * The buffering view is always shown when the player is in the [ buffering][Player.STATE_BUFFERING] state.
         */
        @UnstableApi
        const val SHOW_BUFFERING_ALWAYS = 2
        private const val SURFACE_TYPE_NONE = 0
        private const val SURFACE_TYPE_SURFACE_VIEW = 1
        private const val SURFACE_TYPE_TEXTURE_VIEW = 2
        private const val SURFACE_TYPE_SPHERICAL_GL_SURFACE_VIEW = 3
        private const val SURFACE_TYPE_VIDEO_DECODER_GL_SURFACE_VIEW = 4

        /**
         * Switches the view targeted by a given [Player].
         *
         * @param player        The player whose target view is being switched.
         * @param oldPlayerView The old view to detach from the player.
         * @param newPlayerView The new view to attach to the player.
         */
        @UnstableApi
        fun switchTargetView(
            player: Player?, oldPlayerView: PlayerView?, newPlayerView: PlayerView?
        ) {
            if (oldPlayerView === newPlayerView) {
                return
            }
            // We attach the new view before detaching the old one because this ordering allows the player
            // to swap directly from one surface to another, without transitioning through a state where no
            // surface is attached. This is significantly more efficient and achieves a more seamless
            // transition when using platform provided video decoders.
            if (newPlayerView != null) {
                newPlayerView.player = player
            }
            if (oldPlayerView != null) {
                oldPlayerView.player = null
            }
        }

        @RequiresApi(23)
        private fun configureEditModeLogoV23(
            context: Context, resources: Resources, logo: ImageView
        ) {
            logo.setImageDrawable(
                Util.getDrawable(
                    context, resources, R.drawable.exo_edit_mode_logo
                )
            )
            logo.setBackgroundColor(
                resources.getColor(
                    R.color.exo_edit_mode_background_color, null
                )
            )
        }

        private fun configureEditModeLogo(context: Context, resources: Resources, logo: ImageView) {
            logo.setImageDrawable(
                Util.getDrawable(
                    context, resources, R.drawable.exo_edit_mode_logo
                )
            )
            logo.setBackgroundColor(resources.getColor(R.color.exo_edit_mode_background_color))
        }

        private fun setResizeModeRaw(aspectRatioFrame: AspectRatioFrameLayout, resizeMode: Int) {
            aspectRatioFrame.resizeMode = resizeMode
        }

        /**
         * Applies a texture rotation to a [TextureView].
         */
        private fun applyTextureViewRotation(textureView: TextureView, textureViewRotation: Int) {
            val transformMatrix = Matrix()
            val textureViewWidth = textureView.width.toFloat()
            val textureViewHeight = textureView.height.toFloat()
            if (textureViewWidth != 0f && textureViewHeight != 0f && textureViewRotation != 0) {
                val pivotX = textureViewWidth / 2
                val pivotY = textureViewHeight / 2
                transformMatrix.postRotate(textureViewRotation.toFloat(), pivotX, pivotY)

                // After rotation, scale the rotated texture to fit the TextureView size.
                val originalTextureRect = RectF(0f, 0f, textureViewWidth, textureViewHeight)
                val rotatedTextureRect = RectF()
                transformMatrix.mapRect(rotatedTextureRect, originalTextureRect)
                transformMatrix.postScale(
                    textureViewWidth / rotatedTextureRect.width(),
                    textureViewHeight / rotatedTextureRect.height(),
                    pivotX,
                    pivotY
                )
            }
            textureView.setTransform(transformMatrix)
        }
    }
}