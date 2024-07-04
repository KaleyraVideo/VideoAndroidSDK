package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.view.CallAppBar
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.CameraMessageText
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.MicMessageText
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.HSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel.SheetPanelContent
import com.kaleyra.video_sdk.call.bottomsheetnew.streammenu.HStreamMenuContent
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoComponent
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.call.streamnew.StreamComponent
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import kotlinx.coroutines.launch
import kotlin.math.max

internal val PanelTestTag = "PanelTestTag"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VCallScreen(
    windowSizeClass: WindowSizeClass,
    sheetState: CallSheetState,
    modalSheetComponent: ModalSheetComponent?,
    modalSheetState: SheetState,
    inputMessage: InputMessage?,
    onBackPressed: () -> Unit,
    onModalSheetComponentChange: (ModalSheetComponent?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLargeScreen = windowSizeClass.widthSizeClass in setOf(
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded
    )

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
    val onFileShareClick =
        remember { { onModalSheetComponentChange(ModalSheetComponent.FileShare) } }
    val onWhiteboardClick =
        remember { { onModalSheetComponentChange(ModalSheetComponent.Whiteboard) } }
    val onVirtualBackgroundClick =
        remember { { onModalSheetComponentChange(ModalSheetComponent.VirtualBackground) } }
    val onScreenShareClick =
        remember { { onModalSheetComponentChange(ModalSheetComponent.ScreenShare) } }

    val onMicClick = rememberOnMicClick(callActionsViewModel)
    val onCameraToggle = rememberOnCameraClick(callActionsViewModel)
    val onChatClick = rememberOnChatClick(callActionsViewModel)
    val onHangUpClick = rememberOnHangUpClick(callActionsViewModel)
    val onScreenShareToggle = rememberOnScreenShareClick(callActionsViewModel, onScreenShareClick)

    var selectedStream by remember { mutableStateOf<StreamUi?>(null) }
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    // TODO do a resize test when writing test
    val hasSheetDragContent by remember(isLargeScreen) { derivedStateOf { !isLargeScreen && selectedStream == null && sheetDragActions.value.isNotEmpty() } }
    // TODO test reset on resize
    var showSheetPanelContent by remember(isLargeScreen) { mutableStateOf(false) }

    VCallScreenScaffold(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    showSheetPanelContent = false
                    selectedStream = null
                }
            }
            .clearAndSetSemantics {},
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 4.dp, vertical = 8.dp),
        topAppBar = {
            CallAppBar(
                // TODO test this
                onParticipantClick = { /*TODO*/ },
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetPanelContent = {
            if (isLargeScreen) {
                AnimatedVisibility(
                    visible = showSheetPanelContent,
                    enter = fadeIn(tween()),
                    exit = fadeOut(tween())
                ) {
                    SheetPanelContent(
                        items = sheetDragActions,
                        modifier = Modifier.testTag(PanelTestTag),
                        onItemClick = { callAction ->
                            when (callAction) {
                                is FlipCameraAction -> callActionsViewModel.switchCamera()
                                is AudioAction -> onAudioClick()
                                is ChatAction -> onChatClick()
                                is FileShareAction -> onFileShareClick()
                                is WhiteboardAction -> onWhiteboardClick()
                                is VirtualBackgroundAction -> onVirtualBackgroundClick()
                            }
                        }
                    )
                }
            }
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val sheetActionsCount = callActionsUiState.actionList.count()
                val answerActionSlots =
                    if (callActionsUiState.isRinging) AnswerActionMultiplier else 1
                val itemsPerRow =
                    max(1, sheetActionsCount - sheetDragActions.count() + answerActionSlots)
                HSheetDragContent(
                    callActions = sheetDragActions,
                    itemsPerRow = itemsPerRow,
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isLargeScreen) {
                            Box(
                                modifier = Modifier
                                    .width(250.dp)
                                    .animateContentSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // TODO test this
                                InputMessageHost(
                                    inputMessage = inputMessage,
                                    micMessage = { enabled -> MicMessageText(enabled) },
                                    cameraMessage = { enabled -> CameraMessageText(enabled) }
                                )
                            }
                        }
                        Box(Modifier.animateContentSize()) {
                            val isSheetExpanded by remember(sheetState) {
                                derivedStateOf {
                                    sheetState.targetValue == CallSheetValue.Expanded
                                }
                            }
                            HSheetContent(
                                callActions = callActionsUiState.actionList,
                                maxActions = if (isLargeScreen) LargeScreenMaxActions else CompactScreenMaxActions,
                                showAnswerAction = callActionsUiState.isRinging,
                                isMoreToggled = isSheetExpanded,
                                isLargeScreen = isLargeScreen,
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
                                onMoreToggle = { isSheetCollapsed ->
                                    if (hasSheetDragContent) onChangeSheetState(isSheetCollapsed)
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
                    }
                } else {
                    HStreamMenuContent(
                        selectedStream = currentlySelectedStream,
                        onDismiss = { selectedStream = null }
                    )
                }
            }
        },
        containerColor = if (!isSystemInDarkTheme()) Color(0xFFF9F9FF) else Color(0xFF000000),
        // TODO test this
        sheetDragHandle = (@Composable { InputMessageHandle(inputMessage) }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val top = paddingValues.calculateTopPadding()
        val left = paddingValues.calculateLeftPadding(layoutDirection)
        val right = paddingValues.calculateRightPadding(layoutDirection)

        StreamComponent(
            windowSizeClass = windowSizeClass,
            highlightedStream = selectedStream,
            // TODO test this
            onStreamClick = { stream -> selectedStream = stream },
            // TODO test this
            onStopScreenShareClick = callActionsViewModel::tryStopScreenShare,
            // TODO test this
            onMoreParticipantClick = {},
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(
                    start = left,
                    top = top,
                    end = right,
                    bottom = 116.dp
                )
                .padding(top = 14.dp)
        )

        CallInfoComponent(
            modifier = Modifier
                .padding(top = top)
                .padding(horizontal = 8.dp, vertical = 48.dp)
        )

        CallScreenModalSheet(
            modalSheetComponent = modalSheetComponent,
            sheetState = modalSheetState,
            onRequestDismiss = { onModalSheetComponentChange(null) },
            onUserMessageActionClick = {}
        )
    }
}

