package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.CallInfoBar
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallScreenLandscapeScaffold
import com.kaleyra.video_sdk.call.bottomsheetnew.CallScreenScaffold
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.HSheetDragActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions.VSheetDragActions
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.HSheetActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.VSheetActions
import com.kaleyra.video_sdk.call.callactionnew.AnswerButtonMultiplier
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
import com.kaleyra.video_sdk.extensions.ModifierExtensions.animatePlacement

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
): ImmutableList<@Composable (label: Boolean, modifier: Modifier) -> Unit> {
    with(actions) {
        val hangUpAction: @Composable (Boolean, Modifier) -> Unit =
            { _, modifier ->
                HangUpAction(
                    enabled = hangUpAction.isEnabled,
                    onClick = onHangUpClick,
                    modifier = modifier.animatePlacement()
                )
            }
        val micAction: @Composable ((Boolean, Modifier) -> Unit)? = microphoneAction?.let {
            { _, modifier ->
                MicAction(
                    checked = it.isToggled,
                    enabled = it.isEnabled,
                    warning = it.state == InputCallAction.State.Warning,
                    error = it.state == InputCallAction.State.Error,
                    onCheckedChange = onMicToggled,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val cameraAction: @Composable ((Boolean, Modifier) -> Unit)? = cameraAction?.let {
            { _, modifier ->
                CameraAction(
                    checked = it.isToggled,
                    enabled = it.isEnabled,
                    warning = it.state == InputCallAction.State.Warning,
                    error = it.state == InputCallAction.State.Error,
                    onCheckedChange = onCameraToggled,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val flipCameraAction: @Composable ((Boolean, Modifier) -> Unit)? = flipCameraAction?.let {
            { label, modifier ->
                FlipCameraAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFlipCameraClick,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val audioAction: @Composable ((Boolean, Modifier) -> Unit)? = audioAction?.let {
            { label, modifier ->
                AudioAction(
                    audioDevice = it.audioDevice,
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onAudioClick,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val chatAction: @Composable ((Boolean, Modifier) -> Unit)? = chatAction?.let {
            { label, modifier ->
                ChatAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onChatClick,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val fileShareAction: @Composable ((Boolean, Modifier) -> Unit)? = fileShareAction?.let {
            { label, modifier ->
                FileShareAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFileShareClick,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val screenShareAction: @Composable ((Boolean, Modifier) -> Unit)? = screenShareAction?.let {
            { label, modifier ->
                ScreenShareAction(
                    label = label,
                    enabled = it.isEnabled,
                    checked = it.isToggled,
                    onCheckedChange = onScreenShareToggle,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val whiteboardAction: @Composable ((Boolean, Modifier) -> Unit)? = whiteboardAction?.let {
            { label, modifier ->
                WhiteboardAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onWhiteboardClick,
                    modifier = modifier.animatePlacement()
                )
            }
        }
        val virtualBackgroundAction: @Composable ((Boolean, Modifier) -> Unit)? =
            virtualBackgroundAction?.let {
                { label, modifier ->
                    VirtualBackgroundAction(
                        label = label,
                        enabled = it.isEnabled,
                        onClick = onVirtualBackgroundClick,
                        modifier = modifier.animatePlacement()
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
private fun scaffoldPaddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
    return PaddingValues(
        start = systemBarsPadding.calculateStartPadding(layoutDirection) + horizontal,
        top = systemBarsPadding.calculateTopPadding() + vertical,
        end = systemBarsPadding.calculateEndPadding(layoutDirection) + horizontal,
        bottom = systemBarsPadding.calculateBottomPadding() + vertical
    )
}

@Composable
fun CallScreen(
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
    var sheetDragActions: ImmutableList<@Composable (Boolean, Modifier) -> Unit> by remember {
        mutableStateOf(ImmutableList())
    }
    val hasSheetDragContent by remember {
        derivedStateOf {
            sheetDragActions.value.isNotEmpty()
        }
    }
    var displayAnswerButton by remember { mutableStateOf(true) }
    val sheetState = rememberCallSheetState()
    CallScreenScaffold(
        sheetState = sheetState,
        paddingValues = scaffoldPaddingValues(horizontal = 4.dp, vertical = 12.dp),
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
                val itemsPerRow =
                    actionsComposables.count() - sheetDragActions.count() + if (displayAnswerButton) AnswerButtonMultiplier else 1
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
                sheetState = sheetState,
                actions = actionsComposables,
                maxActions = 8,
                showAnswerAction = displayAnswerButton,
                onAnswerActionClick = { displayAnswerButton = false },
                onActionsPlaced = { itemsPlaced ->
                    sheetDragActions = ImmutableList(actionsComposables.value.takeLast(actionsComposables.count() - itemsPlaced))
                },
                modifier = if (hasSheetDragContent) {
                    Modifier.padding(start = 14.dp, top = 2.dp, end = 14.dp, bottom = 14.dp)
                } else {
                    Modifier.padding(14.dp)
                }.animateContentSize(),
            )
        },
        containerColor = Color.DarkGray,
        sheetDragHandle = if (hasSheetDragContent) {
            { CallBottomSheetDefaults.DragHandle() }
        } else null
    ) { paddingValues ->

    }
}

@Composable
fun VCallScreen(
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
    var sheetDragActions: ImmutableList<@Composable (Boolean, Modifier) -> Unit> by remember {
        mutableStateOf(ImmutableList())
    }
    val hasSheetDragContent by remember {
        derivedStateOf {
            sheetDragActions.value.isNotEmpty()
        }
    }
    var displayAnswerButton by remember { mutableStateOf(true) }
    val sheetState = rememberCallSheetState()
    CallScreenLandscapeScaffold(
        sheetState = sheetState,
        paddingValues = scaffoldPaddingValues(horizontal = 4.dp, vertical = 12.dp),
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
                val itemsPerColumn = actionsComposables.count() - sheetDragActions.count() + if (displayAnswerButton) AnswerButtonMultiplier else 1
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
                sheetState = sheetState,
                actions = actionsComposables,
                maxActions = 8,
                showAnswerAction = displayAnswerButton,
                onAnswerActionClick = { displayAnswerButton = false },
                onActionsPlaced = { itemsPlaced ->
                    sheetDragActions = ImmutableList(actionsComposables.value.takeLast(actionsComposables.count() - itemsPlaced))
                },
                modifier = if (hasSheetDragContent) {
                    Modifier.padding(start = 2.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
                } else {
                    Modifier.padding(14.dp)
                }.animateContentSize(),
            )
        },
        containerColor = Color.DarkGray,
        sheetDragHandle = if (hasSheetDragContent) {
            { CallBottomSheetDefaults.VerticalDragHandle() }
        } else null
    ) { paddingValues ->

    }
}