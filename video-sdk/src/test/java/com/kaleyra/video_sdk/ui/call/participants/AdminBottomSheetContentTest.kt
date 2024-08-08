package com.kaleyra.video_sdk.ui.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.view.AdminBottomSheetContent
import com.kaleyra.video_sdk.call.streamnew.model.core.AudioUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.call.streamnew.model.core.streamUiMock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AdminBottomSheetContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var stream by mutableStateOf(streamUiMock)

    private var streamPinned by mutableStateOf(false)

    private var pinLimitReached by mutableStateOf(false)

    private var onClickStreamId: String? = null

    private var onClickStreamPinned = false

    private var onClickMuteForYou = false

    private var kickParticipantClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            AdminBottomSheetContent(
                stream = stream,
                isStreamPinned = streamPinned,
                isPinLimitReached = pinLimitReached,
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
        stream = streamUiMock
        streamPinned = false
        kickParticipantClicked = false
        onClickStreamPinned = false
        onClickMuteForYou = false
        onClickStreamId = null
    }

    @Test
    fun avatarFailsToLoad_letterIsDisplayed() {
        stream = streamUiMock.copy(username = "custom")
        composeTestRule.onNodeWithText("C").assertIsDisplayed()
    }

    @Test
    fun testUsernameIsDisplayed() {
        stream = streamUiMock.copy(username = "custom")
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
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream)
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun streamIsPinned_unpinButtonIsDisplayed() {
        streamPinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream)
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun streamAudioIsNotMuted_muteAudioForMeButtonIsDisplayed() {
        stream = streamUiMock.copy(audio = AudioUi("id", isMutedForYou = false))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you)
        composeTestRule.onNodeWithText(description).assertHasClickAction()
        composeTestRule.onNodeWithText(description).assertIsDisplayed()
    }

    @Test
    fun streamAudioIsMuted_unmuteAudioForMeButtonIsDisplayed() {
        stream = streamUiMock.copy(audio = AudioUi("id", isMutedForYou = true))
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
        stream = streamUiMock.copy(id = "customStreamId")
        streamPinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", stream.id)
        assertEquals(false, onClickStreamPinned)
    }

    @Test
    fun testOnPinStreamClick() {
        stream = streamUiMock.copy(id = "customStreamId")
        streamPinned = false
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", stream.id)
        assertEquals(true, onClickStreamPinned)
    }

    @Test
    fun testOnMuteStreamClick() {
        stream = streamUiMock.copy(id = "customStreamId", audio = AudioUi("id", isMutedForYou = false))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", stream.id)
        assertEquals(true, onClickMuteForYou)
    }

    @Test
    fun testOnUnMuteStreamClick() {
        stream = streamUiMock.copy(id = "customStreamId", audio = AudioUi("id", isMutedForYou = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you)
        composeTestRule.onNodeWithText(description).performClick()
        assertEquals("customStreamId", stream.id)
        assertEquals(false, onClickMuteForYou)
    }

    @Test
    fun remoteScreenShareStream_muteForYouIsNotEnabled() {
        stream = streamUiMock.copy(isMine = false, audio = AudioUi(id = "id", isEnabled = true), video = VideoUi(id = "id", isEnabled = false, isScreenShare = true))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you)
        composeTestRule
            .onNodeWithText(description)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun localScreenShareStream_unpinButtonIsDisabled() {
        stream = streamUiMock.copy(isMine = true, video = VideoUi(id = "id", isEnabled = true, isScreenShare = true))
        streamPinned = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream)
        composeTestRule
            .onNodeWithText(description)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun pinLimitReached_pinButtonIsDisabled() {
        stream = streamUiMock.copy(isMine = true, video = VideoUi(id = "id", isEnabled = true, isScreenShare = true))
        pinLimitReached = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream)
        composeTestRule
            .onNodeWithText(description)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun pinLimitReached_unpinButtonIsEnabled() {
        stream = streamUiMock.copy(video = VideoUi(id = "id", isEnabled = true))
        streamPinned = true
        pinLimitReached = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream)
        composeTestRule
            .onNodeWithText(description)
            .assertHasClickAction()
            .assertIsEnabled()
    }
}
