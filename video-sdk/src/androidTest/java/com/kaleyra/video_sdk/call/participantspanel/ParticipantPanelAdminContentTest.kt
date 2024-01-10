package com.kaleyra.video_sdk.call.participantspanel

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantPanelAdminBottomSheetItemsForStreamUi
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantPanelAdminContent
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParticipantPanelAdminContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testUsernameShown() {
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUiMock,
                onPinClicked = { a, b -> },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock)
            )
        }
        composeTestRule.onNodeWithText(streamUiMock.username).assertIsDisplayed()
    }

    @Test
    fun testAvatarShown() {
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUiMock,
                onPinClicked = { a, b -> },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock)
            )
        }
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_avatar)).assertIsDisplayed()
    }

    @Test
    fun testPinOptionShown() {
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUiMock,
                onPinClicked = { a, b -> },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_pin_description)).assertIsDisplayed()
    }

    @Test
    fun testUnPinOptionShown() {
        val streamUi = streamUiMock.copy(pinned = true)
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUi,
                onPinClicked = { a, b -> },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUi)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_unpin_description)).assertIsDisplayed()
    }

    @Test
    fun testMuteForYouOptionShown() {
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUiMock,
                onPinClicked = { a, b -> },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute_for_you)).assertIsDisplayed()
    }

    @Test
    fun testUnmuteForYouOptionShown() {
        val streamUi = streamUiMock.copy(audio = streamUiMock.audio!!.copy(isEnabledForYou = false))
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUi,
                onPinClicked = { a, b -> },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUi)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_unmute_for_you)).assertIsDisplayed()
    }

    @Test
    fun testRemoveFromCallOptionShown() {
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUiMock,
                onPinClicked = { a, b -> },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_remove_participant_from_call)).assertIsDisplayed()
    }

    @Test
    fun testPinClicked() {
        var isPinned = false
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUiMock,
                onPinClicked = { streamUi, pinned ->
                    isPinned = pinned
                },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_pin_description)).performClick()
        Assert.assertEquals(true, isPinned)
    }

    @Test
    fun testUnpinClicked() {
        var isPinned: Boolean? = null
        val streamUi = streamUiMock.copy(pinned = true)
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUi,
                onPinClicked = { streamUi, pinned ->
                    isPinned = pinned
                },
                onMuteForYouClicked = { a, b -> },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUi)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_unpin_description)).performClick()
        Assert.assertEquals(false, isPinned)
    }

    @Test
    fun testMuteForYouClicked() {
        var isEnabledForYouCheck: Boolean? = null
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUiMock,
                onPinClicked = { _, _ -> },
                onMuteForYouClicked = { streamUi, isEnabledForYou ->
                    isEnabledForYouCheck = isEnabledForYou
                },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_mute_for_you)).performClick()
        Assert.assertEquals(false, isEnabledForYouCheck)
    }

    @Test
    fun testUnmuteForYouClicked() {
        var isEnabledForYouCheck: Boolean? = null
        val streamUi = streamUiMock.copy(audio = streamUiMock.audio!!.copy(isEnabledForYou = false))
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUi,
                onPinClicked = { _, _ -> },
                onMuteForYouClicked = { streamUi, isEnabledForYou ->
                    isEnabledForYouCheck = isEnabledForYou
                },
                onRemoveFromCallClicked = {},
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUi)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_mic_unmute_for_you)).performClick()
        Assert.assertEquals(true, isEnabledForYouCheck)
    }

    @Test
    fun testRemoveFromCallClicked() {
        var hasRemovedFromCall: Boolean? = null
        val streamUi = streamUiMock
        composeTestRule.setContent {
            ParticipantPanelAdminContent(
                isSystemInDarkTheme = isSystemInDarkTheme(),
                streamUi = streamUi,
                onPinClicked = { _, _ -> },
                onMuteForYouClicked = { _, _ -> },
                onRemoveFromCallClicked = {
                    hasRemovedFromCall = true
                },
                items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUi)
            )
        }
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.kaleyra_call_action_remove_participant_from_call)).performClick()
        Assert.assertEquals(true, hasRemovedFromCall)
    }
}