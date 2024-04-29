package com.kaleyra.video_sdk.call.screennew

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.CallInfoBar
import com.kaleyra.video_sdk.call.audiooutput.model.AudioDeviceUi
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
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.launch

interface CallActionUI {
    val isEnabled: Boolean
}

interface ToggleableCallAction : CallActionUI {
    val isToggled: Boolean
}

interface InputCallAction : ToggleableCallAction {
    val state: State

    enum class State {
        Ok,
        Warning,
        Error
    }
}

@Immutable
data class HangUpAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class FlipCameraAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class AudioAction(
    val audioDevice: AudioDeviceUi,
    override val isEnabled: Boolean = true
) : CallActionUI

@Immutable
data class ChatAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class FileShareAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class WhiteboardAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class VirtualBackgroundAction(override val isEnabled: Boolean = true) : CallActionUI

@Immutable
data class MicAction(
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean,
    override val isToggled: Boolean
) : InputCallAction

@Immutable
data class CameraAction(
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean,
    override val isToggled: Boolean
) : InputCallAction

@Immutable
data class ScreenShareAction(
    override val state: InputCallAction.State = InputCallAction.State.Ok,
    override val isEnabled: Boolean,
    override val isToggled: Boolean
) : InputCallAction

@Immutable
data class CallActionsUI(
    val hangUpAction: HangUpAction? = null,
    val microphoneAction: MicAction? = null,
    val cameraAction: CameraAction? = null,
    val flipCameraAction: FlipCameraAction? = null,
    val audioAction: AudioAction? = null,
    val chatAction: ChatAction? = null,
    val fileShareAction: FileShareAction? = null,
    val screenShareAction: ScreenShareAction? = null,
    val whiteboardAction: WhiteboardAction? = null,
    val virtualBackgroundAction: VirtualBackgroundAction? = null,
)

@Composable
fun ActionsList(
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
): ImmutableList<@Composable ((Modifier, Boolean) -> Unit)> {
    with(actions) {
        val hangUp: @Composable ((Modifier, Boolean) -> Unit)? = hangUpAction?.let {
            { modifier, _ -> HangUpAction(onClick = onHangUpClick, modifier = modifier) }
        }
        val microphone: @Composable ((Modifier, Boolean) -> Unit)? = microphoneAction?.let {
            { modifier, _ ->
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
        val camera: @Composable ((Modifier, Boolean) -> Unit)? = cameraAction?.let {
            { modifier, _ ->
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
        val flipCamera: @Composable ((Modifier, Boolean) -> Unit)? = flipCameraAction?.let {
            { modifier, label ->
                FlipCameraAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFlipCameraClick,
                    modifier = modifier
                )
            }
        }
        val audio: @Composable ((Modifier, Boolean) -> Unit)? = audioAction?.let {
            { modifier, label ->
                AudioAction(
                    audioDevice = it.audioDevice,
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onAudioClick,
                    modifier = modifier
                )
            }
        }
        val chat: @Composable ((Modifier, Boolean) -> Unit)? = chatAction?.let {
            { modifier, label ->
                ChatAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onChatClick,
                    modifier = modifier
                )
            }
        }
        val fileShare: @Composable ((Modifier, Boolean) -> Unit)? = fileShareAction?.let {
            { modifier, label ->
                FileShareAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onFileShareClick,
                    modifier = modifier
                )
            }
        }
        val screenShare: @Composable ((Modifier, Boolean) -> Unit)? = screenShareAction?.let {
            { modifier, label ->
                ScreenShareAction(
                    label = label,
                    enabled = it.isEnabled,
                    checked = it.isToggled,
                    onCheckedChange = onScreenShareToggle,
                    modifier = modifier
                )
            }
        }
        val whiteboard: @Composable ((Modifier, Boolean) -> Unit)? = whiteboardAction?.let {
            { modifier, label ->
                WhiteboardAction(
                    label = label,
                    enabled = it.isEnabled,
                    onClick = onWhiteboardClick,
                    modifier = modifier
                )
            }
        }
        val virtualBackground: @Composable ((Modifier, Boolean) -> Unit)? =
            virtualBackgroundAction?.let {
                { modifier, label ->
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
                hangUp,
                microphone,
                camera,
                flipCamera,
                audio,
                chat,
                fileShare,
                screenShare,
                whiteboard,
                virtualBackground
            )
        )
    }
}


@Composable
fun CallScreen(
    actions: CallActionsUI
) {
    var sheetDragActions by remember {
        mutableStateOf<ImmutableList<@Composable (Modifier, Boolean) -> Unit>>(ImmutableList())
    }
    val scope = rememberCoroutineScope()
    val sheetState = rememberCallSheetState()
    CallScreenScaffold(
        sheetState = sheetState,
        containerColor = Color.LightGray,
        paddingValues = PaddingValues(16.dp),
        topAppBar = {
            CallInfoBar(
                title = "title",
                logo = Logo(),
                recording = false,
                participantCount = 3,
                onParticipantClick = { },
                onBackPressed = {}
            )
        },
        sheetPanelContent = null,
        sheetDragContent = {
            if (sheetDragActions.value.isNotEmpty()) {
                val itemsPerRow = actions.count() - sheetDragActions.count() + 1
                SheetDragContent(
                    modifier = Modifier.padding(16.dp),
                    dragActions = sheetDragActions,
                    itemsPerRow = itemsPerRow
                )
            }
        },
        sheetContent = {
            SheetContent(
                modifier = Modifier.padding(16.dp),
                actions = actions,
                // false -> dialing or drag actions are 0
                showMoreItem = false,
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
                    sheetDragActions =
                        ImmutableList(actions.value.takeLast(actions.count() - itemsCount))
                }
            )
        },
        sheetDragHandle = if (sheetDragActions.value.isNotEmpty()) {
            { CallBottomSheetDefaults.DragHandle() }
        } else null
    ) { paddingValues ->

    }
}