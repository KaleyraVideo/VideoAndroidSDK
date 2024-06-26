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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.appbar.CallAppBar
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.CameraMessageText
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.MicMessageText
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.HSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.VSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.HSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.VSheetDragContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel.SheetPanelContent
import com.kaleyra.video_sdk.call.bottomsheetnew.streammenu.HStreamMenuContent
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionMultiplier
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.call.streamnew.StreamComponent
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.launch

// TODO
//  in the viewmodel write the logic:
//  max stream pin
//  first screen share added as pin
//  when stream is no longer in the list clean the fullscreen stream/pinned streams
//

@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    inputMessage: InputMessage?,
    onBackPressed: () -> Unit,
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val sheetState = rememberCallSheetState()

    val scope = rememberCoroutineScope()
    val onChangeSheetState: () -> Unit = remember {
        {
            scope.launch {
                if (sheetState.currentValue == CallSheetValue.Expanded) sheetState.collapse()
                else sheetState.expand()
            }
        }
    }

    KaleyraM3Theme {
        if (isCompactHeight) {
//        HCallScreen(
//            windowSizeClass = windowSizeClass,
//            inputMessage = inputMessage,
//            sheetState = sheetState,
//            onChangeSheetState = onChangeSheetState,
//            onBackPressed = onBackPressed
//        )
        } else {
            VCallScreen(
                windowSizeClass = windowSizeClass,
                sheetState = sheetState,
                inputMessage = inputMessage,
                onChangeSheetState = onChangeSheetState,
                onBackPressed = onBackPressed
            )
        }
    }
}

internal const val CompactScreenMaxActions = 5
internal const val LargeScreenMaxActions = 8

@Composable
internal fun rememberOnMicClick(callActionsViewModel: CallActionsViewModel): (Boolean) -> Unit {
    val activity = LocalContext.current.findActivity()
    return remember(callActionsViewModel) { { _: Boolean -> callActionsViewModel.toggleMic(activity) } }
}

@Composable
internal fun rememberOnCameraClick(callActionsViewModel: CallActionsViewModel): (Boolean) -> Unit {
    val activity = LocalContext.current.findActivity()
    return remember(callActionsViewModel) { { _: Boolean -> callActionsViewModel.toggleCamera(activity) } }
}

@Composable
internal fun rememberOnScreenShareClick(callActionsViewModel: CallActionsViewModel, onStartScreenShareClick: () -> Unit): (Boolean) -> Unit {
    return remember(callActionsViewModel) {
        { _: Boolean ->
            if (!callActionsViewModel.tryStopScreenShare()) onStartScreenShareClick()
        }
    }
}

@Composable
internal fun rememberOnChatClick(callActionsViewModel: CallActionsViewModel): () -> Unit {
    val activity = LocalContext.current.findActivity()
    return remember(callActionsViewModel) { { callActionsViewModel.showChat(activity) } }
}

@Composable
internal fun rememberOnHangUpClick(callActionsViewModel: CallActionsViewModel): () -> Unit {
    return remember(callActionsViewModel) { { callActionsViewModel.hangUp() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VCallScreen(
    windowSizeClass: WindowSizeClass,
    sheetState: CallSheetState,
    inputMessage: InputMessage?,
    onChangeSheetState: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLargeScreen = windowSizeClass.widthSizeClass in setOf(
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded
    )
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var openBottomSheet by remember { mutableStateOf(false) }

    val callActionsViewModel = viewModel<CallActionsViewModel>(
        factory = CallActionsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    )
    val callActionsUiState by callActionsViewModel.uiState.collectAsStateWithLifecycle()

    val onMicClick = rememberOnMicClick(callActionsViewModel)
    val onCameraToggle = rememberOnCameraClick(callActionsViewModel)
    val onChatClick = rememberOnChatClick(callActionsViewModel)
    val onHangUpClick = rememberOnHangUpClick(callActionsViewModel)
    val onAudioClick = remember { {
        scope.launch {
            openBottomSheet = true
        }; Unit
    } }
    val onFileShareClick = remember { { } }
    val onWhiteboardClick = remember { { } }
    val onVirtualBackgroundClick = remember { { } }
    val onScreenShareClick = remember { { } }
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
                title = "9:00",
                logo = Logo(),
                recording = false,
                participantCount = 3,
                onParticipantClick = {  },
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetPanelContent = {
            // TODO test when this is displayed
            if (isLargeScreen) {
                AnimatedVisibility(
                    visible = showSheetPanelContent,
                    enter = fadeIn(tween()),
                    exit = fadeOut(tween())
                ) {
                    Card(Modifier.width(320.dp)) {
                        SheetPanelContent(
                            items = sheetDragActions,
                            onItemClick = { callAction ->
                                // TODO test all the following onClick
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
            }
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val itemsPerRow = callActionsUiState.actionList.count() - sheetDragActions.count() + if (callActionsUiState.isRinging) AnswerActionMultiplier else 1
                HSheetDragContent(
                    callActions = sheetDragActions,
                    itemsPerRow = itemsPerRow,
                    // TODO test all the following onClick
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
                                    .animateContentSize()
                                    .padding(top = 6.dp),
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
                            HSheetContent(
                                callActions = callActionsUiState.actionList,
                                maxActions = if (isLargeScreen) LargeScreenMaxActions else CompactScreenMaxActions,
                                showAnswerAction = callActionsUiState.isRinging,
                                isLargeScreen = isLargeScreen,
                                onActionsPlaced = { itemsPlaced ->
                                    val actions = callActionsUiState.actionList.value
                                    val dragActions = actions.takeLast(actions.count() - itemsPlaced)
                                    sheetDragActions = dragActions.toImmutableList()
                                },
                                onAnswerClick = callActionsViewModel::accept,
                                onHangUpClick = onHangUpClick,
                                onMicToggle = onMicClick,
                                onCameraToggle = onCameraToggle,
                                // TODO test this
                                onScreenShareToggle = onScreenShareToggle,
                                onFlipCameraClick = callActionsViewModel::switchCamera,
                                // TODO test this
                                onAudioClick = onAudioClick,
                                onChatClick = onChatClick,
                                // TODO test this
                                onFileShareClick = onFileShareClick,
                                // TODO test this
                                onWhiteboardClick = onWhiteboardClick,
                                // TODO test this
                                onVirtualBackgroundClick = onVirtualBackgroundClick,
                                // TODO test this
                                onMoreActionClick = {
                                    if (hasSheetDragContent) onChangeSheetState()
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
                    val streamViewModel = viewModel<StreamViewModel>(
                        factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
                    )
                    val streamUiState by streamViewModel.uiState.collectAsStateWithLifecycle()

                    HStreamMenuContent(
                        // TODO test this
                        fullscreen = streamUiState.fullscreenStream?.id == currentlySelectedStream.id,
                        // TODO test this
                        pin = streamUiState.pinnedStreams.value.fastAny { stream -> stream.id == currentlySelectedStream.id },
                        // TODO test this
                        onCancelClick = { selectedStream = null },
                        // TODO test this
                        onFullscreenClick = { isFullscreen ->
                            if (isFullscreen)  streamViewModel.fullscreen(null)
                            else streamViewModel.fullscreen(currentlySelectedStream)
                            selectedStream = null
                        },
                        // TODO test this
                        onPinClick = { isPinned ->
                            if (isPinned) streamViewModel.unpin(currentlySelectedStream)
                            else streamViewModel.pin(currentlySelectedStream)
                            selectedStream = null
                        }
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

        if (openBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { openBottomSheet = false },
                sheetState = modalSheetState,
                shape = RectangleShape,
                dragHandle = null
            ) {
                Text(modifier = Modifier.fillMaxSize(), text = "ciao bello")
            }
        }

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
    }
}

@Composable
internal fun HCallScreen(
    windowSizeClass: WindowSizeClass,
    callActions: ImmutableList<CallActionUI>,
    inputMessage: InputMessage?,
    sheetState: CallSheetState,
    showAnswerAction: Boolean,
    onChangeSheetState: () -> Unit,
    onParticipantClick: () -> Unit,
    onAnswerActionClick: () -> Unit,
    onHangUpClick: () -> Unit,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    onBackPressed: () -> Unit,
) {
    var currentStream by remember { mutableStateOf<StreamUi?>(null) }
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    val hasSheetDragContent by remember { derivedStateOf { currentStream == null && sheetDragActions.value.isNotEmpty() } }

    HCallScreenScaffold(
        sheetState = sheetState,
        paddingValues = callScreenScaffoldPaddingValues(horizontal = 8.dp, vertical = 4.dp),
        topAppBar = {
            CallAppBar(
                title = "title",
                logo = Logo(),
                recording = false,
                participantCount = 3,
                onParticipantClick = onParticipantClick,
                onBackPressed = onBackPressed,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        },
        sheetDragContent = {
            if (hasSheetDragContent) {
                val itemsPerColumn = callActions.count() - sheetDragActions.count() + 1
                VSheetDragContent(
                    callActions = sheetDragActions,
                    itemsPerColumn = itemsPerColumn,
                    onHangUpClick = onHangUpClick,
                    onMicToggled = onMicToggled,
                    onCameraToggled = onCameraToggled,
                    onScreenShareToggle = onScreenShareToggle,
                    onFlipCameraClick = onFlipCameraClick,
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
                targetState = currentStream == null,
                contentAlignment = Alignment.Center,
                label = "sheet content"
            ) {
                if (it) {
                    Box(Modifier.animateContentSize()) {
                        VSheetContent(
                            callActions = callActions,
                            maxActions = CompactScreenMaxActions,
                            showAnswerAction = showAnswerAction,
                            onActionsPlaced = { itemsPlaced ->
                                sheetDragActions =
                                    ImmutableList(callActions.value.takeLast(callActions.count() - itemsPlaced))
                            },
                            onAnswerActionClick = onAnswerActionClick,
                            onHangUpClick = onHangUpClick,
                            onMicToggled = onMicToggled,
                            onCameraToggled = onCameraToggled,
                            onScreenShareToggle = onScreenShareToggle,
                            onFlipCameraClick = onFlipCameraClick,
                            onAudioClick = onAudioClick,
                            onChatClick = onChatClick,
                            onFileShareClick = onFileShareClick,
                            onWhiteboardClick = onWhiteboardClick,
                            onVirtualBackgroundClick = onVirtualBackgroundClick,
                            onMoreActionClick = onChangeSheetState,
                            modifier = Modifier.padding(
                                start = 5.dp,
                                top = 14.dp,
                                end = 14.dp,
                                bottom = 14.dp
                            )
                        )
                    }
                } else {
//                    VStreamMenuContent(
//                        fullscreen = currentStream == streamContentState.fullscreenStream,
//                        pin = streamContentState.pinnedStreams.containsKey(currentStream?.id),
//                        onCancelClick = { currentStream = null },
//                        onFullscreenClick = { isFullscreen ->
//                            if (isFullscreen) streamContentState.fullscreen(null)
//                            else currentStream?.apply { streamContentState.fullscreen(this) }
//                            currentStream = null
//                        },
//                        onPinClick = { isPinned ->
//                            if (isPinned) streamContentState.unpin(currentStream!!)
//                            else currentStream?.apply { streamContentState.pin(this) }
//                            currentStream = null
//                        }
//                    )
                }
            }

        },
        containerColor = if (!isSystemInDarkTheme()) Color(0xFFF9F9FF) else Color(0xFF000000),
        sheetDragHandle = (@Composable { CallBottomSheetDefaults.VDragHandle() }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val top = paddingValues.calculateTopPadding()
        val bottom = paddingValues.calculateBottomPadding()
        val left = paddingValues.calculateLeftPadding(layoutDirection)

//        StreamContent(
//            state = streamContentState,
//            onStreamClick = {
//                currentStream = if (currentStream != null && currentStream != it) null else it
//            },
//            onStopScreenShareClick = {},
//            modifier = Modifier
//                .fillMaxSize()
//                .navigationBarsPadding()
//                .padding(
//                    start = left,
//                    top = top,
//                    bottom = bottom,
//                    end = 116.dp
//                )
//        )

//        InputMessageHost(
//            inputMessage = inputMessage,
//            modifier = Modifier
//                .padding(vertical = 16.dp)
//                .align(Alignment.BottomCenter)
//        )
    }
}

@Composable
private fun callScreenScaffoldPaddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    return WindowInsets.systemBars
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets(left = horizontal, top = vertical, right = horizontal, bottom = vertical))
        .asPaddingValues()
}