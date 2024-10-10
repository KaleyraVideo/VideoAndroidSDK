package com.kaleyra.video_sdk.ui.call.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video.Company
import com.kaleyra.video_common_ui.ChatViewModel
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.theme.CompanyThemeManager
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.screen.model.MainUiState
import com.kaleyra.video_sdk.call.screen.view.ModalSheetComponent
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.SidePanel
import com.kaleyra.video_sdk.call.screen.viewmodel.MainViewModel
import com.kaleyra.video_sdk.chat.PhoneChatActivity
import com.kaleyra.video_sdk.chat.input.TextFieldTag
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.unmockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.reflect.KClass

@RunWith(RobolectricTestRunner::class)
class SidePanelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var component by mutableStateOf(ModalSheetComponent.Audio)

    private var onDismissed = false

    private var sideBarComponentDisplayed: ModalSheetComponent? = null

    private val mainViewModel = mockk<MainViewModel>(relaxed = true)

    private val chatViewModel = mockk<PhoneChatViewModel>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(MainViewModel)
        mockkObject(PhoneChatViewModel)
        mockkObject(CompanyThemeManager)

        with(mainViewModel) {
            every { uiState } returns MutableStateFlow(MainUiState())
            every { getOtherUserId() } returns "otherId"
        }

        with(chatViewModel) {
            every { uiState } returns MutableStateFlow(ChatUiState.OneToOne())
            every { getLoggedUserId() } returns "loggedId"
            every { theme } returns MutableStateFlow(CompanyUI.Theme())

        }

        every { MainViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<MainViewModel>>(), any()) } returns mainViewModel
        }
        every { PhoneChatViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<PhoneChatViewModel>>(), any()) } returns chatViewModel
        }

        composeTestRule.setContent {
            SidePanel(
                modalSheetComponent = component,
                onDismiss = { onDismissed = true },
                onSideBarComponentDisplayed = { sideBarComponentDisplayed = it }
            )
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
        component = ModalSheetComponent.Audio
        sideBarComponentDisplayed = null
        onDismissed = false
    }

    @Test
    fun fileShareComponent_fileShareComponentIsDisplayed() {
        component = ModalSheetComponent.FileShare
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModalSheetComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_whiteboardComponentIsDisplayed() {
        component = ModalSheetComponent.Whiteboard
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModalSheetComponent.Whiteboard)
    }

    @Test
    fun participantsComponent_participantsComponentIsDisplayed() {
        component = ModalSheetComponent.Participants
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModalSheetComponent.Participants)
    }

    @Test
    fun chatComponent_chatComponentIsDisplayed() {
        component = ModalSheetComponent.Chat
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_chat)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TextFieldTag).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModalSheetComponent.Chat)
        coVerify(exactly = 1) {
            chatViewModel.setChat("loggedId", "otherId")
        }
    }

    @Test
    fun chatComponentAndLoggedUserIdIsNull_onDismissInvoked() {
        component = ModalSheetComponent.Chat
        every { chatViewModel.getLoggedUserId() } returns null

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_chat)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()

        coVerify(exactly = 0) {
            chatViewModel.setChat(any(), any())
        }
        assertEquals(true, onDismissed)
    }

    @Test
    fun chatComponentAndOtherUserIdIsNull_onDismissInvoked() {
        component = ModalSheetComponent.Chat
        every { mainViewModel.getOtherUserId() } returns null

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_chat)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()

        coVerify(exactly = 0) {
            chatViewModel.setChat(any(), any())
        }
        assertEquals(true, onDismissed)
    }
}