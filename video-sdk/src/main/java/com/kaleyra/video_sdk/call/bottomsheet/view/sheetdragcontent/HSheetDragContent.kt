package com.kaleyra.video_sdk.call.bottomsheet.view.sheetdragcontent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ActivityExtensions.unlockDevice
import com.kaleyra.video_sdk.call.bottomsheet.model.CallActionUI
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.view.CallSheetItem
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactions.view.AnswerActionExtendedMultiplier
import com.kaleyra.video_sdk.call.callactions.view.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callactions.view.HangUpActionExtendedMultiplier
import com.kaleyra.video_sdk.call.callactions.view.HangUpActionMultiplier
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import kotlin.math.max

internal val HSheetDragHorizontalPadding = SheetItemsSpacing
internal val HSheetDragVerticalPadding = 20.dp

private const val MaxHSheetDragItems = 5

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun HSheetDragContent(
    viewModel: CallActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    callActions: ImmutableList<CallActionUI>,
    isLargeScreen: Boolean,
    inputPermissions: InputPermissions = InputPermissions(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onModularComponentRequest: (ModularComponent) -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetActionsCount by remember {
        derivedStateOf {
            uiState.actionList.value.count { it !is HangUpAction }
        }
    }
    val hasHangUpAction by remember {
        derivedStateOf {
            uiState.actionList.value.find { it is HangUpAction } != null
        }
    }
    val isRinging by remember {
        derivedStateOf { uiState.isRinging }
    }
    val hangUpActionSlots = remember(hasHangUpAction, isLargeScreen) {
        when {
            !hasHangUpAction -> 0
            isLargeScreen -> HangUpActionExtendedMultiplier
            else -> HangUpActionMultiplier
        }
    }
    val answerActionSlots = remember(isLargeScreen, isRinging) {
        when {
            !isRinging -> 0
            isLargeScreen -> AnswerActionExtendedMultiplier
            else -> AnswerActionMultiplier
        }
    }
    val moreSlots = if (isRinging) 0 else 1
    val itemsPerRow by remember(callActions, hangUpActionSlots, answerActionSlots, moreSlots) {
        derivedStateOf {
            max(1, sheetActionsCount - callActions.count() + hangUpActionSlots + answerActionSlots + moreSlots)
        }
    }

    HSheetDragContent(
        modifier = modifier,
        callActions = callActions,
        itemsPerRow = itemsPerRow,
        inputPermissions = inputPermissions,
        contentPadding = contentPadding,
        onHangUpClick = viewModel::hangUp,
        onMicToggle = remember(viewModel, inputPermissions) { lambda@ {
            val micPermission = inputPermissions.micPermission ?: return@lambda
            if (micPermission.status.isGranted) viewModel.toggleMic(activity)
            else micPermission.launchPermissionRequest()
        } },
        onCameraToggle = remember(viewModel, inputPermissions) { lambda@ {
            val cameraPermission = inputPermissions.cameraPermission ?: return@lambda
            if (uiState.isCameraUsageRestricted || cameraPermission.status.isGranted) viewModel.toggleCamera(activity)
            else cameraPermission.launchPermissionRequest()
        } },
        onScreenShareToggle = remember(viewModel) {
            { if (!viewModel.tryStopScreenShare()) onModularComponentRequest(ModularComponent.ScreenShare) }
        },
        onFlipCameraClick = viewModel::switchCamera,
        onAudioClick = { onModularComponentRequest(ModularComponent.Audio) },
        onChatClick = remember(viewModel, isLargeScreen) {
            {
                if (isLargeScreen) onModularComponentRequest(ModularComponent.Chat)
                else activity.unlockDevice(onUnlocked = { viewModel.showChat(activity) })
            }
        },
        onFileShareClick = {
            onModularComponentRequest(ModularComponent.FileShare)
            viewModel.clearFileShareBadge()
        },
        onWhiteboardClick = { onModularComponentRequest(ModularComponent.Whiteboard) },
        onVirtualBackgroundToggle = { onModularComponentRequest(ModularComponent.VirtualBackground) }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
internal fun HSheetDragContent(
    callActions: ImmutableList<CallActionUI>,
    onHangUpClick: () -> Unit,
    onMicToggle: (Boolean) -> Unit,
    onCameraToggle: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onVirtualBackgroundToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    modifier: Modifier = Modifier,
    itemsPerRow: Int = MaxHSheetDragItems,
    labels: Boolean = true,
    inputPermissions: InputPermissions = InputPermissions(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val shouldExtendLastButton = callActions.count() / itemsPerRow < 1

    val chunkedActions = remember(callActions, itemsPerRow) {
        callActions.value.chunked(itemsPerRow).flatten()
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(itemsPerRow),
        verticalArrangement = Arrangement.spacedBy(HSheetDragVerticalPadding),
        horizontalArrangement = Arrangement.spacedBy(HSheetDragHorizontalPadding),
        contentPadding = contentPadding,
        modifier = modifier
    ) {
       itemsIndexed(
           key = { _, item -> item.id },
           span = { index, _ ->
               if (shouldExtendLastButton && index == chunkedActions.size - 1) GridItemSpan(itemsPerRow - (chunkedActions.size % itemsPerRow - 1))
               else GridItemSpan(1)
           },
           items = chunkedActions
       ) { _, callAction, ->
           CallSheetItem(
               callAction = callAction,
               modifier = Modifier.animateItemPlacement(),
               label = labels,
               extended = false,
               inputPermissions = inputPermissions,
               onHangUpClick = onHangUpClick,
               onMicToggle = onMicToggle,
               onCameraToggle = onCameraToggle,
               onScreenShareToggle = onScreenShareToggle,
               onFlipCameraClick = onFlipCameraClick,
               onAudioClick = onAudioClick,
               onChatClick = onChatClick,
               onFileShareClick = onFileShareClick,
               onWhiteboardClick = onWhiteboardClick,
               onVirtualBackgroundToggle = onVirtualBackgroundToggle
           )
       }
    }
}