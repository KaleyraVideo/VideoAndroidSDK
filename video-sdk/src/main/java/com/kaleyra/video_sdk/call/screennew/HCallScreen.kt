package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.view.CallAppBarComponent
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.HSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.VSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.VSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.streammenu.HStreamMenuContent
import com.kaleyra.video_sdk.call.bottomsheetnew.streammenu.VStreamMenuContent
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.streamnew.StreamComponent
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import kotlinx.coroutines.launch
import kotlin.math.max


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
internal fun HCallScreen(
    windowSizeClass: WindowSizeClass,
    sheetState: CallSheetState,
    modalSheetComponent: ModalSheetComponent?,
    modalSheetState: SheetState,
    onBackPressed: () -> Unit,
    onModalSheetComponentChange: (ModalSheetComponent?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val onChangeSheetState: (Boolean) -> Unit = remember(sheetState) {
        { isSheetCollapsed: Boolean ->
            scope.launch {
                if (isSheetCollapsed) sheetState.expand()
                else sheetState.collapse()
            }
        }
    }

    val callActionsViewModel = viewModel<CallActionsViewModel>(factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
    val callActionsUiState by callActionsViewModel.uiState.collectAsStateWithLifecycle()

    val onAudioClick = remember { { onModalSheetComponentChange(ModalSheetComponent.Audio) } }
    val onFileShareClick = remember { { onModalSheetComponentChange(ModalSheetComponent.FileShare) } }
    val onWhiteboardClick = remember { { onModalSheetComponentChange(ModalSheetComponent.Whiteboard) } }
    val onVirtualBackgroundClick = remember { { onModalSheetComponentChange(ModalSheetComponent.VirtualBackground) } }
    val onScreenShareClick = remember { { onModalSheetComponentChange(ModalSheetComponent.ScreenShare) } }

    val onMicClick = rememberOnMicClick(callActionsViewModel)
    val onCameraToggle = rememberOnCameraClick(callActionsViewModel)
    val onChatClick = rememberOnChatClick(callActionsViewModel)
    val onHangUpClick = rememberOnHangUpClick(callActionsViewModel)
    val onScreenShareToggle = rememberOnScreenShareClick(callActionsViewModel, onScreenShareClick)

    var selectedStream by remember { mutableStateOf<StreamUi?>(null) }
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember { derivedStateOf { selectedStream == null && sheetDragActions.value.isNotEmpty() } }

    HCallScreenScaffold(
        modifier = modifier,
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 8.dp, vertical = 4.dp),
        topAppBar = {
            CallAppBarComponent(
                // TODO test this
                onParticipantClick = { /*TODO*/ },
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val sheetActionsCount = callActionsUiState.actionList.count()
                val itemsPerColumn = sheetActionsCount - sheetDragActions.count() + 1
                VSheetDragContent(
                    callActions = sheetDragActions,
                    itemsPerColumn = itemsPerColumn,
                    onHangUpClick = onHangUpClick,
                    onMicToggled = onMicClick,
                    onCameraToggled = onCameraToggle,
                    onScreenShareToggle = onScreenShareToggle,
                    onFlipCameraClick = callActionsViewModel::switchCamera,
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
            AnimatedContent(
                targetState = selectedStream,
                contentAlignment = Alignment.Center,
                label = "sheet content"
            ) { currentlySelectedStream ->
                if (currentlySelectedStream == null) {
                    Box(Modifier.animateContentSize()) {
                            val isSheetExpanded by remember(sheetState) {
                                derivedStateOf {
                                    sheetState.targetValue == CallSheetValue.Expanded
                                }
                            }
                            VSheetContent(
                                callActions = callActionsUiState.actionList,
                                maxActions = CompactScreenMaxActions,
                                showAnswerAction = callActionsUiState.isRinging,
                                isMoreToggled = isSheetExpanded,
                                onActionsPlaced = { itemsPlaced ->
                                    val actions = callActionsUiState.actionList.value
                                    val dragActions = actions.takeLast(max(0, actions.count() - itemsPlaced))
                                    sheetDragActions = dragActions.toImmutableList()
                                },
                                onAnswerClick = callActionsViewModel::accept,
                                onHangUpClick = onHangUpClick,
                                onMicToggle = onMicClick,
                                onCameraToggle = onCameraToggle,
                                onScreenShareToggle = onScreenShareToggle,
                                onFlipCameraClick = callActionsViewModel::switchCamera,
                                onAudioClick = onAudioClick,
                                onChatClick = onChatClick,
                                onFileShareClick = onFileShareClick,
                                onWhiteboardClick = onWhiteboardClick,
                                onVirtualBackgroundClick = onVirtualBackgroundClick,
                                onMoreToggle = onChangeSheetState,
                                modifier = Modifier
                                    .padding(
                                        start = 5.dp,
                                        top = 14.dp,
                                        end = 14.dp,
                                        bottom = 14.dp
                                    )
                            )
                        }
                } else {
                    VStreamMenuContent(
                        selectedStream = currentlySelectedStream,
                        onDismiss = { selectedStream = null },
                        modifier = Modifier.testTag(StreamMenuContentTestTag)
                    )
                }
            }
        },
        containerColor = if (!isSystemInDarkTheme()) Color(0xFFF9F9FF) else Color(0xFF000000),
        sheetDragHandle = (@Composable { CallBottomSheetDefaults.VDragHandle() }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val top = paddingValues.calculateTopPadding()
        val left = paddingValues.calculateLeftPadding(layoutDirection)
        val bottom = paddingValues.calculateBottomPadding()

        Box(
            modifier = Modifier
                .pointerInteropFilter {
                    // TODO test this
                    when {
                        selectedStream != null -> {
                            selectedStream = null
                            true
                        }
                        else -> false
                    }
                }
                .clearAndSetSemantics {}
        ) {
            StreamComponent(
                windowSizeClass = windowSizeClass,
                highlightedStream = selectedStream,
                onStreamClick = { stream -> selectedStream = stream },
                onStopScreenShareClick = callActionsViewModel::tryStopScreenShare,
                // TODO test this
                onMoreParticipantClick = {},
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(
                        start = left,
                        top = top,
                        end = 116.dp,
                        bottom = bottom,
                    )
                    .padding(top = 14.dp)
            )

            CallInfoComponent(
                modifier = Modifier
                    .padding(top = top)
                    .padding(horizontal = 8.dp, vertical = 48.dp)
            )

            InputMessageHost(
                inputMessage = null,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.BottomCenter)
            )

            CallScreenModalSheet(
                modalSheetComponent = modalSheetComponent,
                sheetState = modalSheetState,
                onRequestDismiss = { onModalSheetComponentChange(null) },
                // TODO test this
                onUserMessageActionClick = { }
            )
        }
    }
}