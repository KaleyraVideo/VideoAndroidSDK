package com.kaleyra.video_sdk.call.bottomsheetm3.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactionsm3.model.CallActionsM3UiState
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionFor
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3Configuration
import com.kaleyra.video_sdk.call.callactionsm3.view.CallActionM3Defaults
import kotlinx.coroutines.launch

@Composable
internal fun CallUiPrimaryActions(
    callActionsUiState: CallActionsM3UiState,
    isSystemInDarkTheme: Boolean
) {
    var orientation by remember { mutableStateOf(Configuration.ORIENTATION_PORTRAIT) }
    val configuration = LocalConfiguration.current

    LaunchedEffect(configuration) {
        snapshotFlow { configuration.orientation }
            .collect { orientation = it }
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> CallUiPhoneLandscapePrimaryActions(isDarkTheme = isSystemInDarkTheme, callActionsUiState = callActionsUiState)
        else -> CallUiPhonePortraitPrimaryActions(isDarkTheme = isSystemInDarkTheme, callActionsUiState = callActionsUiState)
    }
}

@Composable
private fun CallUiPhonePortraitPrimaryActions(
    callActionsUiState: CallActionsM3UiState,
    itemsPerRow: Int = 5,
    isDarkTheme: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, bottom = 16.dp, end = 16.dp)
        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            Table(
                columnCount = itemsPerRow,
                data = callActionsUiState.primaryActionList.value,
                cellContent = { index, action, currentRowItemsCount ->
                    val itemWidth = portraitActionWidth(index = index, itemsPerRow = currentRowItemsCount, maxWidth = maxWidth)
                    val containerWidth = portraitActionContainerWidth(index = index, itemsPerRow = currentRowItemsCount, maxWidth = maxWidth)

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
                                        displayLabel = itemWidth > CallActionM3Defaults.Size,
                                        isDarkTheme = isDarkTheme
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
                                        displayLabel = itemWidth > CallActionM3Defaults.Size,
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            }
                        }
                    }
                })
        }
    }
}

@Composable
private fun CallUiPhoneLandscapePrimaryActions(
    callActionsUiState: CallActionsM3UiState,
    itemsPerRow: Int = kotlin.math.min(callActionsUiState.primaryActionList.value.count(), 5),
    isDarkTheme: Boolean
) {
    val coroutineScope = rememberCoroutineScope()

    var displayLabels by remember { mutableStateOf(false) }

    BoxWithConstraints {
        displayLabels = 75.dp * itemsPerRow <= this.maxHeight

        Row(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 8.dp, end = 16.dp),
        ) {
            LazyHorizontalStaggeredGrid(modifier = Modifier
                .width(64.dp)
                .background(MaterialTheme.colorScheme.surface),
                rows = StaggeredGridCells.Fixed(itemsPerRow)) {
                items(items = callActionsUiState.primaryActionList.value.reversed(), key = { it::class.java.name }) { action ->
                    Box(modifier = Modifier, contentAlignment = Alignment.Center) {
                        Column {
                            when (action) {
                                is CallAction.Camera,
                                is CallAction.Microphone,
                                is CallAction.VirtualBackground,
                                is CallAction.More,
                                is CallAction.ScreenShare -> {
                                    CallActionFor(
                                        buttonWidth = 48.dp,
                                        containerWidth = 48.dp,
                                        actionConfiguration = CallActionM3Configuration.Toggleable(action = action as CallAction.Toggleable, onToggle = {
                                            if (action !is CallAction.More) return@Toggleable
                                            coroutineScope.launch {

                                            }
                                        }),
                                        displayLabel = false,
                                        isDarkTheme = isDarkTheme
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
                                        containerWidth = 48.dp,
                                        actionConfiguration = CallActionM3Configuration.Clickable(action = action, onClick = {
                                            coroutineScope.launch {

                                            }
                                        }),
                                        displayLabel = false,
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            }
                            if (displayLabels) Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier
                .fillMaxHeight()
                .width(32.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(bottomEnd = 16.dp, topEnd = 16.dp)))
        }
    }
}

internal fun portraitActionContainerWidth(index: Int, itemsPerRow: Int, maxWidth: Dp) = when {
    itemsPerRow == 1 -> maxWidth
    itemsPerRow == 2 -> maxWidth / 2
    itemsPerRow == 3 && index + 1 == itemsPerRow -> (maxWidth / 5) * 3
    itemsPerRow == 4 && index + 1 == itemsPerRow -> (maxWidth / 5) * 2
    else -> maxWidth / 5
}

internal fun portraitActionWidth(index: Int, itemsPerRow: Int, maxItemsPerRow: Int = 5, maxWidth: Dp): Dp {
    val spacerWidth = (maxWidth - (maxItemsPerRow * 48).dp) / 4
    return when (itemsPerRow) {
        1 -> maxWidth
        2 -> (maxWidth - spacerWidth) / 2
        3 -> if (index + 1 < 3) 48.dp else 144.dp + spacerWidth * 2
        4 -> if (index + 1 < 4) 48.dp else 96.dp + spacerWidth
        else -> 48.dp
    }
}
