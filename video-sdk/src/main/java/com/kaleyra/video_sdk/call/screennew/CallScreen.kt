package com.kaleyra.video_sdk.call.screennew

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.connectionservice.ConnectionServiceUtils
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasConnectionServicePermissions
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.feedback.UserFeedbackDialog
import com.kaleyra.video_sdk.call.kicked.view.KickedMessageDialog
import com.kaleyra.video_sdk.call.pip.PipScreen
import com.kaleyra.video_sdk.call.screennew.model.MainUiState
import com.kaleyra.video_sdk.call.screennew.viewmodel.MainViewModel
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.utils.CameraPermission
import com.kaleyra.video_sdk.call.utils.ConnectionServicePermissions
import com.kaleyra.video_sdk.call.utils.ContactsPermissions
import com.kaleyra.video_sdk.call.utils.RecordAudioPermission
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardHideRequestMessage
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardShowRequestMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.CollaborationM3Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal const val PipScreenTestTag = "PipScreenTestTag"
internal const val VCallScreenTestTag = "VCallScreenTestTag"
internal const val HCallScreenTestTag = "HCallScreenTestTag"

internal const val CompactScreenMaxActions = 5
internal const val LargeScreenMaxActions = 8

private const val ActivityFinishDelay = 1100L
private const val ActivityFinishErrorDelay = 1500L

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun CallScreen(
    viewModel: MainViewModel = viewModel(
        factory = MainViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    windowSizeClass: WindowSizeClass,
    shouldShowFileShareComponent: Boolean,
    isInPipMode: Boolean,
    enterPip: () -> Unit,
    onDisplayMode: (CallUI.DisplayMode) -> Unit,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    onUsbCameraConnected: (Boolean) -> Unit,
    onActivityFinishing: () -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onConnectionServicePermissionsResult: () -> Unit,
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity() as FragmentActivity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val whiteboardRequest by viewModel.whiteboardRequest.collectAsStateWithLifecycle(initialValue = null)

    val callSheetState = rememberCallSheetState()

    // Needed this to handle properly a sequence of multiple permission
    // Cannot call micPermissionState.launchPermission followed by cameraPermissionState.launchPermission, or vice versa
    val inputPermissionsState = rememberMultiplePermissionsState(permissions = listOf(
        RecordAudioPermission, CameraPermission
    )) { permissionsResult ->
        onAskInputPermissions(false)
        permissionsResult.forEach { (permission, isGranted) ->
            when {
                permission == RecordAudioPermission && isGranted -> viewModel.startMicrophone(activity)
                permission == CameraPermission && isGranted -> viewModel.startCamera(activity)
            }
        }
    }
    val micPermissionState = rememberPermissionState(permission = RecordAudioPermission) { isGranted ->
        onAskInputPermissions(false)
        if (isGranted) viewModel.startMicrophone(activity)
    }
    val cameraPermissionState = rememberPermissionState(permission = CameraPermission) { isGranted ->
        onAskInputPermissions(false)
        if (isGranted) viewModel.startCamera(activity)
    }
    val finishActivity = remember(activity) {
        {
            onActivityFinishing()
            activity.finishAndRemoveTask()
        }
    }
    // code executed when pressing the back button in the call ui
    // the system back gesture or system back button are handled using the BackHandler composable
    val streamViewModel = viewModel<StreamViewModel>(factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration))
    val streamUiState by streamViewModel.uiState.collectAsStateWithLifecycle()
    val onBackPressed by remember(finishActivity, enterPip) {
        derivedStateOf {
            {
                when {
                    uiState.isCallEnded -> finishActivity()
                    streamUiState.fullscreenStream != null -> streamViewModel.fullscreen(null)
                    else -> enterPip()
                }
            }
        }
    }

    val shouldAskConnectionServicePermissions = viewModel.shouldAskConnectionServicePermissions && !activity.hasConnectionServicePermissions()
    var shouldAskInputPermissions by remember { mutableStateOf(!shouldAskConnectionServicePermissions) }
    val contactsPermissionsState = rememberMultiplePermissionsState(permissions = ContactsPermissions) { _ ->
        viewModel.startConnectionService(activity)
        shouldAskInputPermissions = true
    }
    val connectionServicePermissionsState = rememberMultiplePermissionsState(permissions = if (ConnectionServiceUtils.isConnectionServiceSupported) ConnectionServicePermissions else listOf()) { permissionsResult ->
        if (permissionsResult.isNotEmpty() && permissionsResult.all { (_, isGranted) -> isGranted }) {
            contactsPermissionsState.launchMultiplePermissionRequest()
        } else {
            viewModel.tryStartCallService()
            shouldAskInputPermissions = true
        }
        onConnectionServicePermissionsResult()
    }
    if (shouldAskConnectionServicePermissions) {
        LaunchedEffect(connectionServicePermissionsState) {
            connectionServicePermissionsState.launchMultiplePermissionRequest()
        }
    }
    if (shouldAskInputPermissions) {
        LaunchedEffect(inputPermissionsState, micPermissionState, cameraPermissionState) {
            viewModel.setOnAudioOrVideoChanged { isAudioEnabled, isVideoEnabled ->
                onAskInputPermissions(true)
                when {
                    isAudioEnabled && isVideoEnabled -> inputPermissionsState.launchMultiplePermissionRequest()
                    isAudioEnabled -> micPermissionState.launchPermissionRequest()
                    isVideoEnabled -> cameraPermissionState.launchPermissionRequest()
                }
            }
        }
    }

    LaunchedEffect(isInPipMode, onActivityFinishing) {
        viewModel.setOnCallEnded { hasFeedback, hasErrorOccurred, hasBeenKicked ->
            onActivityFinishing()
            when {
                isInPipMode || !activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) -> activity.finishAndRemoveTask()
                !hasFeedback && !hasBeenKicked -> {
                    val delayMs = if (hasErrorOccurred) ActivityFinishErrorDelay else ActivityFinishDelay
                    delay(delayMs)
                    activity.finishAndRemoveTask()
                }
            }
        }
    }

    LaunchedEffect(onDisplayMode) {
        viewModel.setOnDisplayMode(onDisplayMode)
    }

    LaunchedEffect(onUsbCameraConnected) {
        viewModel.setOnUsbCameraConnected(onUsbCameraConnected)
    }

    CollaborationM3Theme(theme = theme) {
        CallScreen(
            windowSizeClass = windowSizeClass,
            uiState = uiState,
            isInPipMode = isInPipMode,
            callSheetState = callSheetState,
            shouldShowFileShareComponent = shouldShowFileShareComponent,
            whiteboardRequest = whiteboardRequest,
            onFileShareVisibility = onFileShareVisibility,
            onWhiteboardVisibility = onWhiteboardVisibility,
            onAskInputPermissions = onAskInputPermissions,
            onCallEndedBack = finishActivity,
            onBackPressed = onBackPressed,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    uiState: MainUiState,
    callSheetState: CallSheetState,
    shouldShowFileShareComponent: Boolean,
    whiteboardRequest: WhiteboardRequest?,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onCallEndedBack: () -> Unit,
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

    LaunchedEffect(whiteboardRequest) {
        when (whiteboardRequest) {
            is WhiteboardRequest.Show -> {
                if (modalSheetComponent == ModalSheetComponent.Whiteboard) return@LaunchedEffect
                onModalSheetComponentRequest(ModalSheetComponent.Whiteboard)
                modalSheetState.expand()
                CallUserMessagesProvider.sendUserMessage(WhiteboardShowRequestMessage(whiteboardRequest.username))
            }

            is WhiteboardRequest.Hide -> {
                if (modalSheetComponent != ModalSheetComponent.Whiteboard) return@LaunchedEffect
                onModalSheetComponentRequest(null)
                modalSheetState.hide()
                CallUserMessagesProvider.sendUserMessage(WhiteboardHideRequestMessage(whiteboardRequest.username))
            }

            else -> Unit
        }
    }

    BackHandler(
        sheetState = callSheetState,
        isCallEnded = uiState.isCallEnded,
        isAnyStreamSelected = selectedStreamId != null,
        onCallEndedBack = onCallEndedBack,
        onDismissSelectedStream = { selectedStreamId = null }
    )

    if (isInPipMode) {
        PipScreen(modifier = Modifier.testTag(PipScreenTestTag))
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
                onAskInputPermissions = onAskInputPermissions,
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
                onAskInputPermissions = onAskInputPermissions,
                onBackPressed = onBackPressed,
                modifier = modifier.testTag(VCallScreenTestTag)
            )
        }

        UserFeedbackDialog(onDismiss = onCallEndedBack)

        KickedMessageDialog(onDismiss = onCallEndedBack)
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
    isAnyStreamSelected: Boolean,
    onCallEndedBack: () -> Unit,
    onDismissSelectedStream: () -> Unit
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
        isAnyStreamSelected -> BackHandler(onBack = onDismissSelectedStream)
        streamUiState.fullscreenStream != null -> BackHandler(onBack = { streamViewModel.fullscreen(null) })
    }
}

@Composable
internal fun callScreenScaffoldPaddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    return WindowInsets.navigationBars
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets(left = horizontal, top = vertical, right = horizontal, bottom = vertical))
        .asPaddingValues()
}