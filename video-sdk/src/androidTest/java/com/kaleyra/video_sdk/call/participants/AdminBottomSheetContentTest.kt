package com.kaleyra.video_sdk.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.view.AdminBottomSheetContent
import com.kaleyra.video_sdk.call.stream.model.AudioUi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdminBottomSheetContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var username by mutableStateOf("username")

    private var streamId by mutableStateOf("streamId")

    private var streamPinned by mutableStateOf(false)

    private var streamAudio by mutableStateOf<AudioUi?>(null)

    private var onClickStreamId: String? = null

    private var onClickStreamPinned = false

    private var onClickMuteForYou = false

    private var kickParticipantClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AdminBottomSheetContent(
                username = username,
                avatar = null,
                streamId = streamId,
                streamAudio = streamAudio,
                streamPinned = streamPinned,
                onMuteStreamClick = { streamId, value ->
                    onClickStreamId = streamId
                    onClickMuteForYou = value
                },
                onPinStreamClick = { streamId, value ->
                    onClickStreamId = streamId
                    onClickStreamPinned = value
                },
                onKickParticipantClick = { kickParticipantClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        streamId = "streamId"
        streamPinned = false
        streamAudio = null
        kickParticipantClicked = false
        onClickStreamPinned = false
        onClickMuteForYou = false
        onClickStreamId = null
    }

    @Test
    fun testUserAvatarIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_avatar)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun avatarFailsToLoad_letterIsDisplayed() {
        username = "custom"
        composeTestRule.onNodeWithText("c").assertIsDisplayed()
    }

    @Test
    fun testUsernameIsDisplayed() {
        username = "custom"
        composeTestRule.onNodeWithText("custom").assertIsDisplayed()
    }

    @Test
    fun testRemoveFromCallButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_remove_from_call)
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun streamIsNotPinned_pinButtonIsDisplayed() {
        streamPinned = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun streamIsPinned_unpinButtonIsDisplayed() {
        streamPinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin)
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun streamAudioIsNotMuted_muteAudioForMeButtonIsDisplayed() {
        streamAudio = AudioUi("id", isMutedForYou = false)
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you)
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun streamAudioIsMuted_unmuteAudioForMeButtonIsDisplayed() {
        streamAudio = AudioUi("id", isMutedForYou = true)
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you)
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun testOnRemoveFromCallButtonClick() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_remove_from_call)
        composeTestRule.onNodeWithText(text).performClick()
        assertEquals(true, kickParticipantClicked)
    }

    @Test
    fun testOnUnpinStreamClick() {
        streamId = "customStreamId"
        streamPinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(false, onClickStreamPinned)
    }

    @Test
    fun testOnPinStreamClick() {
        streamId = "customStreamId"
        streamPinned = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(true, onClickStreamPinned)
    }

    @Test
    fun testOnMuteStreamClick() {
        streamId = "customStreamId"
        streamAudio = AudioUi("id", isMutedForYou = false)
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(true, onClickMuteForYou)
    }

    @Test
    fun testOnUnMuteStreamClick() {
        streamId = "customStreamId"
        streamAudio = AudioUi("id", isMutedForYou = true)
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", streamId)
        assertEquals(false, onClickMuteForYou)
    }
}
