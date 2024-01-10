package com.kaleyra.video_sdk.call.participantspanel

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantsPanelDetailComponent
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParticipantPanelDetailComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testParticipantPanelDetailsShownUsernameDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock,
                isLoggedUserAdmin = false,
                adminUserId = "admin",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { }
            )
        }
        val text = streamUiMock.username
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelDetailsShownWhenLoggedUserIsAdminMoreButtonShown() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock,
                isLoggedUserAdmin = true,
                adminUserId = "admin",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { }
            )
        }
        val moreButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_more_description)
        composeTestRule.onNodeWithContentDescription(moreButtonContentDescription).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelDetailsShownWhenLoggedUserIsNotAdminMoreButtonNotShown() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock,
                isLoggedUserAdmin = false,
                adminUserId = "admin",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        val moreButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_more_description)
        composeTestRule.onNodeWithContentDescription(moreButtonContentDescription).assertDoesNotExist()
    }

    @Test
    fun testParticipantPanelDetailsShownWhenStreamUiPinnedUnpinButtonShown() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = true),
                isLoggedUserAdmin = false,
                adminUserId = "admin",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        val unPinnedContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_unpin_description)
        composeTestRule.onNodeWithContentDescription(unPinnedContentDescription).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelDetailsShownWhenStreamUiNotPinnedPinButtonShown() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = false),
                isLoggedUserAdmin = false,
                adminUserId = "admin",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        val pinnedContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_pin_description)
        composeTestRule.onNodeWithContentDescription(pinnedContentDescription).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelDetailsShownWhenUserIsAdminSubtitleAdminShown() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = false),
                isLoggedUserAdmin = false,
                adminUserId = "username",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        val adminLabelText = composeTestRule.activity.getString(R.string.kaleyra_admin_user)
        composeTestRule.onNodeWithText(adminLabelText).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelDetailsShownWhenUserIsAdminSubtitleParticipantShown() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = false),
                isLoggedUserAdmin = false,
                adminUserId = "username2",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        val participantLabelText = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_participants, 1)
        composeTestRule.onNodeWithText(participantLabelText).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelDetailsShownUsernameShown() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = false),
                isLoggedUserAdmin = false,
                adminUserId = "username2",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        composeTestRule.onNodeWithText(streamUiMock.username).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelDetailsMoreButtonClickedCallbackCalled() {
        var clicked = false
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = false),
                isLoggedUserAdmin = true,
                adminUserId = "username2",
                onMoreClicked = { clicked = true },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        val moreButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_more_description)
        composeTestRule.onNodeWithContentDescription(moreButtonContentDescription).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testParticipantPanelDetailsPinButtonClickedCallbackCalled() {
        var pin: Boolean? = null
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = false),
                isLoggedUserAdmin = false,
                adminUserId = "username2",
                onMoreClicked = { },
                onPinClicked = { pinned -> pin = pinned },
                onMuteClicked = { },
            )
        }
        val pinButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_pin_description)
        composeTestRule.onNodeWithContentDescription(pinButtonContentDescription).performClick()
        Assert.assertEquals(true, pin)
    }

    @Test
    fun testParticipantPanelDetailsUnpinButtonClickedCallbackCalled() {
        var pin: Boolean? = null
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = true),
                isLoggedUserAdmin = false,
                adminUserId = "username2",
                onMoreClicked = { },
                onPinClicked = { pinned -> pin = pinned },
                onMuteClicked = { },
            )
        }
        val unpinButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_call_action_unpin_description)
        composeTestRule.onNodeWithContentDescription(unpinButtonContentDescription).performClick()
        Assert.assertEquals(false, pin)
    }

    @Test
    fun testParticipantPanelDetailsAvatarExists() {
        composeTestRule.setContent {
            ParticipantsPanelDetailComponent(
                streamUi = streamUiMock.copy(pinned = true),
                isLoggedUserAdmin = false,
                adminUserId = "username2",
                onMoreClicked = { },
                onPinClicked = { },
                onMuteClicked = { },
            )
        }
        val userAvatarContentDescription = composeTestRule.activity.getString(R.string.kaleyra_avatar)
        composeTestRule.onNodeWithContentDescription(userAvatarContentDescription).assertIsDisplayed()
    }
}