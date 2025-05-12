package com.kaleyra.video_sdk.ui.call.screen

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.theme.Theme
import com.kaleyra.video_sdk.R
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
import com.kaleyra.video_sdk.call.brandlogo.model.BrandLogoState
import com.kaleyra.video_sdk.call.brandlogo.model.Logo
import com.kaleyra.video_sdk.call.brandlogo.viewmodel.BrandLogoViewModel
import com.kaleyra.video_sdk.call.callactions.model.CallActionsUiState
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.fileshare.model.FileShareUiState
import com.kaleyra.video_sdk.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.InputPermissions
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.VCallScreen
import com.kaleyra.video_sdk.call.screenshare.model.ScreenShareUiState
import com.kaleyra.video_sdk.call.screenshare.viewmodel.ScreenShareViewModel
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.video_sdk.call.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
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
import org.junit.Rule
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
internal abstract class VCallScreenBaseTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    val callActionsUiState = MutableStateFlow(CallActionsUiState())

    val streamUiState = MutableStateFlow(StreamUiState())

    val callViewModel = mockk<CallActionsViewModel>(relaxed = true) {
        every { uiState } returns callActionsUiState
    }

    val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    val audioOutputViewModel = mockk<AudioOutputViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(AudioOutputUiState())
    }

    val screenShareViewModel = mockk<ScreenShareViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(ScreenShareUiState())
    }

    val fileShareViewModel = mockk<FileShareViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(FileShareUiState())
    }

    val whiteboardViewModel = mockk<WhiteboardViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(WhiteboardUiState())
    }

    val virtualBackgroundViewModel = mockk<VirtualBackgroundViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(VirtualBackgroundUiState())
    }

    val callInfoViewModel = mockk<CallInfoViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(
            CallInfoUiState(callStateUi = CallStateUi.Disconnected.Ended, displayState = TextRef.StringResource(
                R.string.kaleyra_strings_info_status_connecting))
        )
    }

    val callAppBarViewModel = mockk<CallAppBarViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(CallAppBarUiState())
    }

    val userMessagesViewModel = mockk<UserMessagesViewModel>(relaxed = true) {
        every { uiState } returns MutableStateFlow(StackedSnackbarUiState())
    }

    val companyLogo = Logo(light = Uri.parse("https://www.example.com/light.png"), dark = Uri.parse("https://www.example.com/dark.png"))
    val brandLogoUiState = MutableStateFlow(BrandLogoState())
    val brandLogoViewModel = mockk<BrandLogoViewModel>(relaxed = true) {
        every { uiState } returns brandLogoUiState
    }

    val phoneChatViewModelState = MutableStateFlow(ChatUiState.OneToOne())
    val phoneChatViewModel = mockk<PhoneChatViewModel>(relaxed = true) {
        every { theme } returns MutableStateFlow(Theme())
        every { uiState } returns phoneChatViewModelState
    }

    val allActions = listOf(
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
        mockkObject(KaleyraVideo)
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
        mockkObject(BrandLogoViewModel)
        mockkObject(PhoneChatViewModel)

        every { KaleyraVideo.isConfigured } returns true
        every { KaleyraVideo.conference } returns mockk(relaxed = true)
        every { KaleyraVideo.conversation } returns mockk(relaxed = true)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk(relaxed = true))

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
                create(any<KClass<VirtualBackgroundViewModel>>(), any())
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

        every { BrandLogoViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<BrandLogoViewModel>>(), any()) } returns brandLogoViewModel
        }

        every { PhoneChatViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<PhoneChatViewModel>>(), any()) } returns phoneChatViewModel
        }
        ContextRetainer().create(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.setUpVCallScreen(
        sheetState: CallSheetState = CallSheetState(),
        onChangeSheetState: (Boolean) -> Unit = {},
        selectedStreamId: String? = null,
        onStreamSelected: (String?) -> Unit = {},
        modalSheetComponent: ModularComponent? = null,
        sidePanelComponent: ModularComponent? = null,
        onModalSheetComponentRequest: (ModularComponent?) -> Unit = { },
        onSidePanelComponentRequest: (ModularComponent?) -> Unit = { },
        onModularComponentDisplayed: (ModularComponent?) -> Unit = { },
        onAskInputPermissions: (Boolean) -> Unit = {},
        onBackPressed: () -> Unit = {},
        onChatDeleted: () -> Unit = {},
        onChatCreationFailed: () -> Unit = {},
        inputPermissions: InputPermissions = InputPermissions()
    ) {
        setContent {
            VCallScreen(
                windowSizeClass = calculateWindowSizeClass(activity),
                sheetState = sheetState,
                modalSheetComponent = modalSheetComponent,
                sidePanelComponent = sidePanelComponent,
                onChangeSheetState = onChangeSheetState,
                selectedStreamId = selectedStreamId,
                onStreamSelected = onStreamSelected,
                inputPermissions = inputPermissions,
                modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                onModalSheetComponentRequest = onModalSheetComponentRequest,
                onSidePanelComponentRequest = onSidePanelComponentRequest,
                onModularComponentDisplayed = onModularComponentDisplayed,
                onAskInputPermissions = onAskInputPermissions,
                onBackPressed = onBackPressed,
                onChatDeleted = onChatDeleted,
                onChatCreationFailed = onChatCreationFailed,
                isTesting = true
            )
        }
    }

}