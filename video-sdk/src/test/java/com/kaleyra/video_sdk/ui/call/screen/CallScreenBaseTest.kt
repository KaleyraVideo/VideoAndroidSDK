package com.kaleyra.video_sdk.ui.call.screen

import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.video_sdk.call.appbar.model.CallAppBarUiState
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.model.AudioAction
import com.kaleyra.video_sdk.call.bottomsheet.model.CameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ChatAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FileShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.FlipCameraAction
import com.kaleyra.video_sdk.call.bottomsheet.model.HangUpAction
import com.kaleyra.video_sdk.call.bottomsheet.model.MicAction
import com.kaleyra.video_sdk.call.bottomsheet.model.ScreenShareAction
import com.kaleyra.video_sdk.call.bottomsheet.model.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.bottomsheet.model.WhiteboardAction
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.feedback.model.FeedbackUiState
import com.kaleyra.video_sdk.call.feedback.viewmodel.FeedbackViewModel
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.kicked.model.KickedMessageUiState
import com.kaleyra.video_sdk.call.kicked.viewmodel.KickedMessageViewModel
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.screen.CallScreen
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.MainUiState
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardRequest
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.common.usermessages.model.StackedSnackbarUiState
import com.kaleyra.video_sdk.common.usermessages.viewmodel.UserMessagesViewModel
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import kotlin.reflect.KClass

internal abstract class CallScreenBaseTest {
    protected val callActionsUiState = MutableStateFlow(CallActionsUiState())

    protected val streamUiState = MutableStateFlow(StreamUiState())

    protected val callViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    protected val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    protected val audioOutputViewModel = mockk<AudioOutputViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(AudioOutputUiState())
    }

    protected val screenShareViewModel = mockk<ScreenShareViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(ScreenShareUiState())
    }

    protected val fileShareViewModel = mockk<FileShareViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(FileShareUiState())
    }

    protected val whiteboardViewModel = mockk<WhiteboardViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(WhiteboardUiState())
    }

    protected val virtualBackgroundViewModel = mockk<VirtualBackgroundViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(VirtualBackgroundUiState())
    }

    protected val callInfoViewModel = mockk<CallInfoViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(CallInfoUiState())
    }

    protected val callAppBarViewModel = mockk<CallAppBarViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(CallAppBarUiState())
    }

    protected val userMessagesViewModel = mockk<UserMessagesViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(StackedSnackbarUiState())
    }

    protected val feedbackUiState: MutableStateFlow<FeedbackUiState> = MutableStateFlow(
        FeedbackUiState.Hidden)
    protected val feedbackViewModel = mockk<FeedbackViewModel>(relaxed = true) {
        every { uiState } returns feedbackUiState
    }

    protected val kickedUiState: MutableStateFlow<KickedMessageUiState> = MutableStateFlow(
        KickedMessageUiState.Hidden)
    protected val kickedMessageViewModel = mockk<KickedMessageViewModel>(relaxed = true) {
        every { uiState } returns kickedUiState
    }

    protected val allActions = listOf(
        HangUpAction(),
        FlipCameraAction(),
        AudioAction(),
        ChatAction(),
        FileShareAction(),
        WhiteboardAction(),
        VirtualBackgroundAction(),
        MicAction(),
        CameraAction(),
        ScreenShareAction.UserChoice(),
    )

    @Before
    fun setUp() {
        mockkObject(CallActionsViewModel)
        mockkObject(StreamViewModel)
        mockkObject(AudioOutputViewModel)
        mockkObject(ScreenShareViewModel)
        mockkObject(FileShareViewModel)
        mockkObject(WhiteboardViewModel)
        mockkObject(VirtualBackgroundViewModel)
        mockkObject(CallInfoViewModel)
        mockkObject(CallAppBarViewModel)
        mockkObject(ParticipantsViewModel)
        mockkObject(UserMessagesViewModel)
        mockkObject(FeedbackViewModel)
        mockkObject(KickedMessageViewModel)
        mockkObject(UserMessagesViewModel)

        every { CallActionsViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<CallActionsViewModel>>(), any()) } returns callViewModel
        }

        every { StreamViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<StreamViewModel>>(), any()) } returns streamViewModel
        }
        every { AudioOutputViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<AudioOutputViewModel>>(), any()) } returns audioOutputViewModel
        }
        every { ScreenShareViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<ScreenShareViewModel>>(), any()) } returns screenShareViewModel
        }
        every { FileShareViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<FileShareViewModel>>(), any()) } returns fileShareViewModel
        }
        every { WhiteboardViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<WhiteboardViewModel>>(), any()) } returns whiteboardViewModel
        }
        every { VirtualBackgroundViewModel.provideFactory(any()) } returns mockk {
            every {
                create(
                    any<KClass<VirtualBackgroundViewModel>>(),
                    any()
                )
            } returns virtualBackgroundViewModel
        }
        every { CallInfoViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<CallInfoViewModel>>(), any()) } returns callInfoViewModel
        }
        every { CallAppBarViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<CallAppBarViewModel>>(), any()) } returns callAppBarViewModel
        }
        every { ParticipantsViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<ParticipantsViewModel>>(), any()) } returns mockk(relaxed = true) {
                every { uiState } returns MutableStateFlow(ParticipantsUiState())
            }
        }
        every { UserMessagesViewModel.provideFactory(any(), any()) } returns mockk {
            every { create(any<KClass<UserMessagesViewModel>>(), any()) } returns userMessagesViewModel
        }
        every { FeedbackViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<FeedbackViewModel>>(), any()) } returns feedbackViewModel
        }
        every { KickedMessageViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<KickedMessageViewModel>>(), any()) } returns kickedMessageViewModel
        }
        ContextRetainer().create(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    protected fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.setUpCallScreen(
        uiState: MainUiState = MainUiState(),
        callSheetState: CallSheetState = CallSheetState(),
        shouldShowFileShareComponent: State<Boolean> = mutableStateOf(false),
        shouldShowSignDocumentsComponent: State<Boolean> = mutableStateOf(false),
        shouldShowSignDocumentViewComponent: State<Boolean> = mutableStateOf(false),
        whiteboardRequest: State<WhiteboardRequest?> = mutableStateOf(null),
        inputPermissions: InputPermissions = InputPermissions(),
        onCallEndedBack: () -> Unit = {},
        onBackPressed: () -> Unit = {},
        onPipAspectRatio: (Rational) -> Unit = {},
        onAskInputPermissions: (Boolean) -> Unit = {},
        onFileShareVisibility: (Boolean) -> Unit = {},
        onWhiteboardVisibility: (Boolean) -> Unit = {},
        onSignDocumentsVisibility: (Boolean) -> Unit = {},
        onSignDocumentViewVisibility: (Boolean) -> Unit = {},
        isPipMode: Boolean = false,
    ) {
        setContent {
            CallScreen(
                windowSizeClass = calculateWindowSizeClass(activity),
                uiState = uiState,
                isInPipMode = isPipMode,
                callSheetState = callSheetState,
                shouldShowFileShareComponent = shouldShowFileShareComponent.value,
                shouldShowSignDocumentsComponent =  shouldShowSignDocumentsComponent.value,
                shouldShowSignDocumentViewComponent = shouldShowSignDocumentViewComponent.value,
                whiteboardRequest = whiteboardRequest.value,
                inputPermissions = inputPermissions,
                onFileShareVisibility = onFileShareVisibility,
                onWhiteboardVisibility = onWhiteboardVisibility,
                onSignDocumentViewVisibility = onSignDocumentViewVisibility,
                onSignDocumentsVisibility = onSignDocumentsVisibility,
                onBackPressed = onBackPressed,
                onAskInputPermissions = onAskInputPermissions,
                onPipAspectRatio = onPipAspectRatio,
                onCallEndedBack = onCallEndedBack,
                onChatDeleted = {},
                onChatCreationFailed = {}
            )
        }
    }
}