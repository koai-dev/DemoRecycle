package com.idance.hocnhayonline.customView.player

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.LinearInterpolator
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.R

/*
 * Copyright 2020 The Android Open Source Project
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
 */@UnstableApi
internal class PlayerControlViewLayoutManager(private val playerControlView: PlayerControlView) {
    private val controlsBackground: View?
    private val centerControls: ViewGroup?
    private val bottomBar: ViewGroup?
    private val minimalControls: ViewGroup?
    private val basicControls: ViewGroup?
    private val extraControls: ViewGroup?
    private val extraControlsScrollView: ViewGroup?
    private val timeView: ViewGroup?
    private val timeBar: View?
    private val overflowShowButton: View?
    private val hideMainBarAnimator: AnimatorSet
    private val hideProgressBarAnimator: AnimatorSet
    private val hideAllBarsAnimator: AnimatorSet
    private val showMainBarAnimator: AnimatorSet
    private val showAllBarsAnimator: AnimatorSet
    private val overflowShowAnimator: ValueAnimator
    private val overflowHideAnimator: ValueAnimator
    private val showAllBarsRunnable: Runnable
    private val hideAllBarsRunnable: Runnable
    private val hideProgressBarRunnable: Runnable
    private val hideMainBarRunnable: Runnable
    private val hideControllerRunnable: Runnable
    private val onLayoutChangeListener: OnLayoutChangeListener
    private val shownButtons: MutableList<View>
    private var uxState: Int
    private var isMinimalMode = false
    private var needToShowBars = false
    var isAnimationEnabled: Boolean

    init {
        showAllBarsRunnable = Runnable { showAllBars() }
        hideAllBarsRunnable = Runnable { hideAllBars() }
        hideProgressBarRunnable = Runnable { hideProgressBar() }
        hideMainBarRunnable = Runnable { hideMainBar() }
        hideControllerRunnable = Runnable { hideController() }
        onLayoutChangeListener =
            OnLayoutChangeListener { v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
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
        isAnimationEnabled = true
        uxState = UX_STATE_ALL_VISIBLE
        shownButtons = ArrayList()

        // Relating to Center View
        controlsBackground = playerControlView.findViewById(R.id.exo_controls_background)
        centerControls = playerControlView.findViewById(R.id.exo_center_controls)

        // Relating to Minimal Layout
        minimalControls = playerControlView.findViewById(R.id.exo_minimal_controls)

        // Relating to Bottom Bar View
        bottomBar = playerControlView.findViewById(R.id.exo_bottom_bar)

        // Relating to Bottom Bar Left View
        timeView = playerControlView.findViewById(R.id.exo_time)
        timeBar = playerControlView.findViewById(R.id.exo_progress)

        // Relating to Bottom Bar Right View
        basicControls = playerControlView.findViewById(R.id.exo_basic_controls)
        extraControls = playerControlView.findViewById(R.id.exo_extra_controls)
        extraControlsScrollView =
            playerControlView.findViewById(R.id.exo_extra_controls_scroll_view)
        overflowShowButton = playerControlView.findViewById(R.id.exo_overflow_show)
        val overflowHideButton = playerControlView.findViewById<View>(R.id.exo_overflow_hide)
        if (overflowShowButton != null && overflowHideButton != null) {
            overflowShowButton.setOnClickListener(View.OnClickListener { v: View ->
                onOverflowButtonClick(
                    v
                )
            })
            overflowHideButton.setOnClickListener { v: View -> onOverflowButtonClick(v) }
        }
        val fadeOutAnimator = ValueAnimator.ofFloat(1.0f, 0.0f)
        fadeOutAnimator.interpolator = LinearInterpolator()
        fadeOutAnimator.addUpdateListener { animation: ValueAnimator ->
            val animatedValue = animation.animatedValue as Float
            if (controlsBackground != null) {
                controlsBackground.alpha = animatedValue
            }
            if (centerControls != null) {
                centerControls.alpha = animatedValue
            }
            if (minimalControls != null) {
                minimalControls.alpha = animatedValue
            }
        }
        fadeOutAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    if (timeBar is DefaultTimeBar && !isMinimalMode) {
                        timeBar.hideScrubber(DURATION_FOR_HIDING_ANIMATION_MS)
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (controlsBackground != null) {
                        controlsBackground.visibility = View.INVISIBLE
                    }
                    if (centerControls != null) {
                        centerControls.visibility = View.INVISIBLE
                    }
                    if (minimalControls != null) {
                        minimalControls.visibility = View.INVISIBLE
                    }
                }
            })
        val fadeInAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        fadeInAnimator.interpolator = LinearInterpolator()
        fadeInAnimator.addUpdateListener { animation: ValueAnimator ->
            val animatedValue = animation.animatedValue as Float
            if (controlsBackground != null) {
                controlsBackground.alpha = animatedValue
            }
            if (centerControls != null) {
                centerControls.alpha = animatedValue
            }
            if (minimalControls != null) {
                minimalControls.alpha = animatedValue
            }
        }
        fadeInAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    if (controlsBackground != null) {
                        controlsBackground.visibility = View.VISIBLE
                    }
                    if (centerControls != null) {
                        centerControls.visibility = View.VISIBLE
                    }
                    if (minimalControls != null) {
                        minimalControls.visibility =
                            if (isMinimalMode) View.VISIBLE else View.INVISIBLE
                    }
                    if (timeBar is DefaultTimeBar && !isMinimalMode) {
                        timeBar.showScrubber(DURATION_FOR_SHOWING_ANIMATION_MS)
                    }
                }
            })
        val resources = playerControlView.resources
        val translationYForProgressBar =
            (resources.getDimension(R.dimen.exo_styled_bottom_bar_height)
                    - resources.getDimension(R.dimen.exo_styled_progress_bar_height))
        val translationYForNoBars = resources.getDimension(R.dimen.exo_styled_bottom_bar_height)
        hideMainBarAnimator = AnimatorSet()
        hideMainBarAnimator.duration = DURATION_FOR_HIDING_ANIMATION_MS
        hideMainBarAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    setUxState(UX_STATE_ANIMATING_HIDE)
                }

                override fun onAnimationEnd(animation: Animator) {
                    setUxState(UX_STATE_ONLY_PROGRESS_VISIBLE)
                    if (needToShowBars) {
                        playerControlView.post(showAllBarsRunnable)
                        needToShowBars = false
                    }
                }
            })
        hideMainBarAnimator
            .play(fadeOutAnimator)
            .with(ofTranslationY(0f, translationYForProgressBar, timeBar))
            .with(ofTranslationY(0f, translationYForProgressBar, bottomBar))
        hideProgressBarAnimator = AnimatorSet()
        hideProgressBarAnimator.duration = DURATION_FOR_HIDING_ANIMATION_MS
        hideProgressBarAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    setUxState(UX_STATE_ANIMATING_HIDE)
                }

                override fun onAnimationEnd(animation: Animator) {
                    setUxState(UX_STATE_NONE_VISIBLE)
                    if (needToShowBars) {
                        playerControlView.post(showAllBarsRunnable)
                        needToShowBars = false
                    }
                }
            })
        hideProgressBarAnimator
            .play(ofTranslationY(translationYForProgressBar, translationYForNoBars, timeBar))
            .with(ofTranslationY(translationYForProgressBar, translationYForNoBars, bottomBar))
        hideAllBarsAnimator = AnimatorSet()
        hideAllBarsAnimator.duration = DURATION_FOR_HIDING_ANIMATION_MS
        hideAllBarsAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    setUxState(UX_STATE_ANIMATING_HIDE)
                }

                override fun onAnimationEnd(animation: Animator) {
                    setUxState(UX_STATE_NONE_VISIBLE)
                    if (needToShowBars) {
                        playerControlView.post(showAllBarsRunnable)
                        needToShowBars = false
                    }
                }
            })
        hideAllBarsAnimator
            .play(fadeOutAnimator)
            .with(ofTranslationY(0f, translationYForNoBars, timeBar))
            .with(ofTranslationY(0f, translationYForNoBars, bottomBar))
        showMainBarAnimator = AnimatorSet()
        showMainBarAnimator.duration = DURATION_FOR_SHOWING_ANIMATION_MS
        showMainBarAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    setUxState(UX_STATE_ANIMATING_SHOW)
                }

                override fun onAnimationEnd(animation: Animator) {
                    setUxState(UX_STATE_ALL_VISIBLE)
                }
            })
        showMainBarAnimator
            .play(fadeInAnimator)
            .with(ofTranslationY(translationYForProgressBar, 0f, timeBar))
            .with(ofTranslationY(translationYForProgressBar, 0f, bottomBar))
        showAllBarsAnimator = AnimatorSet()
        showAllBarsAnimator.duration = DURATION_FOR_SHOWING_ANIMATION_MS
        showAllBarsAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    setUxState(UX_STATE_ANIMATING_SHOW)
                }

                override fun onAnimationEnd(animation: Animator) {
                    setUxState(UX_STATE_ALL_VISIBLE)
                }
            })
        showAllBarsAnimator
            .play(fadeInAnimator)
            .with(ofTranslationY(translationYForNoBars, 0f, timeBar))
            .with(ofTranslationY(translationYForNoBars, 0f, bottomBar))
        overflowShowAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
        overflowShowAnimator.duration = DURATION_FOR_SHOWING_ANIMATION_MS
        overflowShowAnimator.addUpdateListener { animation: ValueAnimator ->
            animateOverflow(
                animation.animatedValue as Float
            )
        }
        overflowShowAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    if (extraControlsScrollView != null) {
                        extraControlsScrollView.visibility = View.VISIBLE
                        extraControlsScrollView.translationX =
                            extraControlsScrollView.width.toFloat()
                        extraControlsScrollView.scrollTo(extraControlsScrollView.width, 0)
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (basicControls != null) {
                        basicControls.visibility = View.INVISIBLE
                    }
                }
            })
        overflowHideAnimator = ValueAnimator.ofFloat(1.0f, 0.0f)
        overflowHideAnimator.duration = DURATION_FOR_SHOWING_ANIMATION_MS
        overflowHideAnimator.addUpdateListener { animation: ValueAnimator ->
            animateOverflow(
                animation.animatedValue as Float
            )
        }
        overflowHideAnimator.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    if (basicControls != null) {
                        basicControls.visibility = View.VISIBLE
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (extraControlsScrollView != null) {
                        extraControlsScrollView.visibility = View.INVISIBLE
                    }
                }
            })
    }

    fun show() {
        if (!playerControlView.isVisible) {
            playerControlView.visibility = View.VISIBLE
            playerControlView.updateAll()
            playerControlView.requestPlayPauseFocus()
        }
        showAllBars()
    }

    fun hide() {
        if (uxState == UX_STATE_ANIMATING_HIDE || uxState == UX_STATE_NONE_VISIBLE) {
            return
        }
        removeHideCallbacks()
        if (!isAnimationEnabled) {
            hideController()
        } else if (uxState == UX_STATE_ONLY_PROGRESS_VISIBLE) {
            hideProgressBar()
        } else {
            hideAllBars()
        }
    }

    fun hideImmediately() {
        if (uxState == UX_STATE_ANIMATING_HIDE || uxState == UX_STATE_NONE_VISIBLE) {
            return
        }
        removeHideCallbacks()
        hideController()
    }

    fun resetHideCallbacks() {
        if (uxState == UX_STATE_ANIMATING_HIDE) {
            return
        }
        removeHideCallbacks()
        val showTimeoutMs = playerControlView.getShowTimeoutMs()
        if (showTimeoutMs > 0) {
            if (!isAnimationEnabled) {
                postDelayedRunnable(hideControllerRunnable, showTimeoutMs.toLong())
            } else if (uxState == UX_STATE_ONLY_PROGRESS_VISIBLE) {
                postDelayedRunnable(hideProgressBarRunnable, ANIMATION_INTERVAL_MS)
            } else {
                postDelayedRunnable(hideMainBarRunnable, showTimeoutMs.toLong())
            }
        }
    }

    fun removeHideCallbacks() {
        playerControlView.removeCallbacks(hideControllerRunnable)
        playerControlView.removeCallbacks(hideAllBarsRunnable)
        playerControlView.removeCallbacks(hideMainBarRunnable)
        playerControlView.removeCallbacks(hideProgressBarRunnable)
    }

    fun onAttachedToWindow() {
        playerControlView.addOnLayoutChangeListener(onLayoutChangeListener)
    }

    fun onDetachedFromWindow() {
        playerControlView.removeOnLayoutChangeListener(onLayoutChangeListener)
    }

    val isFullyVisible: Boolean
        get() = uxState == UX_STATE_ALL_VISIBLE && playerControlView.isVisible

    fun setShowButton(button: View?, showButton: Boolean) {
        if (button == null) {
            return
        }
        if (!showButton) {
            button.visibility = View.GONE
            shownButtons.remove(button)
            return
        }
        if (isMinimalMode && shouldHideInMinimalMode(button)) {
            button.visibility = View.INVISIBLE
        } else {
            button.visibility = View.VISIBLE
        }
        shownButtons.add(button)
    }

    fun getShowButton(button: View?): Boolean {
        return button != null && shownButtons.contains(button)
    }

    private fun setUxState(uxState: Int) {
        val prevUxState = this.uxState
        this.uxState = uxState
        if (uxState == UX_STATE_NONE_VISIBLE) {
            playerControlView.visibility = View.GONE
        } else if (prevUxState == UX_STATE_NONE_VISIBLE) {
            playerControlView.visibility = View.VISIBLE
        }
        // TODO(insun): Notify specific uxState. Currently reuses legacy visibility listener for API
        //  compatibility.
        if (prevUxState != uxState) {
            playerControlView.notifyOnVisibilityChange()
        }
    }

    fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        controlsBackground?.layout(0, 0, right - left, bottom - top)
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
        val useMinimalMode = useMinimalMode()
        if (isMinimalMode != useMinimalMode) {
            isMinimalMode = useMinimalMode
            v.post { updateLayoutForSizeChange() }
        }
        val widthChanged = right - left != oldRight - oldLeft
        if (!isMinimalMode && widthChanged) {
            v.post { onLayoutWidthChanged() }
        }
    }

    private fun onOverflowButtonClick(v: View) {
        resetHideCallbacks()
        if (v.id == R.id.exo_overflow_show) {
            overflowShowAnimator.start()
        } else if (v.id == R.id.exo_overflow_hide) {
            overflowHideAnimator.start()
        }
    }

    private fun showAllBars() {
        if (!isAnimationEnabled) {
            setUxState(UX_STATE_ALL_VISIBLE)
            resetHideCallbacks()
            return
        }
        when (uxState) {
            UX_STATE_NONE_VISIBLE -> showAllBarsAnimator.start()
            UX_STATE_ONLY_PROGRESS_VISIBLE -> showMainBarAnimator.start()
            UX_STATE_ANIMATING_HIDE -> needToShowBars = true
            UX_STATE_ANIMATING_SHOW -> return
            else -> {}
        }
        resetHideCallbacks()
    }

    private fun hideAllBars() {
        hideAllBarsAnimator.start()
    }

    private fun hideProgressBar() {
        hideProgressBarAnimator.start()
    }

    private fun hideMainBar() {
        hideMainBarAnimator.start()
        postDelayedRunnable(hideProgressBarRunnable, ANIMATION_INTERVAL_MS)
    }

    private fun hideController() {
        setUxState(UX_STATE_NONE_VISIBLE)
    }

    private fun postDelayedRunnable(runnable: Runnable, interval: Long) {
        if (interval >= 0) {
            playerControlView.postDelayed(runnable, interval)
        }
    }

    private fun animateOverflow(animatedValue: Float) {
        if (extraControlsScrollView != null) {
            val extraControlTranslationX =
                (extraControlsScrollView.width * (1 - animatedValue)).toInt()
            extraControlsScrollView.translationX = extraControlTranslationX.toFloat()
        }
        if (timeView != null) {
            timeView.alpha = 1 - animatedValue
        }
        if (basicControls != null) {
            basicControls.alpha = 1 - animatedValue
        }
    }

    private fun useMinimalMode(): Boolean {
        val width = (playerControlView.width
                - playerControlView.paddingLeft
                - playerControlView.paddingRight)
        val height = (playerControlView.height
                - playerControlView.paddingBottom
                - playerControlView.paddingTop)
        val centerControlWidth = (getWidthWithMargins(centerControls)
                - if (centerControls != null) centerControls.paddingLeft + centerControls.paddingRight else 0)
        val centerControlHeight = (getHeightWithMargins(centerControls)
                - if (centerControls != null) centerControls.paddingTop + centerControls.paddingBottom else 0)
        val defaultModeMinimumWidth = Math.max(
            centerControlWidth,
            getWidthWithMargins(timeView) + getWidthWithMargins(overflowShowButton)
        )
        val defaultModeMinimumHeight = centerControlHeight + 2 * getHeightWithMargins(
            bottomBar
        )
        return width <= defaultModeMinimumWidth || height <= defaultModeMinimumHeight
    }

    private fun updateLayoutForSizeChange() {
        if (minimalControls != null) {
            minimalControls.visibility = if (isMinimalMode) View.VISIBLE else View.INVISIBLE
        }
        if (timeBar != null) {
            val timeBarMarginBottom = playerControlView
                .resources
                .getDimensionPixelSize(R.dimen.exo_styled_progress_margin_bottom)
            val timeBarParams = timeBar.layoutParams as MarginLayoutParams
            if (timeBarParams != null) {
                timeBarParams.bottomMargin = if (isMinimalMode) 0 else timeBarMarginBottom
                timeBar.layoutParams = timeBarParams
            }
            if (timeBar is DefaultTimeBar) {
                val defaultTimeBar = timeBar
                if (isMinimalMode) {
                    defaultTimeBar.hideScrubber( /* disableScrubberPadding= */true)
                } else if (uxState == UX_STATE_ONLY_PROGRESS_VISIBLE) {
                    defaultTimeBar.hideScrubber( /* disableScrubberPadding= */false)
                } else if (uxState != UX_STATE_ANIMATING_HIDE) {
                    defaultTimeBar.showScrubber()
                }
            }
        }
        for (v in shownButtons) {
            v.visibility =
                if (isMinimalMode && shouldHideInMinimalMode(v)) View.INVISIBLE else View.VISIBLE
        }
    }

    private fun shouldHideInMinimalMode(button: View): Boolean {
        val id = button.id
        return id == R.id.exo_bottom_bar || id == R.id.exo_prev || id == R.id.exo_next || id == R.id.exo_rew || id == R.id.exo_rew_with_amount || id == R.id.exo_ffwd || id == R.id.exo_ffwd_with_amount
    }

    private fun onLayoutWidthChanged() {
        if (basicControls == null || extraControls == null) {
            return
        }
        val width = (playerControlView.width
                - playerControlView.paddingLeft
                - playerControlView.paddingRight)

        // Reset back to all controls being basic controls and the overflow not being needed. The last
        // child of extraControls is the overflow hide button, which shouldn't be moved back.
        while (extraControls.childCount > 1) {
            val controlViewIndex = extraControls.childCount - 2
            val controlView = extraControls.getChildAt(controlViewIndex)
            extraControls.removeViewAt(controlViewIndex)
            basicControls.addView(controlView,  /* index= */0)
        }
        if (overflowShowButton != null) {
            overflowShowButton.visibility = View.GONE
        }

        // Calculate how much of the available width is occupied. The last child of basicControls is the
        // overflow show button, which we're currently assuming will not be visible.
        var occupiedWidth = getWidthWithMargins(timeView)
        val endIndex = basicControls.childCount - 1
        for (i in 0 until endIndex) {
            val controlView = basicControls.getChildAt(i)
            occupiedWidth += getWidthWithMargins(controlView)
        }
        if (occupiedWidth > width) {
            // We need to move some controls to extraControls.
            if (overflowShowButton != null) {
                overflowShowButton.visibility = View.VISIBLE
                occupiedWidth += getWidthWithMargins(overflowShowButton)
            }
            val controlsToMove = ArrayList<View>()
            // The last child of basicControls is the overflow show button, which shouldn't be moved.
            for (i in 0 until endIndex) {
                val control = basicControls.getChildAt(i)
                occupiedWidth -= getWidthWithMargins(control)
                controlsToMove.add(control)
                if (occupiedWidth <= width) {
                    break
                }
            }
            if (!controlsToMove.isEmpty()) {
                basicControls.removeViews( /* start= */0, controlsToMove.size)
                for (i in controlsToMove.indices) {
                    // The last child of extraControls is the overflow hide button. Add controls before it.
                    val index = extraControls.childCount - 1
                    extraControls.addView(controlsToMove[i], index)
                }
            }
        } else {
            // If extraControls are visible, hide them since they're now empty.
            if (extraControlsScrollView != null && extraControlsScrollView.visibility == View.VISIBLE && !overflowHideAnimator.isStarted) {
                overflowShowAnimator.cancel()
                overflowHideAnimator.start()
            }
        }
    }

    companion object {
        private const val ANIMATION_INTERVAL_MS: Long = 2000
        private const val DURATION_FOR_HIDING_ANIMATION_MS: Long = 250
        private const val DURATION_FOR_SHOWING_ANIMATION_MS: Long = 250

        // Int for defining the UX state where all the views (ProgressBar, BottomBar) are
        // all visible.
        private const val UX_STATE_ALL_VISIBLE = 0

        // Int for defining the UX state where only the ProgressBar view is visible.
        private const val UX_STATE_ONLY_PROGRESS_VISIBLE = 1

        // Int for defining the UX state where none of the views are visible.
        private const val UX_STATE_NONE_VISIBLE = 2

        // Int for defining the UX state where the views are being animated to be hidden.
        private const val UX_STATE_ANIMATING_HIDE = 3

        // Int for defining the UX state where the views are being animated to be shown.
        private const val UX_STATE_ANIMATING_SHOW = 4
        private fun ofTranslationY(
            startValue: Float,
            endValue: Float,
            target: View?
        ): ObjectAnimator {
            return ObjectAnimator.ofFloat(target, "translationY", startValue, endValue)
        }

        private fun getWidthWithMargins(v: View?): Int {
            if (v == null) {
                return 0
            }
            var width = v.width
            val layoutParams = v.layoutParams
            if (layoutParams is MarginLayoutParams) {
                val marginLayoutParams = layoutParams
                width += marginLayoutParams.leftMargin + marginLayoutParams.rightMargin
            }
            return width
        }

        private fun getHeightWithMargins(v: View?): Int {
            if (v == null) {
                return 0
            }
            var height = v.height
            val layoutParams = v.layoutParams
            if (layoutParams is MarginLayoutParams) {
                height += layoutParams.topMargin + layoutParams.bottomMargin
            }
            return height
        }
    }
}