package com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.ModalSheetComponent
import com.kaleyra.video_sdk.call.screennew.ScreenShareAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SheetPanelContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    callActions: ImmutableList<CallActionUI>,
    onModalSheetComponentRequest: (ModalSheetComponent) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()

    SheetPanelContent(
        modifier = modifier,
        callActions = callActions,
        onItemClick = remember(viewModel) {
            { callAction ->
                when (callAction) {
                    is ScreenShareAction -> {
                        if (!viewModel.tryStopScreenShare()) onModalSheetComponentRequest(ModalSheetComponent.ScreenShare)
                    }

                    is FlipCameraAction -> viewModel.switchCamera()
                    is AudioAction -> onModalSheetComponentRequest(ModalSheetComponent.Audio)
                    is ChatAction -> activity.unlockDevice { viewModel.showChat(activity) }
                    is FileShareAction -> onModalSheetComponentRequest(ModalSheetComponent.FileShare)
                    is WhiteboardAction -> onModalSheetComponentRequest(ModalSheetComponent.Whiteboard)
                    is VirtualBackgroundAction -> onModalSheetComponentRequest(ModalSheetComponent.VirtualBackground)
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
                        VirtualBackgroundAction()
                    )
                ),
                onItemClick = { }
            )
        }
    }
}