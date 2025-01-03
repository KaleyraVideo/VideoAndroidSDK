package com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SheetPanelContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    screenShareViewModel: ScreenShareViewModel = viewModel(factory = ScreenShareViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    callActions: ImmutableList<CallActionUI>,
    onModularComponentRequest: (ModularComponent) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    var screenShareMode: ScreenShareAction? by remember { mutableStateOf(null) }
    screenShareMode = callActions.value.firstOrNull { it is ScreenShareAction } as? ScreenShareAction

    SheetPanelContent(
        modifier = modifier,
        callActions = callActions,
        onItemClick = remember(viewModel) {
            { callAction ->
                when (callAction) {
                    is ScreenShareAction -> {
                        if (!viewModel.tryStopScreenShare()) {

                            when (screenShareMode) {
                                null -> Unit
                                is ScreenShareAction.UserChoice -> onModularComponentRequest(ModularComponent.ScreenShare)
                                is ScreenShareAction.App -> screenShareViewModel.shareApplicationScreen(activity, {}, {})
                                is ScreenShareAction.WholeDevice -> {
                                    onAskInputPermissions(true)
                                    activity.unlockDevice(
                                        onUnlocked = {
                                            screenShareViewModel.shareDeviceScreen(
                                                activity,
                                                onScreenSharingStarted = {
                                                    onAskInputPermissions(false)
                                                },
                                                onScreenSharingAborted = {
                                                    onAskInputPermissions(false)
                                                }
                                            )
                                        },
                                        onDismiss = {
                                            onAskInputPermissions(false)
                                        })
                                }
                            }
                        }
                    }

                    is FlipCameraAction -> viewModel.switchCamera()
                    is AudioAction -> onModularComponentRequest(ModularComponent.Audio)
                    is ChatAction -> onModularComponentRequest(ModularComponent.Chat)
                    is FileShareAction -> onModularComponentRequest(ModularComponent.FileShare)
                    is WhiteboardAction -> onModularComponentRequest(ModularComponent.Whiteboard)
                    is VirtualBackgroundAction -> onModularComponentRequest(ModularComponent.VirtualBackground)
                }
            }
        }
    )
}

@Composable
internal fun SheetPanelContent(
    callActions: ImmutableList<CallActionUI>,
    onItemClick: (CallActionUI) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.width(320.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 12.dp),
            modifier = modifier
        ) {
            items(items = callActions.value, key = { it.id }) {
                SheetPanelItem(
                    callAction = it,
                    modifier = Modifier
                        .clickable(
                            role = Role.Button,
                            onClick = { onItemClick(it) }
                        )
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelContentPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelContent(
                callActions = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        AudioAction(),
                        ChatAction(notificationCount = 4),
                        FileShareAction(notificationCount = 2),
                        WhiteboardAction(notificationCount = 3),
                        ScreenShareAction.WholeDevice(),
                        VirtualBackgroundAction()
                    )
                ),
                onItemClick = { }
            )
        }
    }
}