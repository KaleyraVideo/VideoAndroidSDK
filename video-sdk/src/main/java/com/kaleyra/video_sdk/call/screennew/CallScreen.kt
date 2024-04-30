package com.kaleyra.video_sdk.call.screennew

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.CallInfoBar
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallScreenScaffold
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.dragcontent.SheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.SheetContent
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
            { _, modifier -> HangUpAction(onClick = onHangUpClick, modifier = modifier) }
        val micAction: @Composable ((Boolean, Modifier) -> Unit)? = microphoneAction?.let {
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
        val cameraAction: @Composable ((Boolean, Modifier) -> Unit)? = cameraAction?.let {
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
        val flipCameraAction: @Composable ((Boolean, Modifier) -> Unit)? = flipCameraAction?.let {
            { label, modifier ->
                FlipCameraAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFlipCameraClick,
                    modifier = modifier
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
                    modifier = modifier
                )
            }
        }
        val chatAction: @Composable ((Boolean, Modifier) -> Unit)? = chatAction?.let {
            { label, modifier ->
                ChatAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onChatClick,
                    modifier = modifier
                )
            }
        }
        val fileShareAction: @Composable ((Boolean, Modifier) -> Unit)? = fileShareAction?.let {
            { label, modifier ->
                FileShareAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFileShareClick,
                    modifier = modifier
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
                    modifier = modifier
                )
            }
        }
        val whiteboardAction: @Composable ((Boolean, Modifier) -> Unit)? = whiteboardAction?.let {
            { label, modifier ->
                WhiteboardAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onWhiteboardClick,
                    modifier = modifier
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
    var showMoreItem by remember { mutableStateOf(true) }
    var sheetDragActions: ImmutableList<@Composable (Boolean, Modifier) -> Unit> by remember {
        mutableStateOf(ImmutableList())
    }
    val hasSheetDragContent by remember {
        derivedStateOf {
            sheetDragActions.value.isNotEmpty()
        }
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberCallSheetState()
    CallScreenScaffold(
        sheetState = sheetState,
        containerColor = Color.LightGray,
        paddingValues = PaddingValues(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 32.dp),
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
            Box(Modifier.size(56.dp))
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val itemsPerRow = actionsComposables.count() - sheetDragActions.count() + 1
                SheetDragContent(
                    modifier = Modifier.padding(14.dp),
                    dragActions = sheetDragActions,
                    itemsPerRow = itemsPerRow
                )
            }
        },
        sheetContent = {
            SheetContent(
                modifier = if (hasSheetDragContent) {
                    Modifier.padding(start = 14.dp, top = 2.dp, end = 14.dp, bottom = 14.dp)
                } else {
                    Modifier.padding(14.dp)
                },
                actions = actionsComposables,
                // false -> dialing or drag actions are 0
                showMoreItem = showMoreItem,
                onMoreItemClick = {
                    scope.launch {
                        if (sheetState.currentValue == CallSheetValue.Expanded) {
                            sheetState.collapse()
                        } else {
                            sheetState.expand()
                        }
                    }
                },
                onItemsPlaced = { itemsCount ->
                    sheetDragActions = ImmutableList(actionsComposables.value.takeLast(actionsComposables.count() - itemsCount))
                    showMoreItem = sheetDragActions.value.isNotEmpty()
                }
            )
        },
        sheetDragHandle = if (hasSheetDragContent) {
            { CallBottomSheetDefaults.DragHandle() }
        } else null
    ) { paddingValues ->

    }
}