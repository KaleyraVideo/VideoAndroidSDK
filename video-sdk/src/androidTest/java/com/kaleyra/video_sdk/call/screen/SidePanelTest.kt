package com.kaleyra.video_sdk.call.screen

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.theme.CompanyThemeManager
import com.kaleyra.video_common_ui.theme.Theme
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screen.model.MainUiState
import com.kaleyra.video_sdk.call.screen.model.ModularComponent
import com.kaleyra.video_sdk.call.screen.view.vcallscreen.SidePanel
import com.kaleyra.video_sdk.call.screen.viewmodel.MainViewModel
import com.kaleyra.video_sdk.chat.input.TextFieldTag
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.reflect.KClass

class SidePanelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var component by mutableStateOf(ModularComponent.Audio)

    private var onDismissed = false

    private var sideBarComponentDisplayed: ModularComponent? = null

    private val mainViewModel = mockk<MainViewModel>(relaxed = true)

    private val chatViewModel = mockk<PhoneChatViewModel>(relaxed = true)

    private var isChatDeleted = false

    private var isChatCreationFailed = false

    private val chatUiState = MutableStateFlow(ChatUiState.OneToOne())
    @Before
    fun setUp() {
        mockkObject(MainViewModel)
        mockkObject(PhoneChatViewModel)
        mockkObject(CompanyThemeManager)

        with(mainViewModel) {
            every { uiState } returns MutableStateFlow(MainUiState())
            coEvery { getChatId() } returns "chatId"
        }

        with(chatViewModel) {
            every { uiState } returns chatUiState
            every { getLoggedUserId() } returns "loggedId"
            every { theme } returns MutableStateFlow(Theme())

        }

        every { MainViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<MainViewModel>>(), any()) } returns mainViewModel
        }
        every { PhoneChatViewModel.provideFactory(any()) } returns mockk {
            every { create(any<KClass<PhoneChatViewModel>>(), any()) } returns chatViewModel
        }

        composeTestRule.setContent {
            SidePanel(
                modularComponent = component,
                onDismiss = { onDismissed = true },
                onComponentDisplayed = { sideBarComponentDisplayed = it },
                onChatDeleted = { isChatDeleted = true },
                onChatCreationFailed = { isChatCreationFailed = true },
                onOtherModularComponentReuquested = { }
            )
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
        component = ModularComponent.Audio
        sideBarComponentDisplayed = null
        onDismissed = false
        isChatDeleted = false
        isChatCreationFailed = false
    }

    @Test
    fun fileShareComponent_fileShareComponentIsDisplayed() {
        component = ModularComponent.FileShare
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_fileshare)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModularComponent.FileShare)
    }

    @Test
    fun whiteboardComponent_whiteboardComponentIsDisplayed() {
        component = ModularComponent.Whiteboard
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_whiteboard)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModularComponent.Whiteboard)
    }

    @Test
    fun participantsComponent_participantsComponentIsDisplayed() {
        component = ModularComponent.Participants
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_participants_component_change_layout)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModularComponent.Participants)
    }

    @Test
    fun chatComponent_chatComponentIsDisplayed() {
        component = ModularComponent.Chat
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_chat)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TextFieldTag).assertIsDisplayed()
        assertEquals(sideBarComponentDisplayed, ModularComponent.Chat)
        coVerify(exactly = 1) {
            chatViewModel.setChat("loggedId", "chatId")
        }
    }

    @Test
    fun chatComponent_chatIsDeleted_chatIsDeletedInvoked() {
        chatUiState.tryEmit(ChatUiState.OneToOne(isDeleted = true))
        component = ModularComponent.Chat
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_chat)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TextFieldTag).assertIsNotDisplayed()
        assertEquals(sideBarComponentDisplayed, ModularComponent.Chat)
        assert(isChatDeleted)
    }

    @Test
    fun chatComponent_chatHasFailedCreation_chatHasFailedCreationInvoked() {
        chatUiState.tryEmit(ChatUiState.OneToOne(hasFailedCreation = true))
        component = ModularComponent.Chat
        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_chat)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TextFieldTag).assertIsNotDisplayed()
        assertEquals(sideBarComponentDisplayed, ModularComponent.Chat)
        assert(isChatCreationFailed)
    }

    @Test
    fun chatComponentAndLoggedUserIdIsNull_onDismissInvoked() {
        component = ModularComponent.Chat
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
        component = ModularComponent.Chat
        coEvery { mainViewModel.getChatId() } returns null

        val componentTitle = composeTestRule.activity.getString(R.string.kaleyra_chat)
        composeTestRule.onNodeWithText(componentTitle).assertIsDisplayed()

        coVerify(exactly = 0) {
            chatViewModel.setChat(any(), any())
        }
        assertEquals(true, onDismissed)
    }
}