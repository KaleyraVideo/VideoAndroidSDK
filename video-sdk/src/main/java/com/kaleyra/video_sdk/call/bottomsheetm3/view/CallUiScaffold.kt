@file:OptIn(ExperimentalFoundationApi::class)

package com.kaleyra.video_sdk.call.bottomsheetm3.view

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun CallUiScaffold(
    windowSizeClass: WindowSizeClass,
    content: @Composable () -> Unit,
    primaryActions: @Composable () -> Unit,
    bottomSheetState: CallUiBottomSheetState,
    secondaryActions: @Composable BoxScope.() -> Unit,
    sheetShape: Shape = RoundedCornerShape(16.dp),
    sheetBackgroundColor: Color = Color.Transparent,
) {
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded || windowSizeClass.heightSizeClass == WindowHeightSizeClass.Expanded) {
        TabletCallUiScaffold(content, primaryActions, secondaryActions)
        return
    }

    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> LandscapeCallUiScaffold(content, primaryActions, bottomSheetState, secondaryActions, sheetShape, sheetBackgroundColor)
        else -> PortraitCallUiScaffold(content, primaryActions, bottomSheetState, secondaryActions , sheetShape, sheetBackgroundColor)
    }
}

@Composable
fun TabletCallUiScaffold(
    content: @Composable () -> Unit,
    primaryActions: @Composable () -> Unit,
    secondaryActions: @Composable BoxScope.() -> Unit) {

    // content
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(end = 116.dp)) {
        content()
    }

    // actions
    Row(modifier = Modifier
        .fillMaxSize()) {

        Spacer(modifier = Modifier
            .fillMaxHeight()
            .weight(1f))

        // secondary actions
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth(),
            content = {
                secondaryActions()
            }
        )

        // primary actions
        primaryActions()
    }
}

@Composable
fun LandscapeCallUiScaffold(
    content: @Composable () -> Unit,
    primaryActions: @Composable () -> Unit,
    bottomSheetState: CallUiBottomSheetState,
    secondaryActions: @Composable BoxScope.() -> Unit,
    sheetShape: Shape = RoundedCornerShape(16.dp),
    sheetBackgroundColor: Color = Color.Transparent,
) {
    var sheetWidth by remember { mutableIntStateOf(0) }
    var sheetOffset by remember { mutableIntStateOf(0) }

    val bottomSheetNestedScrollConnection = remember(bottomSheetState.draggableState) {
        CallUiBottomSheetNestedScrollConnection(
            state = bottomSheetState.draggableState,
            orientation = Orientation.Horizontal
        )
    }

    // content
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(end = 116.dp)) {
        content()
    }

    // call actions
    Row(modifier = Modifier
        .fillMaxSize()) {

        Spacer(modifier = Modifier
            .fillMaxHeight()
            .weight(1f))

        // secondary actions
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .graphicsLayer { clip = true }
                .onSizeChanged {
                    sheetWidth = it.width
                    if (sheetWidth <= 0) return@onSizeChanged
                    bottomSheetState.updateAnchors(sheetWidth)
                    sheetOffset = bottomSheetState
                        .requireOffset()
                        .roundToInt()
                }
                .offset {
                    val offset = if (bottomSheetState.draggableState.offset.isNaN()) 0 else bottomSheetState
                        .requireOffset()
                        .roundToInt()
                    println(offset)
                    IntOffset(
                        x = offset,
                        y = 0
                    )
                }
                .anchoredDraggable(
                    state = bottomSheetState.draggableState,
                    orientation = Orientation.Horizontal
                )
                .nestedScroll(bottomSheetNestedScrollConnection)
                .background(sheetBackgroundColor, sheetShape),
            content = {
                secondaryActions()
            }
        )

        // primary actions
        primaryActions()
    }
}

@Composable
fun PortraitCallUiScaffold(
    content: @Composable () -> Unit,
    primaryActions: @Composable () -> Unit,
    bottomSheetState: CallUiBottomSheetState,
    secondaryActions: @Composable BoxScope.() -> Unit,
    sheetShape: Shape = RoundedCornerShape(16.dp),
    sheetBackgroundColor: Color = Color.Transparent,
) {
    var sheetHeight by remember { mutableIntStateOf(0) }
    var sheetOffset by remember { mutableIntStateOf(0) }

    val bottomSheetNestedScrollConnection = remember(bottomSheetState.draggableState) {
        CallUiBottomSheetNestedScrollConnection(
            state = bottomSheetState.draggableState,
            orientation = Orientation.Vertical
        )
    }

    val modifier = Modifier

    // content
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 116.dp)) {
        content()
    }

    // call actions
    Column(modifier = modifier
        .fillMaxSize()) {

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(1f))

        // secondary actions
        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .graphicsLayer { clip = true }
                .onSizeChanged {
                    sheetHeight = it.height
                    if (sheetHeight <= 0) return@onSizeChanged
                    bottomSheetState.updateAnchors(sheetHeight)
                    sheetOffset = bottomSheetState
                        .requireOffset()
                        .roundToInt()
                }
                .offset {
                    val offset = if (bottomSheetState.draggableState.offset.isNaN()) 0 else bottomSheetState
                        .requireOffset()
                        .roundToInt()
                    println(offset)
                    IntOffset(
                        x = 0,
                        y = offset
                    )
                }
                .anchoredDraggable(
                    state = bottomSheetState.draggableState,
                    orientation = Orientation.Vertical
                )
                .nestedScroll(bottomSheetNestedScrollConnection)
                .background(sheetBackgroundColor, sheetShape),
            content = secondaryActions
        )

        // primary actions
        primaryActions()
    }
}

private fun CallUiBottomSheetNestedScrollConnection(
    state: AnchoredDraggableState<CallUiBottomSheetValue>,
    orientation: Orientation
): NestedScrollConnection = object : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.offsetToFloat()
        return if (delta < 0 && source == NestedScrollSource.Drag) {
            state.dispatchRawDelta(delta).toOffset()
        } else {
            Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val delta = available.offsetToFloat()
        return if (source == NestedScrollSource.Drag) {
            state.dispatchRawDelta(delta).toOffset()
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val toFling = available.velocityToFloat()
        val currentOffset = state.requireOffset()
        return if (toFling < 0 && currentOffset > state.anchors.minAnchor()) {
            state.settle(toFling)
            available
        } else {
            Velocity.Zero
        }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val toFling = available.velocityToFloat()
        state.settle(toFling)
        return available
    }


    private fun Offset.offsetToFloat(): Float = if (orientation == Orientation.Horizontal) x else y

    private fun Float.toOffset(): Offset = Offset(
        x = if (orientation == Orientation.Horizontal) this else 0f,
        y = if (orientation == Orientation.Vertical) this else 0f
    )

    private fun Velocity.velocityToFloat() = if (orientation == Orientation.Horizontal) x else y
}