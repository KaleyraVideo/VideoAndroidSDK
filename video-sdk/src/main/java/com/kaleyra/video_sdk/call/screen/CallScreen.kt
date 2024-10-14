package com.kaleyra.video_sdk.call.screen

import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheet.rememberCallSheetState
import com.kaleyra.video_sdk.call.feedback.UserFeedbackDialog
import com.kaleyra.video_sdk.call.kicked.view.KickedMessageDialog
import com.kaleyra.video_sdk.call.pip.PipScreen
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.MainUiState
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.hcallscreen.HCallScreen
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.VCallScreen
import com.kaleyra.video_sdk.call.screen.viewmodel.MainViewModel
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.utils.CameraPermission
import com.kaleyra.video_sdk.call.utils.ConnectionServicePermissions
import com.kaleyra.video_sdk.call.utils.ContactsPermissions
import com.kaleyra.video_sdk.call.utils.RecordAudioPermission
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.common.usermessages.model.WhiteboardRequestMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.CollaborationTheme
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isAtLeastMediumWidth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
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
    onPipAspectRatio: (Rational) -> Unit,
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

    val wasMicPermissionAsked = remember { mutableStateOf(false) }
    val wasCameraPermissionAsked = remember { mutableStateOf(false) }
    val shouldAskCameraPermission = remember { mutableStateOf(false) }

    // Needed this to handle properly a sequence of multiple permission
    // Cannot call micPermissionState.launchPermission followed by cameraPermissionState.launchPermission, or vice versa
    val inputPermissionsState = rememberMultiplePermissionsState(permissions = listOf(
        RecordAudioPermission, CameraPermission
    )) { permissionsResult ->
        onAskInputPermissions(false)
        permissionsResult.forEach { (permission, isGranted) ->
            when  {
                permission == RecordAudioPermission && isGranted -> viewModel.startMicrophone(activity)
                permission == CameraPermission && isGranted -> viewModel.startCamera(activity)
            }
        }
        wasMicPermissionAsked.value = true
        wasCameraPermissionAsked.value = true
    }
    val micPermissionState = rememberPermissionState(permission = RecordAudioPermission) { isGranted ->
        onAskInputPermissions(false)
        wasMicPermissionAsked.value = true
        if (isGranted) viewModel.startMicrophone(activity)
    }
    val cameraPermissionState = rememberPermissionState(permission = CameraPermission) { isGranted ->
        onAskInputPermissions(false)
        wasCameraPermissionAsked.value = true
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
                    streamUiState.fullscreenStream != null -> {
                        streamViewModel.fullscreen(null)
                        streamViewModel.unpinAll()
                    }
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
                shouldAskCameraPermission.value = isAudioEnabled && isVideoEnabled
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

    val inputPermissions by remember(wasMicPermissionAsked, wasCameraPermissionAsked, shouldAskCameraPermission) {
        derivedStateOf {
            InputPermissions(
                shouldAskCameraPermission = shouldAskCameraPermission.value,
                wasMicPermissionAsked = wasMicPermissionAsked.value,
                wasCameraPermissionAsked = wasCameraPermissionAsked.value,
                micPermission = micPermissionState,
                cameraPermission = cameraPermissionState
            )
        }
    }

    CollaborationTheme(theme = theme) {
        CallScreen(
            windowSizeClass = windowSizeClass,
            uiState = uiState,
            isInPipMode = isInPipMode,
            callSheetState = callSheetState,
            shouldShowFileShareComponent = shouldShowFileShareComponent,
            whiteboardRequest = whiteboardRequest,
            inputPermissions = inputPermissions,
            onFileShareVisibility = onFileShareVisibility,
            onWhiteboardVisibility = onWhiteboardVisibility,
            onAskInputPermissions = onAskInputPermissions,
            onPipAspectRatio = onPipAspectRatio,
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
    inputPermissions: InputPermissions,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    onAskInputPermissions: (Boolean) -> Unit,
    onPipAspectRatio: (Rational) -> Unit,
    onCallEndedBack: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    isInPipMode: Boolean = false
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val isLargeScreen = windowSizeClass.isAtLeastMediumWidth()

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

    val modalSheetComponent: MutableState<ModularComponent?> = remember { mutableStateOf(null) }
    val onModalSheetComponentRequest = remember(modalSheetComponent) {
        { component: ModularComponent? -> modalSheetComponent.value  = component }
    }

    val sidePanelComponent: MutableState<ModularComponent?> = remember(isLargeScreen) { mutableStateOf(null) }
    val onSidePanelComponentRequest = remember(sidePanelComponent) {
        { component: ModularComponent? -> sidePanelComponent.value = component.takeIf { it != sidePanelComponent.value } }
    }

    val lastModularComponentDisplayed: MutableState<ModularComponent?> = remember { mutableStateOf(null) }
    val onModularComponentDisplayed = remember(lastModularComponentDisplayed) {
        { component: ModularComponent? -> lastModularComponentDisplayed.value = component }
    }

    var selectedStreamId by remember { mutableStateOf<String?>(null) }
    val onStreamSelected = remember {
        { streamId: String? -> selectedStreamId = streamId }
    }

    FileShareVisibilityObserver(
        modalSheetComponent = modalSheetComponent.value,
        sidePanelComponent = sidePanelComponent.value,
        onFileShareVisibility
    )

    WhiteboardVisibilityObserver(
        modalSheetComponent = modalSheetComponent.value,
        sidePanelComponent = sidePanelComponent.value,
        onWhiteboardVisibility
    )

    if (shouldShowFileShareComponent) {
        LaunchedEffect(Unit) {
            if (!isLargeScreen) onModalSheetComponentRequest(ModularComponent.FileShare)
            else onSidePanelComponentRequest(ModularComponent.FileShare)
        }
    }

    LaunchedEffect(whiteboardRequest) {
        when (whiteboardRequest) {
            is WhiteboardRequest.Show -> {
                val targetComponent = if (isLargeScreen) sidePanelComponent else modalSheetComponent
                if (targetComponent.value == ModularComponent.Whiteboard)  return@LaunchedEffect
                if (isLargeScreen) onSidePanelComponentRequest(ModularComponent.Whiteboard) else onModalSheetComponentRequest(ModularComponent.Whiteboard)
                snapshotFlow { lastModularComponentDisplayed.value }.firstOrNull { it == ModularComponent.Whiteboard }
                CallUserMessagesProvider.sendUserMessage(
                    WhiteboardRequestMessage.WhiteboardShowRequestMessage(whiteboardRequest.username)
                )
            }

            is WhiteboardRequest.Hide -> {
                val targetComponent = if (isLargeScreen) sidePanelComponent else modalSheetComponent
                if (targetComponent.value != ModularComponent.Whiteboard) return@LaunchedEffect
                if (isLargeScreen) onSidePanelComponentRequest(null) else onModalSheetComponentRequest(null)
                snapshotFlow { lastModularComponentDisplayed.value }.firstOrNull { it != ModularComponent.Whiteboard }
                CallUserMessagesProvider.sendUserMessage(
                    WhiteboardRequestMessage.WhiteboardHideRequestMessage(whiteboardRequest.username)
                )
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
        PipScreen(
            onPipAspectRatio = onPipAspectRatio,
            modifier = Modifier.testTag(PipScreenTestTag)
        )
    } else {
        if (isCompactHeight) {
            HCallScreen(
                windowSizeClass = windowSizeClass,
                sheetState = callSheetState,
                modalSheetState = modalSheetState,
                modalSheetComponent = modalSheetComponent.value,
                inputPermissions = inputPermissions,
                onChangeSheetState = onChangeCallSheetState,
                selectedStreamId = selectedStreamId,
                onStreamSelected = onStreamSelected,
                onModalSheetComponentRequest = onModalSheetComponentRequest,
                onModularComponentDisplayed = onModularComponentDisplayed,
                onAskInputPermissions = onAskInputPermissions,
                onBackPressed = onBackPressed,
                modifier = modifier.testTag(HCallScreenTestTag)
            )
        } else {
            VCallScreen(
                windowSizeClass = windowSizeClass,
                sheetState = callSheetState,
                modalSheetState = modalSheetState,
                modalSheetComponent = modalSheetComponent.value,
                sidePanelComponent = sidePanelComponent.value,
                inputPermissions = inputPermissions,
                onChangeSheetState = onChangeCallSheetState,
                selectedStreamId = selectedStreamId,
                onStreamSelected = onStreamSelected,
                onModalSheetComponentRequest = onModalSheetComponentRequest,
                onSidePanelComponentRequest = onSidePanelComponentRequest,
                onModularComponentDisplayed = onModularComponentDisplayed,
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
    modalSheetComponent: ModularComponent?,
    sidePanelComponent: ModularComponent?,
    onFileShareVisibility: (Boolean) -> Unit,
) {
    val isFileShareComponent = modalSheetComponent == ModularComponent.FileShare || sidePanelComponent == ModularComponent.FileShare
    LaunchedEffect(isFileShareComponent) {
        onFileShareVisibility(isFileShareComponent)
    }
}

@Composable
internal fun WhiteboardVisibilityObserver(
    modalSheetComponent: ModularComponent?,
    sidePanelComponent: ModularComponent?,
    onWhiteboardVisibility: (Boolean) -> Unit,
) {
    val isWhiteboardComponent = modalSheetComponent == ModularComponent.Whiteboard || sidePanelComponent == ModularComponent.Whiteboard
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
        streamUiState.fullscreenStream != null -> BackHandler(onBack = {
            streamViewModel.fullscreen(null)
            streamViewModel.unpinAll()
        })
    }
}

@Composable
internal fun callScreenScaffoldPaddingValues(left: Dp = 0.dp, top: Dp = 0.dp, right: Dp = 0.dp, bottom: Dp = 0.dp): PaddingValues {
    return WindowInsets.navigationBars
        .add(WindowInsets.statusBars)
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets(left, top, right, bottom))
        .asPaddingValues()
}