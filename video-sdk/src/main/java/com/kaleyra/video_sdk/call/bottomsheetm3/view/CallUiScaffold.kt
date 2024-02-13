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
    secondaryActions: @Composable () -> Unit,
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
        Configuration.ORIENTATION_LANDSCAPE -> LandscapeCallUiScaffold(content, primaryActions, secondaryActions)
        else -> PortraitCallUiScaffold(content, primaryActions, secondaryActions)
    }
}

@Composable
fun PortraitCallUiScaffold(
    content: @Composable () -> Unit,
    primaryActions: @Composable () -> Unit,
    secondaryActions: @Composable () -> Unit,
) {
    // content
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(bottom = 116.dp)) {
        content()
    }

    // call actions
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier
            .fillMaxWidth()
            .weight(1f))

        // secondary actions
        secondaryActions()

        // primary actions
        primaryActions()
    }
}

@Composable
fun LandscapeCallUiScaffold(
    content: @Composable () -> Unit,
    primaryActions: @Composable () -> Unit,
    secondaryActions: @Composable () -> Unit
) {
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
        secondaryActions()

        // primary actions
        primaryActions()
    }
}

@Composable
fun TabletCallUiScaffold(
    content: @Composable () -> Unit,
    primaryActions: @Composable () -> Unit,
    secondaryActions: @Composable () -> Unit) {

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
