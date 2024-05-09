package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.CallInfoBar
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.HSheetDragActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.VSheetDragActions
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.HSheetActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.VSheetActions
import com.kaleyra.video_sdk.call.callactionnew.AudioAction
import com.kaleyra.video_sdk.call.callactionnew.CameraAction
import com.kaleyra.video_sdk.call.callactionnew.ChatAction
import com.kaleyra.video_sdk.call.callactionnew.FileShareAction
import com.kaleyra.video_sdk.call.callactionnew.FlipCameraAction
import com.kaleyra.video_sdk.call.callactionnew.HangUpAction
import com.kaleyra.video_sdk.call.callactionnew.MicAction
import com.kaleyra.video_sdk.call.callactionnew.ScreenShareAction
import com.kaleyra.video_sdk.call.callactionnew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.callactionnew.WhiteboardAction
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.launch

internal typealias ActionComposable = @Composable (label: Boolean, modifier: Modifier) -> Unit


@Composable
fun actionsComposablesFor(
    actions: CallActionsUI,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onHangUpClick: () -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit
): ImmutableList<ActionComposable> {
    with(actions) {
        val hangUpAction: ActionComposable =
            { _, modifier ->
                HangUpAction(
                    enabled = hangUpAction.isEnabled,
                    onClick = onHangUpClick,
                    modifier = modifier
                )
            }
        val micAction: ActionComposable? = microphoneAction?.let {
            { _, modifier ->
                MicAction(
                    checked = it.isToggled,
                    enabled = it.isEnabled,
                    warning = it.state == InputCallAction.State.Warning,
                    error = it.state == InputCallAction.State.Error,
                    onCheckedChange = onMicToggled,
                    modifier = modifier
                )
            }
        }
        val cameraAction: ActionComposable? = cameraAction?.let {
            { _, modifier ->
                CameraAction(
                    checked = it.isToggled,
                    enabled = it.isEnabled,
                    warning = it.state == InputCallAction.State.Warning,
                    error = it.state == InputCallAction.State.Error,
                    onCheckedChange = onCameraToggled,
                    modifier = modifier
                )
            }
        }
        val flipCameraAction: ActionComposable? = flipCameraAction?.let {
            { label, modifier ->
                FlipCameraAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFlipCameraClick,
                    modifier = modifier
                )
            }
        }
        val audioAction: ActionComposable? = audioAction?.let {
            { label, modifier ->
                AudioAction(
                    audioDevice = it.audioDevice,
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onAudioClick,
                    modifier = modifier
                )
            }
        }
        val chatAction: ActionComposable? = chatAction?.let {
            { label, modifier ->
                ChatAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onChatClick,
                    modifier = modifier
                )
            }
        }
        val fileShareAction: ActionComposable? = fileShareAction?.let {
            { label, modifier ->
                FileShareAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFileShareClick,
                    modifier = modifier
                )
            }
        }
        val screenShareAction: ActionComposable? = screenShareAction?.let {
            { label, modifier ->
                ScreenShareAction(
                    label = label,
                    enabled = it.isEnabled,
                    checked = it.isToggled,
                    onCheckedChange = onScreenShareToggle,
                    modifier = modifier
                )
            }
        }
        val whiteboardAction: ActionComposable? = whiteboardAction?.let {
            { label, modifier ->
                WhiteboardAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onWhiteboardClick,
                    modifier = modifier
                )
            }
        }
        val virtualBackgroundAction: ActionComposable? =
            virtualBackgroundAction?.let {
                { label, modifier ->
                    VirtualBackgroundAction(
                        label = label,
                        enabled = it.isEnabled,
                        onClick = onVirtualBackgroundClick,
                        modifier = modifier
                    )
                }
            }

        return ImmutableList(
            listOfNotNull(
                hangUpAction,
                micAction,
                cameraAction,
                flipCameraAction,
                audioAction,
                chatAction,
                fileShareAction,
                screenShareAction,
                whiteboardAction,
                virtualBackgroundAction
            )
        )
    }
}

@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    actions: CallActionsUI,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onHangUpClick: () -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val actionsComposables = actionsComposablesFor(
        actions = actions,
        onMicToggled = onMicToggled,
        onCameraToggled = onCameraToggled,
        onScreenShareToggle = onScreenShareToggle,
        onHangUpClick = onHangUpClick,
        onFlipCameraClick = onFlipCameraClick,
        onAudioClick = onAudioClick,
        onChatClick = onChatClick,
        onFileShareClick = onFileShareClick,
        onWhiteboardClick = onWhiteboardClick,
        onVirtualBackgroundClick = onVirtualBackgroundClick
    )
    val sheetState = rememberCallSheetState()

    val scope = rememberCoroutineScope()
    val flipSheetState: () -> Unit = remember {
        {
            scope.launch {
                if (sheetState.currentValue == CallSheetValue.Expanded) sheetState.collapse()
                else sheetState.expand()
            }
        }
    }

    if (isCompactHeight) {
        HCallScreen(
            sheetState = sheetState,
            actions = actionsComposables,
            flipSheetState = flipSheetState
        )
    } else {
        VCallScreen(
            windowSizeClass = windowSizeClass,
            sheetState = sheetState,
            actions = actionsComposables,
            flipSheetState = flipSheetState
        )
    }
}

@Composable
internal fun VCallScreen(
    windowSizeClass: WindowSizeClass,
    sheetState: CallSheetState,
    actions: ImmutableList<ActionComposable>,
    flipSheetState: () -> Unit
) {
    val isLargeScreen = windowSizeClass.widthSizeClass in setOf(WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded)
   
    var sheetDragActions: ImmutableList<ActionComposable> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember { derivedStateOf { !isLargeScreen and sheetDragActions.value.isNotEmpty() } }
   
    var showSheetPanelContent by remember { mutableStateOf(false) }

    VCallScreenScaffold(
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 4.dp, vertical = 8.dp),
        topAppBar = {
            CallInfoBar(
                title = "title",
                logo = Logo(),
                recording = false,
                participantCount = 3,
                onParticipantClick = { },
                onBackPressed = {},
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetPanelContent = {
            AnimatedVisibility(
                visible = isLargeScreen and showSheetPanelContent,
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
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val itemsPerRow = actions.count() - sheetDragActions.count() + 1
//                + if (displayAnswerButton) AnswerButtonMultiplier else 1
                HSheetDragActions(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(14.dp),
                    actions = sheetDragActions,
                    itemsPerRow = itemsPerRow
                )
            }
        },
        sheetContent = {
            HSheetActions(
                actions = actions,
                maxActions = 8,
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {
                    if (hasSheetDragContent) flipSheetState()
                    else showSheetPanelContent = !showSheetPanelContent
                },
                onActionsPlaced = { itemsPlaced ->
                    sheetDragActions = ImmutableList(actions.value.takeLast(actions.count() - itemsPlaced))
                },
                modifier = Modifier.padding(14.dp),
            )
        },
        containerColor = Color.DarkGray,
        sheetDragHandle = if (hasSheetDragContent) {
            { CallBottomSheetDefaults.HDragHandle() }
        } else null
    ) { paddingValues ->

    }
}

@Composable
internal fun HCallScreen(
    sheetState: CallSheetState,
    actions: ImmutableList<ActionComposable>,
    flipSheetState: () -> Unit
) {
    var sheetDragActions: ImmutableList<ActionComposable> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember { derivedStateOf { sheetDragActions.value.isNotEmpty() } }

    HCallScreenScaffold(
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 8.dp, vertical = 4.dp),
        topAppBar = {
            CallInfoBar(
                title = "title",
                logo = Logo(),
                recording = false,
                participantCount = 3,
                onParticipantClick = { },
                onBackPressed = {},
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val itemsPerColumn = actions.count() - sheetDragActions.count() + 1
//                + if (displayAnswerButton) AnswerButtonMultiplier else 1
                VSheetDragActions(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(14.dp),
                    actions = sheetDragActions,
                    itemsPerColumn = itemsPerColumn
                )
            }
        },
        sheetContent = {
            VSheetActions(
                actions = actions,
                maxActions = 8,
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = flipSheetState,
                onActionsPlaced = { itemsPlaced ->
                    sheetDragActions = ImmutableList(actions.value.takeLast(actions.count() - itemsPlaced))
                },
                modifier = Modifier.padding(14.dp),
            )
        },
        containerColor = Color.DarkGray,
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