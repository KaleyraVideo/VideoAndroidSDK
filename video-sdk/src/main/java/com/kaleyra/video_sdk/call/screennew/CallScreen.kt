package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.view.CallAppBar
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.VSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.VSheetDragContent
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    inputMessage: InputMessage?,
    onBackPressed: () -> Unit,
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val sheetState = rememberCallSheetState()

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
                            isMoreToggled = false,
                            onActionsPlaced = { itemsPlaced ->
                                sheetDragActions =
                                    ImmutableList(callActions.value.takeLast(callActions.count() - itemsPlaced))
                            },
                            onAnswerClick = onAnswerActionClick,
                            onHangUpClick = onHangUpClick,
                            onMicToggle = onMicToggled,
                            onCameraToggle = onCameraToggled,
                            onScreenShareToggle = onScreenShareToggle,
                            onFlipCameraClick = onFlipCameraClick,
                            onAudioClick = onAudioClick,
                            onChatClick = onChatClick,
                            onFileShareClick = onFileShareClick,
                            onWhiteboardClick = onWhiteboardClick,
                            onVirtualBackgroundClick = onVirtualBackgroundClick,
                            onMoreToggle = {
                                onChangeSheetState()
                            },
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
internal fun callScreenScaffoldPaddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    return WindowInsets.systemBars
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets(left = horizontal, top = vertical, right = horizontal, bottom = vertical))
        .asPaddingValues()
}