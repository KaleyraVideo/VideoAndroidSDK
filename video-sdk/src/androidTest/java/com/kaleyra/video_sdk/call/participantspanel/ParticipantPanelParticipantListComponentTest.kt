package com.kaleyra.video_sdk.call.participantspanel

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantsPanelCallParticipantsListComponent
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParticipantPanelParticipantListComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testParticipantPanelCallParticipantsComponentInCallUserSectionDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelCallParticipantsListComponent(
                inCallStreamUi = ImmutableList((0..10).map {
                    streamUiMock
                }),
                invitedParticipants = ImmutableList(listOf("user1", "user2")),
                isLoggedUserAdmin = true,
                adminUserId = "user1",
                isDarkTheme = true,
                onPin = { streamUi, pinned ->},
                onMute = { streamUi, muted ->},            )
        }
        val inCallUsersLabelText = composeTestRule.activity.getString(R.string.kaleyra_users_in_call)
        composeTestRule.onNodeWithText(inCallUsersLabelText).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelCallParticipantsComponentInvitedUserSectionDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelCallParticipantsListComponent(
                inCallStreamUi = ImmutableList((0..2).map {
                    streamUiMock
                }),
                invitedParticipants = ImmutableList(listOf("user1", "user2")),
                isLoggedUserAdmin = true,
                adminUserId = "user1",
                onPin = { streamUi, pinned ->},
                onMute = { streamUi, muted ->},
                isDarkTheme = true
            )
        }
        val invitedUsersLabelText = composeTestRule.activity.getString(R.string.kaleyra_invited_users)
        composeTestRule.onNodeWithText(invitedUsersLabelText).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelCallParticipantsComponentInvitedUsersDisplayed() {
        val invitedParticipants = listOf("user1", "user2")
        composeTestRule.setContent {
            ParticipantsPanelCallParticipantsListComponent(
                inCallStreamUi = ImmutableList(listOf()),
                invitedParticipants = ImmutableList(invitedParticipants),
                isLoggedUserAdmin = true,
                adminUserId = "user1",
                onPin = { streamUi, pinned ->},
                onMute = { streamUi, muted ->},
                isDarkTheme = true
            )
        }
        invitedParticipants.forEach { invitedParticipant ->
            composeTestRule.onNodeWithText(invitedParticipant).assertIsDisplayed()
        }
    }

    @Test
    fun testParticipantPanelCallParticipantsComponentInCallUsersDisplayed() {
        val inCallParticipants = listOf("user1", "user2")
        composeTestRule.setContent {
            ParticipantsPanelCallParticipantsListComponent(
                inCallStreamUi = ImmutableList(
                    inCallParticipants.map { inCallParticipant ->
                        streamUiMock.copy(username = inCallParticipant)
                    }),
                invitedParticipants = ImmutableList(listOf()),
                isLoggedUserAdmin = true,
                adminUserId = "user1",
                isDarkTheme = true,
                onPin = { streamUi, pinned ->},
                onMute = { streamUi, muted ->},            )
        }
        inCallParticipants.forEach { invitedParticipant ->
            composeTestRule.onNodeWithText(invitedParticipant).assertIsDisplayed()
        }
    }

    @Test
    fun testParticipantPanelCallParticipantsComponentNoInCallUserSectionNotDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelCallParticipantsListComponent(
                inCallStreamUi = ImmutableList(listOf()),
                invitedParticipants = ImmutableList(listOf("user1", "user2")),
                isLoggedUserAdmin = true,
                adminUserId = "user1",
                isDarkTheme = true,
                onPin = { streamUi, pinned ->},
                onMute = { streamUi, muted ->},            )
        }
        val inCallUsersLabelText = composeTestRule.activity.getString(R.string.kaleyra_users_in_call)
        composeTestRule.onNodeWithText(inCallUsersLabelText).assertDoesNotExist()
    }

    @Test
    fun testParticipantPanelCallParticipantsComponentNotInvitedUserSectionNotDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelCallParticipantsListComponent(
                inCallStreamUi = ImmutableList((0..10).map { index ->
                    streamUiMock.let { it.copy(username = "$it$index") }
                }),
                invitedParticipants = ImmutableList(listOf()),
                isLoggedUserAdmin = true,
                adminUserId = "user1",
                isDarkTheme = true,
                onPin = { streamUi, pinned ->},
                onMute = { streamUi, muted ->},            )
        }
        val invitedUsersLabelText = composeTestRule.activity.getString(R.string.kaleyra_invited_users)
        composeTestRule.onNodeWithText(invitedUsersLabelText).assertDoesNotExist()
    }
}