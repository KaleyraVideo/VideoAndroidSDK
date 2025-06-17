@file:OptIn(ExperimentalPermissionsApi::class)

package com.kaleyra.video_sdk.call.bottomsheet.view.sheetpanel

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.SignatureAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SheetPanelContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    screenShareViewModel: ScreenShareViewModel = viewModel(factory = ScreenShareViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    callActions: ImmutableList<CallActionUI>,
    onModularComponentRequest: (ModularComponent) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    inputPermissions: InputPermissions = InputPermissions(),
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current.findActivity()
    var screenShareMode: ScreenShareAction? by remember { mutableStateOf(null) }
    screenShareMode =
        callActions.value.firstOrNull { it is ScreenShareAction } as? ScreenShareAction

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SheetPanelContent(
        modifier = modifier,
        callActions = callActions,
        onItemClick = remember(viewModel) {
            { callAction ->
                if (!callAction.isEnabled) Unit
                else when (callAction) {
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
                    is SignatureAction -> onModularComponentRequest(ModularComponent.SignDocuments)
                    is WhiteboardAction -> onModularComponentRequest(ModularComponent.Whiteboard)
                    is VirtualBackgroundAction -> onModularComponentRequest(ModularComponent.VirtualBackground)
                    is HangUpAction -> viewModel.hangUp()
                    is MicAction -> {
                        if (inputPermissions.micPermission == null) Unit
                        else if (inputPermissions.micPermission!!.status.isGranted) viewModel.toggleMic(activity)
                        else inputPermissions.micPermission!!.launchPermissionRequest()
                    }

                    is CameraAction -> {
                        if (inputPermissions.cameraPermission == null) Unit
                        else if (uiState.isCameraUsageRestricted || inputPermissions.cameraPermission!!.status.isGranted) viewModel.toggleCamera(activity)
                        else inputPermissions.cameraPermission!!.launchPermissionRequest()
                    }

                    is CustomAction -> {
                        callAction.onClick()
                    }
                }
            }
        }
    )
}

@Composable
internal fun SheetPanelContent(
    callActions: ImmutableList<CallActionUI>,
    onItemClick: (CallActionUI) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = Modifier.width(320.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
            modifier = modifier
        ) {
            items(items = callActions.value, key = { it.id }) {
                val interactionSource = remember { MutableInteractionSource() }
                SheetPanelItem(
                    callAction = it,
                    modifier = Modifier
                        .highlightOnFocus(interactionSource)
                        .height(36.dp)
                        .clickable(
                            onClick = { onItemClick(it) },
                            indication = null,
                            interactionSource = interactionSource
                        )
                        .padding(start = 0.dp, end = 9.dp, top = 0.dp, bottom = 0.dp)
                )
                Spacer(Modifier.size(16.dp))
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
                        SignatureAction(notificationCount = 3),
                        WhiteboardAction(notificationCount = 4),
                        ScreenShareAction.WholeDevice(),
                        VirtualBackgroundAction(),
                        MicAction(),
                        CameraAction(),
                        HangUpAction(),
                        CustomAction(icon = R.drawable.kaleyra_icon_reply, buttonTexts = CustomCallAction.ButtonTexts("Custom", "Custom")),
                    )
                ),
                onItemClick = { }
            )
        }
    }
}