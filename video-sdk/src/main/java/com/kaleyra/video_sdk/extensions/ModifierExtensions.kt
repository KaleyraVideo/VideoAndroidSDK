/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.extensions

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.DeferredTargetAnimation
import androidx.compose.animation.core.ExperimentalAnimatableApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private val FocusHighlightStroke = 2.dp
private val FocusHighlightColor = Color.Red

internal object ModifierExtensions {

    @Stable
    internal fun Modifier.supportRtl(): Modifier =
        composed {
            if (LocalLayoutDirection.current == LayoutDirection.Rtl) scale(-1f, -1f) else this
        }

    @Stable
    internal fun Modifier.highlightOnFocus(interactionSource: MutableInteractionSource): Modifier =
        this.composed {
            val inputModeManager = LocalInputModeManager.current
            val isFocused = interactionSource.collectIsFocusedAsState().value
            val enableHighlight = remember(
                inputModeManager,
                isFocused
            ) { derivedStateOf { inputModeManager.inputMode != InputMode.Touch && isFocused }.value }
            border(
                width = if (enableHighlight) FocusHighlightStroke else 0.dp,
                color = if (enableHighlight) FocusHighlightColor else Color.Transparent
            )
        }

    @Stable
    internal fun Modifier.pulse(durationMillis: Int = 1000, enabled: Boolean = true): Modifier =
        composed {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )
            graphicsLayer {
                if (enabled) this.alpha = alpha
            }
        }

    @OptIn(ExperimentalLayoutApi::class)
    internal fun Modifier.clearFocusOnKeyboardDismiss(): Modifier = composed {
        var isFocused by remember { mutableStateOf(false) }
        var keyboardAppearedSinceLastFocused by remember { mutableStateOf(false) }
        if (isFocused) {
            val imeIsVisible = WindowInsets.isImeVisible
            val focusManager = LocalFocusManager.current
            LaunchedEffect(imeIsVisible) {
                if (imeIsVisible) {
                    keyboardAppearedSinceLastFocused = true
                } else if (keyboardAppearedSinceLastFocused) {
                    focusManager.clearFocus()
                }
            }
        }
        onFocusEvent {
            if (isFocused == it.isFocused) return@onFocusEvent
            isFocused = it.isFocused
            if (isFocused) {
                keyboardAppearedSinceLastFocused = false
            }
        }
    }

    /**
     * It's animating the constraints update on the element.
     *
     * @receiver Modifier
     * @param animationSpec FiniteAnimationSpec<IntSize>
     * @return Modifier
     */
    @OptIn(ExperimentalAnimatableApi::class)
    internal fun Modifier.animateConstraints(
        animationSpec: FiniteAnimationSpec<IntSize> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    ) = composed {
        val coroutineScope = rememberCoroutineScope()
        val sizeAnimation = remember { DeferredTargetAnimation(IntSize.VectorConverter) }
        this.approachLayout(
            isMeasurementApproachInProgress = { lookaheadSize ->
                sizeAnimation.updateTarget(lookaheadSize, coroutineScope)
                !sizeAnimation.isIdle
            }
        ) { measurable, _ ->
            val (width, height) = sizeAnimation.updateTarget(lookaheadSize, coroutineScope, animationSpec)
            val animatedConstraints = Constraints.fixed(width, height)
            val placeable = measurable.measure(animatedConstraints)
            layout(placeable.width, placeable.height) {
                placeable.place(0, 0)
            }
        }
    }

    /**
     * Animate the placement using the lookahead scope.
     * It's animating the placement from the current
     * position of the element to its future position.
     *
     * @receiver Modifier
     * @param lookaheadScope LookaheadScope
     * @param animationSpec FiniteAnimationSpec<IntOffset>
     * @return Modifier
     */
    @OptIn(ExperimentalAnimatableApi::class)
    internal fun Modifier.animatePlacement(
        lookaheadScope: LookaheadScope,
        animationSpec: FiniteAnimationSpec<IntOffset> = spring()
    ) = composed {
        val coroutineScope = rememberCoroutineScope()
        val offsetAnimation = remember { DeferredTargetAnimation(IntOffset.VectorConverter) }
        this.approachLayout(
            isMeasurementApproachInProgress = { false },
            isPlacementApproachInProgress = { lookaheadCoordinates ->
                val target = with(lookaheadScope) {
                    lookaheadScopeCoordinates.localLookaheadPositionOf(lookaheadCoordinates).round()
                }
                offsetAnimation.updateTarget(target, coroutineScope)
                !offsetAnimation.isIdle
            }
        ) { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                val coordinates = coordinates
                if (coordinates != null) {
                    // Calculates the target offset within the lookaheadScope
                    val target: IntOffset = with(lookaheadScope) {
                        lookaheadScopeCoordinates.localLookaheadPositionOf(coordinates).round()
                    }
                    // Uses the target offset to start an offset animation
                    val animatedOffset = offsetAnimation.updateTarget(target, coroutineScope, animationSpec)
                    // Calculates the current offset within the given LookaheadScope
                    val placementOffset = with(lookaheadScope) {
                        lookaheadScopeCoordinates.localPositionOf(coordinates, Offset.Zero).round()
                    }
                    // Calculates the delta between animated position in scope and current
                    // position in scope, and places the child at the delta offset. This puts
                    // the child layout at the animated position.
                    val (x, y) = animatedOffset - placementOffset
                    placeable.place(x, y)
                } else {
                    placeable.place(0, 0)
                }
            }
        }
    }

    /**
     * Animate the placement of an element starting from an initial offset
     *
     * @receiver Modifier
     * @param initialOffset IntOffset
     * @param animationSpec AnimationSpec<IntOffset>
     * @return Modifier
     */
    internal fun Modifier.animatePlacement(
        initialOffset: IntOffset = IntOffset.Zero,
        animationSpec: AnimationSpec<IntOffset> = spring()
    ): Modifier = composed {
        val scope = rememberCoroutineScope()
        var animatable: Animatable<IntOffset, AnimationVector2D>? by remember { mutableStateOf(null) }
        var targetOffset by remember { mutableStateOf(initialOffset) }

        this
            .onPlaced {
                targetOffset = it
                    .positionInParent()
                    .round()
            }
            .offset {
                val anim = animatable ?: Animatable(targetOffset, IntOffset.VectorConverter).also {
                    animatable = it
                }
                if (anim.targetValue != targetOffset) {
                    scope.launch {
                        anim.animateTo(targetOffset, animationSpec)
                    }
                }
                anim.value - targetOffset
            }
    }

    /**
     * Draws a rounder corner border with enhanced rendering compared to the standard border
     * @receiver Modifier
     * @param width Dp border width
     * @param color Color border color
     * @param alpha Float border alpha
     * @param cornerRadius CornerRadius border corner radius
     * @return Modifier
     */
    internal fun Modifier.drawRoundedCornerBorder(
        width: Dp,
        color: Color,
        alpha: Float,
        cornerRadius: CornerRadius
    ): Modifier {
        if (width == 0.dp) return this
        return drawWithContent {
            drawContent()
            drawRoundRect(
                color = color,
                alpha = alpha,
                cornerRadius = cornerRadius,
                blendMode = BlendMode.SrcOver,
                style = Stroke(width = width.toPx())
            )
        }
    }

    /**
     * Draws a circle border with enhanced rendering compared to the standard border
     *
     * @receiver Modifier
     * @param color Color
     * @param width Dp
     */
    internal fun Modifier.drawCircleBorder(
        color: Color,
        width: Dp,
        alpha: Float = 1f
    ): Modifier {
        if (width == 0.dp) return this
        return drawWithContent {
            drawContent()
            drawCircle(
                color,
                blendMode = BlendMode.SrcOver,
                style = Stroke(width = width.toPx()),
                alpha = alpha
            )
        }
    }

    /**
     * Draws a radial gradient shadow around the composable element.
     *
     * This modifier draws a radial gradient that fades from the specified [color] at the center
     * to transparent at the edges. The gradient's radius is calculated based on the width of the
     * composable element and the provided [radius] parameter.
     *
     * @param color The color of the shadow at its center.
     * @param radius The additional radius to extend the shadow beyond the element's bounds.
     *               A larger radius will result in a more diffused shadow.
     * @return A [Modifier] that draws the radial gradient shadow.
     */
    internal fun Modifier.drawGradientShadow(
        color: Color,
        radius: Dp
    ): Modifier {
        return drawWithContent {
            val gradientRadius = (this.size.width / 2f) + radius.toPx()
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color, Color.Transparent),
                    radius = gradientRadius
                ),
                radius = gradientRadius,
            )
            drawContent()
        }
    }

    /**
     * A [Modifier] that draws a border around elements that are recomposing. The border increases in
     * size and interpolates from red to green as more recompositions occur before a timeout.
     */
    @Stable
    internal fun Modifier.recomposeHighlighter(): Modifier = this.then(recomposeModifier)

    // Use a single instance + @Stable to ensure that recompositions can enable skipping optimizations
    // Modifier.composed will still remember unique data per call site.
    private val recomposeModifier =
        Modifier.composed(inspectorInfo = debugInspectorInfo { name = "recomposeHighlighter" }) {
            // The total number of compositions that have occurred. We're not using a State<> here be
            // able to read/write the value without invalidating (which would cause infinite
            // recomposition).
            val totalCompositions = remember { arrayOf(0L) }
            totalCompositions[0]++

            // The value of totalCompositions at the last timeout.
            val totalCompositionsAtLastTimeout = remember { mutableStateOf(0L) }

            // Start the timeout, and reset everytime there's a recomposition. (Using totalCompositions
            // as the key is really just to cause the timer to restart every composition).
            LaunchedEffect(totalCompositions[0]) {
                delay(3000)
                totalCompositionsAtLastTimeout.value = totalCompositions[0]
            }

            Modifier.drawWithCache {
                onDrawWithContent {
                    // Draw actual content.
                    drawContent()

                    // Below is to draw the highlight, if necessary. A lot of the logic is copied from
                    // Modifier.border
                    val numCompositionsSinceTimeout =
                        totalCompositions[0] - totalCompositionsAtLastTimeout.value

                    val hasValidBorderParams = size.minDimension > 0f
                    if (!hasValidBorderParams || numCompositionsSinceTimeout <= 0) {
                        return@onDrawWithContent
                    }

                    val (color, strokeWidthPx) =
                        when (numCompositionsSinceTimeout) {
                            // We need at least one composition to draw, so draw the smallest border
                            // color in blue.
                            1L -> Color.Blue to 1f
                            // 2 compositions is _probably_ okay.
                            2L -> Color.Green to 2.dp.toPx()
                            // 3 or more compositions before timeout may indicate an issue. lerp the
                            // color from yellow to red, and continually increase the border size.
                            else -> {
                                lerp(
                                    Color.Yellow.copy(alpha = 0.8f),
                                    Color.Red.copy(alpha = 0.5f),
                                    min(1f, (numCompositionsSinceTimeout - 1).toFloat() / 100f)
                                ) to numCompositionsSinceTimeout.toInt().dp.toPx()
                            }
                        }

                    val halfStroke = strokeWidthPx / 2
                    val topLeft = Offset(halfStroke, halfStroke)
                    val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)

                    val fillArea = (strokeWidthPx * 2) > size.minDimension
                    val rectTopLeft = if (fillArea) Offset.Zero else topLeft
                    val size = if (fillArea) size else borderSize
                    val style = if (fillArea) Fill else Stroke(strokeWidthPx)

                    drawRect(
                        brush = SolidColor(color),
                        topLeft = rectTopLeft,
                        size = size,
                        style = style
                    )
                }
            }
        }
}
