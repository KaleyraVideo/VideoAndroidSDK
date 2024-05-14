package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.CallAppBar
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.HSheetActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.VSheetActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.HSheetDragActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.VSheetDragActions
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.launch

@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    actions: ImmutableList<CallActionUI>,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onHangUpClick: () -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    onParticipantClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val isLargeScreen = windowSizeClass.widthSizeClass in setOf(
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded
    )

    val sheetState = rememberCallSheetState()

    var showAnswerAction by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val onChangeSheetState: () -> Unit = remember {
        {
            scope.launch {
                if (sheetState.currentValue == CallSheetValue.Expanded) sheetState.collapse()
                else sheetState.expand()
            }
        }
    }

    if (isCompactHeight) {
        HCallScreen(
            callActions = actions,
            sheetState = sheetState,
            showAnswerAction = showAnswerAction,
            onChangeSheetState = onChangeSheetState,
            onParticipantClick = onParticipantClick,
            onAnswerActionClick = { showAnswerAction = false },
            onHangUpClick = onHangUpClick,
            onMicToggled = onMicToggled,
            onCameraToggled = onCameraToggled,
            onScreenShareToggle = onScreenShareToggle,
            onFlipCameraClick = onFlipCameraClick,
            onAudioClick = onAudioClick,
            onChatClick = onChatClick,
            onFileShareClick = onFileShareClick,
            onWhiteboardClick = onWhiteboardClick,
            onVirtualBackgroundClick = onVirtualBackgroundClick,
            onBackPressed = onBackPressed
        )
    } else {
        VCallScreen(
            isLargeScreen = isLargeScreen,
            callActions = actions,
            sheetState = sheetState,
            showAnswerAction = showAnswerAction,
            onChangeSheetState = onChangeSheetState,
            onParticipantClick = onParticipantClick,
            onAnswerActionClick = { showAnswerAction = false },
            onHangUpClick = onHangUpClick,
            onMicToggled = onMicToggled,
            onCameraToggled = onCameraToggled,
            onScreenShareToggle = onScreenShareToggle,
            onFlipCameraClick = onFlipCameraClick,
            onAudioClick = onAudioClick,
            onChatClick = onChatClick,
            onFileShareClick = onFileShareClick,
            onWhiteboardClick = onWhiteboardClick,
            onVirtualBackgroundClick = onVirtualBackgroundClick,
            onBackPressed = onBackPressed
        )
    }
}

internal const val CompactScreenMaxActions = 5
internal const val LargeScreenMaxActions = 8

@Composable
internal fun VCallScreen(
    isLargeScreen: Boolean,
    sheetState: CallSheetState,
    callActions: ImmutableList<CallActionUI>,
    showAnswerAction: Boolean,
    onChangeSheetState: () -> Unit,
    onAnswerActionClick: () -> Unit,
    onHangUpClick: () -> Unit,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    onParticipantClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    // TODO do a resize test when writing test
    val hasSheetDragContent by remember(isLargeScreen) { derivedStateOf { !isLargeScreen and sheetDragActions.value.isNotEmpty() } }
    // TODO test reset on resize
    var showSheetPanelContent by remember(isLargeScreen) { mutableStateOf(false) }

    VCallScreenScaffold(
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 4.dp, vertical = 8.dp),
        topAppBar = {
            CallAppBar(
                title = "title",
                logo = Logo(),
                recording = false,
                participantCount = 3,
                onParticipantClick = onParticipantClick,
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetPanelContent = {
            if (isLargeScreen) {
                AnimatedVisibility(
                    visible = showSheetPanelContent,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(Modifier.width(320.dp)) {
                        Column {
                            Text("ciao")
                            Text("ciao")
                            Text("ciao")
                            Text("ciao")
                            Text("ciao")
                            Text("ciao")
                            Text("ciao")
                        }
                    }
                }
            }
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val itemsPerRow = callActions.count() - sheetDragActions.count() + if (showAnswerAction) AnswerActionMultiplier else 1
                HSheetDragActions(
                    callActions = sheetDragActions,
                    itemsPerRow = itemsPerRow,
                    onHangUpClick = onHangUpClick,
                    onMicToggled = onMicToggled,
                    onCameraToggled = onCameraToggled,
                    onScreenShareToggle = onScreenShareToggle,
                    onFlipCameraClick = onFlipCameraClick,
                    onAudioClick = onAudioClick,
                    onChatClick = onChatClick,
                    onFileShareClick = onFileShareClick,
                    onWhiteboardClick = onWhiteboardClick,
                    onVirtualBackgroundClick = onVirtualBackgroundClick,
                    modifier = Modifier
                        .animateContentSize()
                        .padding(14.dp)
                )
            }
        },
        sheetContent = {
            Box(Modifier.animateContentSize()) {
                HSheetActions(
                    isLargeScreen = isLargeScreen,
                    callActions = callActions,
                    maxActions = if (isLargeScreen) LargeScreenMaxActions else CompactScreenMaxActions,
                    showAnswerAction = showAnswerAction,
                    onActionsPlaced = { itemsPlaced ->
                        sheetDragActions = ImmutableList(callActions.value.takeLast(callActions.count() - itemsPlaced))
                    },
                    onAnswerActionClick = onAnswerActionClick,
                    onHangUpClick = onHangUpClick,
                    onMicToggled = onMicToggled,
                    onCameraToggled = onCameraToggled,
                    onScreenShareToggle = onScreenShareToggle,
                    onFlipCameraClick = onFlipCameraClick,
                    onAudioClick = onAudioClick,
                    onChatClick = onChatClick,
                    onFileShareClick = onFileShareClick,
                    onWhiteboardClick = onWhiteboardClick,
                    onVirtualBackgroundClick = onVirtualBackgroundClick,
                    onMoreActionClick = {
                        if (hasSheetDragContent) onChangeSheetState()
                        else showSheetPanelContent = !showSheetPanelContent
                    },
                    modifier = Modifier
                        .padding(
                            start = 14.dp,
                            top = if (isLargeScreen) 14.dp else 5.dp,
                            end = 14.dp,
                            bottom = 14.dp
                        )
                )
            }
        },
        containerColor = Color.Gray,
        sheetDragHandle = if (hasSheetDragContent) {
            { CallBottomSheetDefaults.HDragHandle() }
        } else null
    ) { paddingValues ->

    }
}

@Composable
internal fun HCallScreen(
    callActions: ImmutableList<CallActionUI>,
    sheetState: CallSheetState,
    showAnswerAction: Boolean,
    onChangeSheetState: () -> Unit,
    onParticipantClick: () -> Unit,
    onAnswerActionClick: () -> Unit,
    onHangUpClick: () -> Unit,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember { derivedStateOf { sheetDragActions.value.isNotEmpty() } }

    HCallScreenScaffold(
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 8.dp, vertical = 4.dp),
        topAppBar = {
            CallAppBar(
                title = "title",
                logo = Logo(),
                recording = false,
                participantCount = 3,
                onParticipantClick = onParticipantClick,
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val itemsPerColumn = callActions.count() - sheetDragActions.count() + 1
                VSheetDragActions(
                    callActions = sheetDragActions,
                    itemsPerColumn = itemsPerColumn,
                    onHangUpClick = onHangUpClick,
                    onMicToggled = onMicToggled,
                    onCameraToggled = onCameraToggled,
                    onScreenShareToggle = onScreenShareToggle,
                    onFlipCameraClick = onFlipCameraClick,
                    onAudioClick = onAudioClick,
                    onChatClick = onChatClick,
                    onFileShareClick = onFileShareClick,
                    onWhiteboardClick = onWhiteboardClick,
                    onVirtualBackgroundClick = onVirtualBackgroundClick,
                    modifier = Modifier
                        .animateContentSize()
                        .padding(14.dp)
                )
            }
        },
        sheetContent = {
            Box(Modifier.animateContentSize()) {
                VSheetActions(
                    callActions = callActions,
                    maxActions = CompactScreenMaxActions,
                    showAnswerAction = showAnswerAction,
                    onActionsPlaced = { itemsPlaced ->
                        sheetDragActions =
                            ImmutableList(callActions.value.takeLast(callActions.count() - itemsPlaced))
                    },
                    onAnswerActionClick = onAnswerActionClick,
                    onHangUpClick = onHangUpClick,
                    onMicToggled = onMicToggled,
                    onCameraToggled = onCameraToggled,
                    onScreenShareToggle = onScreenShareToggle,
                    onFlipCameraClick = onFlipCameraClick,
                    onAudioClick = onAudioClick,
                    onChatClick = onChatClick,
                    onFileShareClick = onFileShareClick,
                    onWhiteboardClick = onWhiteboardClick,
                    onVirtualBackgroundClick = onVirtualBackgroundClick,
                    onMoreActionClick = onChangeSheetState,
                    modifier = Modifier.padding(
                        start = 5.dp,
                        top = 14.dp,
                        end = 14.dp,
                        bottom = 14.dp
                    )
                )
            }
        },
        containerColor = Color.Gray,
        sheetDragHandle = if (hasSheetDragContent) {
            { CallBottomSheetDefaults.VDragHandle() }
        } else null
    ) { paddingValues ->

    }
}

@Composable
private fun callScreenScaffoldPaddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    return WindowInsets.systemBars
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets(left = horizontal, top = vertical, right = horizontal, bottom = vertical))
        .asPaddingValues()
}