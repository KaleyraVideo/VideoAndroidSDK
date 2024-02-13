@file:OptIn(ExperimentalFoundationApi::class)

package com.kaleyra.video_sdk.call.bottomsheetm3.view

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactionsm3.model.CallActionsM3UiState
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionFor
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3Configuration
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun CallUiSecondaryActions(
    callActionsUiState: CallActionsM3UiState,
    bottomSheetState: CallUiBottomSheetState,
    isSystemInDarkTheme: Boolean
) {
    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> CallUiPhoneLandscapeSecondaryActions(callActionsUiState, bottomSheetState = bottomSheetState, isSystemInDarkTheme = isSystemInDarkTheme)
        else -> CallUiPhonePortraitSecondaryActions(callActionsUiState, bottomSheetState = bottomSheetState, isSystemInDarkTheme = isSystemInDarkTheme)
    }
}

@Composable
internal fun CallUiPhonePortraitSecondaryActions(
    callActionsUiState: CallActionsM3UiState,
    bottomSheetState: CallUiBottomSheetState,
    itemsPerRow: Int = kotlin.math.min(callActionsUiState.secondaryActionList.value.count(), 5),
    isSystemInDarkTheme: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    var sheetHeight by remember { mutableIntStateOf(0) }
    var sheetOffset by remember { mutableIntStateOf(0) }

    val bottomSheetNestedScrollConnection = remember(bottomSheetState.draggableState) {
        callUiBottomSheetNestedScrollConnection(
            state = bottomSheetState.draggableState,
            orientation = Orientation.Vertical
        )
    }
    bottomSheetState.peekOffset = 32.dp

    Box(
        modifier = Modifier
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
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Box(modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp))) {
                if (callActionsUiState.secondaryActionList.value.isNotEmpty()) DragLine(onClickLabel = stringResource(id = R.string.kaleyra_call_drag_to_expand), onClick = {})
            }
            BoxWithConstraints(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Table(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    columnCount = itemsPerRow,
                    data = callActionsUiState.secondaryActionList.value,
                    cellContent = { index, action, itemsPerRow ->
                        val availableWidth = this.maxWidth - 16.dp
                        val itemWidth = portraitActionWidth(maxWidth = availableWidth, itemsPerRow = itemsPerRow, index = index)
                        val containerWidth = portraitActionContainerWidth(index = index, maxWidth = availableWidth, itemsCount = itemsPerRow)

                        Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                            Column {
                                when (action) {
                                    is CallAction.Camera,
                                    is CallAction.Microphone,
                                    is CallAction.VirtualBackground,
                                    is CallAction.More,
                                    is CallAction.ScreenShare -> {
                                        CallActionFor(
                                            buttonWidth = itemWidth,
                                            containerWidth = containerWidth,
                                            actionConfiguration = CallActionM3Configuration.Toggleable(action = action as CallAction.Toggleable, onToggle = {
                                                if (action !is CallAction.More) return@Toggleable
                                                coroutineScope.launch {

                                                }
                                            }),
                                            badgeCount = 0,
                                            displayLabel = true,
                                            isDarkTheme = isSystemInDarkTheme
                                        )
                                    }

                                    is CallAction.Answer,
                                    is CallAction.Audio,
                                    is CallAction.Chat,
                                    is CallAction.FileShare,
                                    is CallAction.HangUp,
                                    is CallAction.SwitchCamera,
                                    is CallAction.Whiteboard -> {
                                        CallActionFor(
                                            buttonWidth = itemWidth,
                                            containerWidth = containerWidth,
                                            actionConfiguration = CallActionM3Configuration.Clickable(action = action, onClick = {
                                                coroutineScope.launch {

                                                }
                                            }),
                                            badgeCount = 0,
                                            displayLabel = true,
                                            isDarkTheme = isSystemInDarkTheme
                                        )
                                    }
                                }
                            }
                        }
                    })
            }
            Divider(modifier = Modifier
                .height(16.dp),
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
internal fun CallUiPhoneLandscapeSecondaryActions(
    callActionsUiState: CallActionsM3UiState,
    bottomSheetState: CallUiBottomSheetState,
    itemsPerRow: Int = kotlin.math.min(callActionsUiState.secondaryActionList.value.count(), 5),
    isSystemInDarkTheme: Boolean
) {
    val coroutineScope = rememberCoroutineScope()
    var displayLabels by remember { mutableStateOf(false) }
    var sheetWidth by remember { mutableIntStateOf(0) }
    var sheetOffset by remember { mutableIntStateOf(0) }

    val bottomSheetNestedScrollConnection = remember(bottomSheetState.draggableState) {
        callUiBottomSheetNestedScrollConnection(
            state = bottomSheetState.draggableState,
            orientation = Orientation.Horizontal
        )
    }
    bottomSheetState.peekOffset = 32.dp

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
    ) {
        BoxWithConstraints {
            displayLabels = 75.dp * itemsPerRow <= this.maxHeight

            Row(modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp)
            ) {
                Box(modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))) {
                    if (callActionsUiState.secondaryActionList.value.isNotEmpty()) {
                        VerticalDragLine(onClickLabel = stringResource(id = R.string.kaleyra_call_drag_to_expand), onClick = {})
                    }
                }
                LazyHorizontalStaggeredGrid(modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surface),
                    rows = StaggeredGridCells.Fixed(itemsPerRow)) {

                    items(callActionsUiState.secondaryActionList.value) { action ->
                        Box(modifier = Modifier.width(80.dp), contentAlignment = Alignment.Center) {
                            when (action) {
                                is CallAction.Camera,
                                is CallAction.Microphone,
                                is CallAction.VirtualBackground,
                                is CallAction.More,
                                is CallAction.ScreenShare -> {
                                    CallActionFor(
                                        buttonWidth = 48.dp,
                                        containerWidth = 96.dp,
                                        actionConfiguration = CallActionM3Configuration.Toggleable(action = action as CallAction.Toggleable, onToggle = {
                                            if (action !is CallAction.More) return@Toggleable
                                            coroutineScope.launch {

                                            }
                                        }),
                                        isDarkTheme = isSystemInDarkTheme,
                                        displayLabel = displayLabels
                                    )
                                }

                                is CallAction.Answer,
                                is CallAction.Audio,
                                is CallAction.Chat,
                                is CallAction.FileShare,
                                is CallAction.HangUp,
                                is CallAction.SwitchCamera,
                                is CallAction.Whiteboard -> {
                                    CallActionFor(
                                        buttonWidth = 48.dp,
                                        containerWidth = 96.dp,
                                        actionConfiguration = CallActionM3Configuration.Clickable(action = action, onClick = {}),
                                        isDarkTheme = isSystemInDarkTheme,
                                        displayLabel = displayLabels
                                    )
                                }
                            }
                        }
                    }
                }
                Divider(
                    modifier = Modifier
                        .width(16.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surface,
                )
            }
        }
    }
}
