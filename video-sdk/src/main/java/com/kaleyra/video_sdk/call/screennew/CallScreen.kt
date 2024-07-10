package com.kaleyra.video_sdk.call.screennew

import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.launch

internal const val PipScreenTestTag = "PipScreenTestTag"
internal const val VCallScreenTestTag = "VCallScreenTestTag"
internal const val HCallScreenTestTag = "HCallScreenTestTag"

internal const val CompactScreenMaxActions = 5
internal const val LargeScreenMaxActions = 8

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    shouldShowFileShareComponent: Boolean,
    isInPipMode: Boolean,
    enterPip: () -> Unit,
    onPipAspectRatio: (Rational) -> Unit,
    onDisplayMode: (CallUI.DisplayMode) -> Unit,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    onUsbCameraConnected: (Boolean) -> Unit,
    onActivityFinishing: () -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onConnectionServicePermissionsResult: () -> Unit,
) {
//    val theme by viewModel.theme.collectAsStateWithLifecycle()
//    val activity = LocalContext.current.findActivity() as FragmentActivity
//    val callUiState by viewModel.uiState.collectAsStateWithLifecycle()
//    val whiteboardRequest by viewModel.whiteboardRequest.collectAsStateWithLifecycle(initialValue = null)

//    // Needed this to handle properly a sequence of multiple permission
//    // Cannot call micPermissionState.launchPermission followed by cameraPermissionState.launchPermission, or vice versa
//    val inputPermissionsState = rememberMultiplePermissionsState(permissions = listOf(
//        RecordAudioPermission, CameraPermission
//    )) { permissionsResult ->
//        onAskInputPermissions(false)
//        permissionsResult.forEach { (permission, isGranted) ->
//            when {
//                permission == RecordAudioPermission && isGranted -> viewModel.startMicrophone(activity)
//                permission == CameraPermission && isGranted -> viewModel.startCamera(activity)
//            }
//        }
//    }
//    val micPermissionState = rememberPermissionState(permission = RecordAudioPermission) { isGranted ->
//        onAskInputPermissions(false)
//        if (isGranted) viewModel.startMicrophone(activity)
//    }
//    val cameraPermissionState = rememberPermissionState(permission = CameraPermission) { isGranted ->
//        onAskInputPermissions(false)
//        if (isGranted) viewModel.startCamera(activity)
//    }
//    val finishActivity = remember(activity) {
//        {
//            onActivityFinishing()
//            activity.finishAndRemoveTask()
//        }
//    }
//    // code executed when pressing the back button in the call ui
//    // the system back gesture or system back button are handled using the BackHandler composable
//    val onBackPressed by remember(finishActivity, enterPip) {
//        derivedStateOf {
//            {
//                when {
//                    callUiState.callState is CallStateUi.Disconnected.Ended -> finishActivity()
//                    callUiState.fullscreenStream != null -> {
//                        viewModel.fullscreenStream(null)
//                    }
//
//                    else -> enterPip()
//                }
//            }
//        }
//    }
//    LaunchedEffect(isInPipMode, onActivityFinishing) {
//        viewModel.setOnCallEnded { hasFeedback, hasErrorOccurred, hasBeenKicked ->
//            onActivityFinishing()
//            when {
//                isInPipMode || !activity.isAtLeastResumed() -> activity.finishAndRemoveTask()
//                !hasFeedback && !hasBeenKicked -> {
//                    val delayMs = if (hasErrorOccurred) ActivityFinishErrorDelay else ActivityFinishDelay
//                    delay(delayMs)
//                    activity.finishAndRemoveTask()
//                }
//            }
//        }
//    }
//    LaunchedEffect(onPipAspectRatio) {
//        viewModel.setOnPipAspectRatio(onPipAspectRatio)
//    }
//    LaunchedEffect(onDisplayMode) {
//        viewModel.setOnDisplayMode(onDisplayMode)
//    }
//    val shouldAskConnectionServicePermissions = viewModel.shouldAskConnectionServicePermissions && !activity.hasConnectionServicePermissions()
//    var shouldAskInputPermissions by remember { mutableStateOf(!shouldAskConnectionServicePermissions) }
//    val contactsPermissionsState = rememberMultiplePermissionsState(permissions = ContactsPermissions) { _ ->
//        viewModel.startConnectionService(activity)
//        shouldAskInputPermissions = true
//    }
//    val connectionServicePermissionsState = rememberMultiplePermissionsState(permissions = if (ConnectionServiceUtils.isConnectionServiceSupported) ConnectionServicePermissions else listOf()) { permissionsResult ->
//        if (permissionsResult.isNotEmpty() && permissionsResult.all { (_, isGranted) -> isGranted }) {
//            contactsPermissionsState.launchMultiplePermissionRequest()
//        } else {
//            viewModel.tryStartCallService()
//            shouldAskInputPermissions = true
//        }
//        onConnectionServicePermissionsResult()
//    }
//    if (shouldAskConnectionServicePermissions) {
//        LaunchedEffect(connectionServicePermissionsState) {
//            connectionServicePermissionsState.launchMultiplePermissionRequest()
//        }
//    }
//    if (shouldAskInputPermissions) {
//        LaunchedEffect(inputPermissionsState, micPermissionState, cameraPermissionState) {
//            viewModel.setOnAudioOrVideoChanged { isAudioEnabled, isVideoEnabled ->
//                onAskInputPermissions(true)
//                when {
//                    isAudioEnabled && isVideoEnabled -> inputPermissionsState.launchMultiplePermissionRequest()
//                    isAudioEnabled -> micPermissionState.launchPermissionRequest()
//                    isVideoEnabled -> cameraPermissionState.launchPermissionRequest()
//                }
//            }
//        }
//    }
//    LaunchedEffect(onUsbCameraConnected) {
//        viewModel.setOnUsbCameraConnected(onUsbCameraConnected)
//    }
//    CollaborationTheme(theme = theme, transparentSystemBars = true) { isDarkTheme ->
//        CallScreen(
//            windowSizeClass = windowSizeClass,
//            whiteboardRequest = whiteboardRequest,
//            onThumbnailStreamClick = viewModel::swapThumbnail,
//            onThumbnailStreamDoubleClick = viewModel::fullscreenStream,
//            onFullscreenStreamClick = viewModel::fullscreenStream,
//            onUserFeedback = viewModel::sendUserFeedback,
//            onConfigurationChange = viewModel::updateStreamsArrangement,
//            onBackPressed = onBackPressed,
//            onCallEndedBack = finishActivity,
//            isInPipMode = isInPipMode,
//            isDarkTheme = isDarkTheme,
//            onFileShareVisibility = onFileShareVisibility,
//            onWhiteboardVisibility = onWhiteboardVisibility,
//        )
//    }

    val callSheetState = rememberCallSheetState()

    KaleyraM3Theme {
        CallScreen(
            windowSizeClass = windowSizeClass,
            isInPipMode = isInPipMode,
            callSheetState = callSheetState,
            shouldShowFileShareComponent = shouldShowFileShareComponent,
            onFileShareVisibility = onFileShareVisibility,
            onWhiteboardVisibility = onWhiteboardVisibility,
            onBackPressed = { },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    callSheetState: CallSheetState,
    shouldShowFileShareComponent: Boolean,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    isInPipMode: Boolean = false
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val scope = rememberCoroutineScope()

    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val onChangeCallSheetState: (Boolean) -> Unit = remember(callSheetState) {
        { isSheetCollapsed: Boolean ->
            scope.launch {
                if (isSheetCollapsed) callSheetState.expand()
                else callSheetState.collapse()
            }
        }
    }

    var modalSheetComponent: ModalSheetComponent? by remember { mutableStateOf(null) }
    val onModalSheetComponentRequest = remember {
        { component: ModalSheetComponent? -> modalSheetComponent = component }
    }

    var selectedStreamId by remember { mutableStateOf<String?>(null) }
    val onStreamSelected = remember {
        { streamId: String? -> selectedStreamId = streamId }
    }

    FileShareVisibilityObserver(modalSheetComponent, onFileShareVisibility)

    WhiteboardVisibilityObserver(modalSheetComponent, onWhiteboardVisibility)

    if (shouldShowFileShareComponent) {
        LaunchedEffect(Unit) {
            modalSheetComponent = ModalSheetComponent.FileShare
        }
    }

//    LaunchedEffect(whiteboardRequest) {
//        when (whiteboardRequest) {
//            is WhiteboardRequest.Show -> {
//                if (modalSheetComponent == ModalSheetComponent.Whiteboard) return@LaunchedEffect
//                onModalSheetComponentRequest(ModalSheetComponent.Whiteboard)
//                snapshotFlow { modalSheetComponent }.first { it == ModalSheetComponent.Whiteboard }
//                CallUserMessagesProvider.sendUserMessage(WhiteboardShowRequestMessage(whiteboardRequest.username))
//            }
//            is WhiteboardRequest.Hide -> {
//                if (modalSheetComponent != ModalSheetComponent.Whiteboard) return@LaunchedEffect
//                onModalSheetComponentRequest(null)
//                snapshotFlow { modalSheetComponent }.first { it == null }
//                CallUserMessagesProvider.sendUserMessage(WhiteboardHideRequestMessage(whiteboardRequest.username))
//            }
//            else -> Unit
//        }
//    }

//    BackHandler(
//        sheetState = sheetState,
//        isCallEnded = false,
//        onCallEndedBack = onCallEndedBack
//    )

    if (isInPipMode) {
        Text("to implement", modifier = Modifier.testTag(PipScreenTestTag))
    } else {
        if (isCompactHeight) {
            HCallScreen(
                windowSizeClass = windowSizeClass,
                sheetState = callSheetState,
                modalSheetState = modalSheetState,
                modalSheetComponent = modalSheetComponent,
                onChangeSheetState = onChangeCallSheetState,
                selectedStreamId = selectedStreamId,
                onStreamSelected = onStreamSelected,
                onModalSheetComponentRequest = onModalSheetComponentRequest,
                onBackPressed = onBackPressed,
                modifier = modifier.testTag(HCallScreenTestTag)
            )
        } else {
            VCallScreen(
                windowSizeClass = windowSizeClass,
                sheetState = callSheetState,
                modalSheetState = modalSheetState,
                modalSheetComponent = modalSheetComponent,
                onChangeSheetState = onChangeCallSheetState,
                selectedStreamId = selectedStreamId,
                onStreamSelected = onStreamSelected,
                onModalSheetComponentRequest = onModalSheetComponentRequest,
                onBackPressed = onBackPressed,
                modifier = modifier.testTag(VCallScreenTestTag)
            )
        }

//    val callState = callUiState.callState
//    if (callUiState.showFeedback) {
//        val activity = LocalContext.current.findActivity() as ComponentActivity
//        if (activity.isAtLeastResumed()) {
//            UserFeedbackDialog(onDismiss = onCallEndedBack)
//        }
//    }

//    if (callState is CallStateUi.Disconnected.Ended.Kicked) {
//        val activity = LocalContext.current.findActivity() as ComponentActivity
//        if (activity.isAtLeastResumed()) {
//            KickedMessageDialog(adminName = callState.adminName, onDismiss = onCallEndedBack)
//        }
//    }
    }
}

@Composable
internal fun FileShareVisibilityObserver(
    modalSheetComponent: ModalSheetComponent?,
    onFileShareVisibility: (Boolean) -> Unit,
) {
    val isFileShareComponent = modalSheetComponent == ModalSheetComponent.FileShare
    LaunchedEffect(isFileShareComponent) {
        onFileShareVisibility(isFileShareComponent)
    }
}

@Composable
internal fun WhiteboardVisibilityObserver(
    modalSheetComponent: ModalSheetComponent?,
    onWhiteboardVisibility: (Boolean) -> Unit,
) {
    val isWhiteboardComponent = modalSheetComponent == ModalSheetComponent.Whiteboard
    LaunchedEffect(isWhiteboardComponent) {
        onWhiteboardVisibility(isWhiteboardComponent)
    }
}

@Composable
private fun BackHandler(
    sheetState: CallSheetState,
    isCallEnded: Boolean,
    onCallEndedBack: () -> Unit
) {
    val streamViewModel = viewModel<StreamViewModel>(factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
    val streamUiState by streamViewModel.uiState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val collapseSheet: () -> Unit = remember {
        {
            scope.launch {
                sheetState.collapse()
            }
        }
    }

    when {
        isCallEnded -> BackHandler(onBack = onCallEndedBack)
        sheetState.targetValue == CallSheetValue.Expanded -> BackHandler(onBack = collapseSheet)
        streamUiState.fullscreenStream != null -> BackHandler(onBack = { streamViewModel.fullscreen(null) })
    }
}

@Composable
internal fun callScreenScaffoldPaddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    return WindowInsets.systemBars
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets(left = horizontal, top = vertical, right = horizontal, bottom = vertical))
        .asPaddingValues()
}