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
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
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
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.call.stream.model.AudioUi
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.VideoUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
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
    actions: ImmutableList<CallActionUI>,
    inputMessage: InputMessage?,
    onMicToggled: (Boolean) -> Unit,
    onCameraToggled: (Boolean) -> Unit,
    onScreenShareToggle: (Boolean) -> Unit,
    onHangUpClick: () -> Unit,
    onFlipCameraClick: () -> Unit,
    onAudioClick: () -> Unit,
    onChatClick: () -> Unit,
    onFileShareClick: () -> Unit,
    onWhiteboardClick: () -> Unit,
    onVirtualBackgroundClick: () -> Unit,
    onParticipantClick: () -> Unit,
    onBackPressed: () -> Unit,
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val sheetState = rememberCallSheetState()

    var showAnswerAction by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val onChangeSheetState: () -> Unit = remember {
        {
            scope.launch {
                if (sheetState.currentValue == CallSheetValue.Expanded) sheetState.collapse()
                else sheetState.expand()
            }
        }
    }

    var streams = remember {
        mutableStateOf(
            ImmutableList(
                listOf(
                    StreamUi(id = "1", username = "username1"),
                    StreamUi(id = "2", username = "username2"),
                    StreamUi(id = "3", username = "username3"),
                    StreamUi(id = "4", username = "username4"),
                    StreamUi(id = "5", username = "username5"),
                    StreamUi(id = "6", username = "username6"),
                    StreamUi(id = "7", username = "username7"),
                    StreamUi(id = "8", username = "username8", audio = AudioUi(id = "id", isEnabled = true, isMutedForYou = true)),
                    StreamUi(
                        id = "10",
                        username = "username10",
                        mine = true
                    ),
                    StreamUi(
                        id = "11",
                        username = "username11",
                        mine = false,
                        video = VideoUi(id = "4", isScreenShare = true)
                    ),
                    StreamUi(
                        id = "12",
                        username = "username12",
                        mine = false,
                        video = VideoUi(id = "5", isScreenShare = true)
                    ),
                    StreamUi(
                        id = "13",
                        username = "username13",
                        mine = false,
                        video = VideoUi(id = "6", isScreenShare = true)
                    ),
                    StreamUi(
                        id = "14",
                        username = "username14",
                        mine = false,
                        video = VideoUi(id = "7", isScreenShare = true)
                    ),
//                    StreamUi(id = "10", username = "username10"),
//                    StreamUi(id = "11", username = "username11"),
//                    StreamUi(id = "12", username = "username12"),
//                    StreamUi(id = "13", username = "username13"),
//                    StreamUi(id = "14", username = "username14"),
//                    StreamUi(id = "15", username = "username15"),
//                    StreamUi(id = "16", username = "username16"),
//                    StreamUi(id = "17", username = "username17"),
//                    StreamUi(id = "18", username = "username18", mine = true, video = VideoUi(id = "1", isScreenShare = true)),
                )
            )
        )
    }



//    LaunchedEffect(Unit) {
//        delay(5000)
//        streams.value = ImmutableList(
//            listOf(
//                StreamUi(id = "1", username = "username1"),
//                StreamUi(id = "2", username = "username2"),
//                StreamUi(id = "3", username = "username3"),
//                StreamUi(id = "4", username = "username4"),
//                StreamUi(id = "5", username = "username5"),
//                StreamUi(id = "6", username = "username6"),
//                StreamUi(id = "7", username = "username7"),
//                StreamUi(id = "8", username = "username8"),
//                StreamUi(
//                    id = "9",
//                    username = "username9",
//                    mine = true,
//                    video = VideoUi(id = "2", isScreenShare = true)
//                ),
//                StreamUi(
//                    id = "10",
//                    username = "username10",
//                    mine = false,
//                    video = VideoUi(id = "3", isScreenShare = true)
//                ),
//                StreamUi(
//                    id = "11",
//                    username = "username11",
//                    mine = false,
//                    video = VideoUi(id = "4", isScreenShare = true)
//                ),
//                StreamUi(
//                    id = "12",
//                    username = "username12",
//                    mine = false,
//                    video = VideoUi(id = "5", isScreenShare = true)
//                ),
//                StreamUi(
//                    id = "13",
//                    username = "username13",
//                    mine = false,
//                    video = VideoUi(id = "6", isScreenShare = true)
//                ),
//                StreamUi(
//                    id = "14",
//                    username = "username14",
//                    mine = false,
//                    video = VideoUi(id = "7", isScreenShare = true)
//                ),
//                StreamUi(id = "18", username = "username18", mine = true, video = VideoUi(id = "1", isScreenShare = true)),
//            )
//        )
//    }

    val windowSizeClass2 = rememberUpdatedState(newValue = windowSizeClass)
    val streamContentController = rememberStreamContentController(streams = streams, windowSizeClass = windowSizeClass2)

    if (isCompactHeight) {
//        HCallScreen(
//            windowSizeClass = windowSizeClass,
//            callActions = actions,
//            streamContentState = streamContentState,
//            inputMessage = inputMessage,
//            sheetState = sheetState,
//            showAnswerAction = showAnswerAction,
//            onChangeSheetState = onChangeSheetState,
//            onParticipantClick = onParticipantClick,
//            onAnswerActionClick = { showAnswerAction = false },
//            onHangUpClick = onHangUpClick,
//            onMicToggled = onMicToggled,
//            onCameraToggled = onCameraToggled,
//            onScreenShareToggle = onScreenShareToggle,
//            onFlipCameraClick = onFlipCameraClick,
//            onAudioClick = onAudioClick,
//            onChatClick = onChatClick,
//            onFileShareClick = onFileShareClick,
//            onWhiteboardClick = onWhiteboardClick,
//            onVirtualBackgroundClick = onVirtualBackgroundClick,
//            onBackPressed = onBackPressed
//        )
    } else {
        VCallScreen(
            windowSizeClass = windowSizeClass,
            callActions = actions,
            inputMessage = inputMessage,
            streamContentController = streamContentController,
            sheetState = sheetState,
            showAnswerAction = showAnswerAction,
            onChangeSheetState = onChangeSheetState,
            onParticipantClick = onParticipantClick,
            onAnswerActionClick = { showAnswerAction = false },
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
            onBackPressed = onBackPressed,
            onFullscreenClick = { stream, isFullscreen ->
                streamContentController.fullscreen(if (isFullscreen) null else stream)
            },
            onPinClick = { stream, isPinned ->
                if (isPinned) streamContentController.unpinStream(stream)
                else streamContentController.pinStream(stream)
            }
        )
    }
}

internal const val CompactScreenMaxActions = 5
internal const val LargeScreenMaxActions = 8

@Composable
internal fun VCallScreen(
    windowSizeClass: WindowSizeClass,
    sheetState: CallSheetState,
    callActions: ImmutableList<CallActionUI>,
    streamContentController: StreamContentController,
    inputMessage: InputMessage?,
    showAnswerAction: Boolean,
    onChangeSheetState: () -> Unit,
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
    onParticipantClick: () -> Unit,
    onBackPressed: () -> Unit,
    onFullscreenClick: (StreamUi, Boolean) -> Unit,
    onPinClick: (StreamUi, Boolean) -> Unit,
) {
    val isLargeScreen = windowSizeClass.widthSizeClass in setOf(
        WindowWidthSizeClass.Medium,
        WindowWidthSizeClass.Expanded
    )

    var currentStream by remember { mutableStateOf<StreamUi?>(null) }
    var sheetDragActions: ImmutableList<CallActionUI> by remember { mutableStateOf(ImmutableList()) }
    // TODO do a resize test when writing test
    val hasSheetDragContent by remember(isLargeScreen) { derivedStateOf { !isLargeScreen && currentStream == null && sheetDragActions.value.isNotEmpty() } }
    // TODO test reset on resize
    var showSheetPanelContent by remember(isLargeScreen) { mutableStateOf(false) }

    VCallScreenScaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    showSheetPanelContent = false
                    currentStream = null
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
                onParticipantClick = onParticipantClick,
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
                    Card(Modifier.width(320.dp)) {
                        SheetPanelContent(
                            items = sheetDragActions,
                            onItemClick = { callAction ->
                                when (callAction) {
                                    is FlipCameraAction -> onFlipCameraClick()
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
                val itemsPerRow =
                    callActions.count() - sheetDragActions.count() + if (showAnswerAction) AnswerActionMultiplier else 1
                HSheetDragContent(
                    callActions = sheetDragActions,
                    itemsPerRow = itemsPerRow,
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
                targetState = currentStream,
                contentAlignment = Alignment.Center,
                label = "sheet content"
            ) { current ->
                if (current == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isLargeScreen) {
                            Box(
                                modifier = Modifier
                                    .width(250.dp)
                                    .animateContentSize()
                                    .padding(top = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                InputMessageHost(
                                    inputMessage = inputMessage,
                                    micMessage = { enabled -> MicMessageText(enabled) },
                                    cameraMessage = { enabled -> CameraMessageText(enabled) }
                                )
                            }
                        }
                        Box(Modifier.animateContentSize()) {
                            HSheetContent(
                                isLargeScreen = isLargeScreen,
                                callActions = callActions,
                                maxActions = if (isLargeScreen) LargeScreenMaxActions else CompactScreenMaxActions,
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
                    HStreamMenuContent(
                        fullscreen = streamContentController.fullscreenStream?.id == current.id,
                        pin = streamContentController.pinnedStreams.fastAny { stream -> stream.id == current.id },
                        onCancelClick = { currentStream = null },
                        onFullscreenClick = { isFullscreen ->
                            onFullscreenClick(current, isFullscreen)
                            currentStream = null
                        },
                        onPinClick = { isPinned ->
                            onPinClick(current, isPinned)
                            currentStream = null
                        }
                    )
                }
            }
        },
        containerColor = if (!isSystemInDarkTheme()) Color(0xFFF9F9FF) else Color(0xFF000000),
        sheetDragHandle = (@Composable { InputMessageHandle(inputMessage) }).takeIf { hasSheetDragContent }
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val top = paddingValues.calculateTopPadding()
        val left = paddingValues.calculateLeftPadding(layoutDirection)
        val right = paddingValues.calculateRightPadding(layoutDirection)

        StreamContainer(
            streamContentController = streamContentController,
            highlightedStream = currentStream,
            onStreamClick = { stream -> currentStream = stream },
            onStopScreenShareClick = {},
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