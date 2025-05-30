package com.kaleyra.video_sdk.ui.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.AdminBottomSheetTag
import com.kaleyra.video_sdk.call.participants.ParticipantsComponent
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.ui.performVerticalSwipe
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ParticipantsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streamsLayout by mutableStateOf(StreamsLayout.Mosaic)

    private var streams by mutableStateOf(ImmutableList<StreamUi>())

    private var adminStreamsIds by mutableStateOf(ImmutableList<String>())

    private var pinnedStreamsIds by mutableStateOf(ImmutableList<String>())

    private var invited by mutableStateOf(ImmutableList<String>())

    private var amIAdmin by mutableStateOf(false)

    private var isPinLimitReached by mutableStateOf(false)

    private var participantsCount by mutableStateOf(0)

    private var layoutClicked: StreamsLayout? = null

    private var clickedStreamId: String? = null

    private var isStreamMuted: Boolean? = null

    private var isStreamMicDisabled: Boolean? = null

    private var isStreamPinned: Boolean? = null

    private var isCloseClicked = false

    private var isKickParticipantClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ParticipantsComponent(
                streamsLayout = streamsLayout,
                streams = streams,
                invited = invited,
                adminsStreamsIds = adminStreamsIds,
                pinnedStreamsIds = pinnedStreamsIds,
                amIAdmin = amIAdmin,
                isPinLimitReached = isPinLimitReached,
                participantsCount = participantsCount,
                onLayoutClick = { layoutClicked = it },
                onMuteStreamClick = { streamId, value ->
                    clickedStreamId = streamId
                    isStreamMuted = value
                },
                onDisableMicClick = { streamId, value ->
                    clickedStreamId = streamId
                    isStreamMicDisabled = value
                },
                onPinStreamClick = { streamId, value ->
                    clickedStreamId = streamId
                    isStreamPinned = value
                },
                onKickParticipantClick = { isKickParticipantClicked = true },
                onCloseClick = { isCloseClicked = true }
            )
        }
    }

    @After
    fun tearDown() {
        streamsLayout = StreamsLayout.Mosaic
        streams = ImmutableList()
        adminStreamsIds = ImmutableList()
        pinnedStreamsIds = ImmutableList()
        invited = ImmutableList()
        amIAdmin = false
        layoutClicked = null
        isStreamMuted = null
        isStreamMicDisabled = null
        isStreamPinned = null
        isCloseClicked = false
        isKickParticipantClicked = false
        participantsCount = 0
    }

    @Test
    fun testParticipantCountIsDisplayed() {
        participantsCount = 4
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_participants_component_participants, 4, 4)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testCloseIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(text).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun testMosaicButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mosaic)
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testAutoButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_auto)
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun avatarFailsToLoad_letterIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(userInfo = UserInfo("userId", "username", ImmutableUri()))))
        composeTestRule.onNodeWithText("U").assertIsDisplayed()
    }

    @Test
    fun testYouIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = true)))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_you, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testUsernameIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock))
        composeTestRule.onNodeWithText(streamUiMock.userInfo!!.username).assertIsDisplayed()
    }

    @Test
    fun testAdminTextIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock))
        adminStreamsIds = ImmutableList(listOf(streamUiMock.id))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_admin)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testScreenShareTextIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(video = VideoUi(id = "id", isScreenShare = true))))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_screenshare)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testScreenShareTextIsDisplayedWhenUserAdmin() {
        adminStreamsIds = ImmutableList(listOf(streamUiMock.id))
        streams = ImmutableList(listOf(streamUiMock.copy(video = VideoUi(id = "id", isScreenShare = true))))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_screenshare)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testParticipantTextIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_participant)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndAudioIsDisabled_enableMicButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = false))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_enable_microphone_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndAudioIsEnabled_disableMicButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = true))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmAdminAndAudioIsDisabled_enableMicButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = false))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_enable_microphone_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmAdminAndAudioIsEnabled_disableMicButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = true, audio = AudioUi(id = "id", isEnabled = true))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndStreamIsNotMineAndAudioIsNotMuted_muteAudioForMeButtonIsDisplayed() {
        amIAdmin = false
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = false, audio = AudioUi("id", isMutedForYou = false))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndStreamIsNotMineAndAudioIsMuted_unmuteAudioForMeButtonIsDisplayed() {
        amIAdmin = false
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = false, audio = AudioUi("id", isMutedForYou = true))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndIsPinned_unpinButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = true)))
        pinnedStreamsIds = ImmutableList(listOf(streamUiMock.id))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun streamIsMineAndIsNotPinned_pinButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = true)))
        pinnedStreamsIds = ImmutableList(listOf())
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndIsPinned_unpinButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock))
        amIAdmin = false
        pinnedStreamsIds = ImmutableList(listOf(streamUiMock.id))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmNotAdminAndIsNotPinned_pinButtonIsDisplayed() {
        streams = ImmutableList(listOf(streamUiMock))
        amIAdmin = false
        pinnedStreamsIds = ImmutableList(listOf())
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun iAmAdminAndStreamIsNotMine_showMoreActionsButtonIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = false)))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_show_more_actions)
        composeTestRule.onNodeWithContentDescription(description).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(description).assertIsDisplayed()
    }

    @Test
    fun participantItemMoreClick_adminBottomSheetIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(isMine = false)))
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithTag(AdminBottomSheetTag).assertIsDisplayed()
    }

    @Test
    fun adminBottomSheetUserAvatarFailsToLoad_letterIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onAllNodesWithText(streamUiMock.userInfo!!.username[0].uppercase())[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText(streamUiMock.userInfo!!.username[0].uppercase())[1].assertIsDisplayed()
    }

    @Test
    fun testAdminBottomSheetUsernameIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onAllNodesWithText(streamUiMock.userInfo!!.username)[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText(streamUiMock.userInfo!!.username)[1].assertIsDisplayed()
    }

    @Test
    fun testAdminBottomSheetRemoveFromCallButtonIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_remove_from_call)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun streamIsNotPinned_adminBottomSheetPinButtonIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId")))
        pinnedStreamsIds = ImmutableList(listOf())
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun streamIsPinned_adminBottomSheetUnpinButtonIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId")))
        pinnedStreamsIds = ImmutableList(listOf("customStreamId"))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun streamAudioIsNotMuted_adminBottomSheetMuteAudioForMeButtonIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(audio = AudioUi("id", isMutedForYou = false))))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun streamAudioIsMuted_adminBottomSheetUnmuteAudioForMeButtonIsDisplayed() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(audio = AudioUi("id", isMutedForYou = true))))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testOnClickMosaicButton() {
        streamsLayout = StreamsLayout.Auto
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mosaic)
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals(StreamsLayout.Mosaic, layoutClicked)
    }

    @Test
    fun testOnClickAutoButton() {
        streamsLayout = StreamsLayout.Mosaic
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_auto)
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals(StreamsLayout.Auto, layoutClicked)
    }

    @Test
    fun testOnCloseClick() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, isCloseClicked)
    }

    @Test
    fun testParticipantItemOnMuteStreamClick() {
        amIAdmin = false
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", isMine = false, audio = AudioUi("id", isEnabled = true, isMutedForYou = false))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(true, isStreamMuted)
    }

    @Test
    fun testParticipantItemOnUnMuteStreamClick() {
        amIAdmin = false
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", isMine = false, audio = AudioUi("id", isMutedForYou = true))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(false, isStreamMuted)
    }

    @Test
    fun testParticipantItemOnDisableMicClick() {
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", isMine = true, audio = AudioUi(id = "id", isEnabled = true))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(true, isStreamMicDisabled)
    }

    @Test
    fun testParticipantItemOnEnableMicClick() {
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", isMine = true, audio = AudioUi(id = "id", isEnabled = false))))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_enable_microphone_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(false, isStreamMicDisabled)
    }

    @Test
    fun testParticipantItemOnUnpinStreamClick() {
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", isMine = true)))
        pinnedStreamsIds = ImmutableList(listOf("customStreamId"))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(false, isStreamPinned)
    }

    @Test
    fun testParticipantItemOnPinStreamClick() {
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", isMine = true)))
        pinnedStreamsIds = ImmutableList(listOf())
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream_description, streamUiMock.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(description).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(true, isStreamPinned)
    }

    @Test
    fun testAdminBottomSheetOnRemoveFromCallButtonClick() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_remove_from_call)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals(true, isKickParticipantClicked)
    }

    @Test
    fun testAdminBottomSheetOnUnpinStreamClick() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId")))
        pinnedStreamsIds = ImmutableList(listOf("customStreamId"))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(false, isStreamPinned)
    }

    @Test
    fun testAdminBottomSheetOnPinStreamClick() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId")))
        pinnedStreamsIds = ImmutableList(listOf())
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(true, isStreamPinned)
    }

    @Test
    fun testAdminBottomSheetOnMuteStreamClick() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", audio = AudioUi("id", isMutedForYou = false))))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(true, isStreamMuted)
    }

    @Test
    fun testAdminBottomSheetOnUnMuteStreamClick() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock.copy(id = "customStreamId", audio = AudioUi("id", isMutedForYou = true))))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.onNodeWithText(text).performClick()
        Assert.assertEquals("customStreamId", clickedStreamId)
        Assert.assertEquals(false, isStreamMuted)
    }

    @Test
    fun adminBottomSheetClosedOnStreamPin() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        pinnedStreamsIds = ImmutableList()
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(text).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AdminBottomSheetTag).assertDoesNotExist()
    }

    @Test
    fun adminBottomSheetClosedOnStreamMute() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you, streamUiMock.userInfo!!.username)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(text).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AdminBottomSheetTag).assertDoesNotExist()
    }

    @Test
    fun adminBottomSheetClosednParticipantKicked() = runTest {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_remove_from_call)
        composeTestRule.performClickOnMoreButton()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(description).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AdminBottomSheetTag).assertDoesNotExist()
    }

    // TODO understand why this test do not pass on robolectric
//    @Test
//    fun adminBottomSheetClosedOnRootClick() {
//        amIAdmin = true
//        streams = ImmutableList(listOf(streamUiMock))
//        composeTestRule.performClickOnMoreButton()
//        composeTestRule.waitForIdle()
//        composeTestRule.onAllNodes(isRoot()).onLast().performClick()
//        composeTestRule.waitForIdle()
//        composeTestRule.onNodeWithTag(AdminBottomSheetTag).assertDoesNotExist()
//    }

    @Test
    fun adminBottomSheetClosedOnVerticalSwipe() {
        amIAdmin = true
        streams = ImmutableList(listOf(streamUiMock))
        composeTestRule.performClickOnMoreButton()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AdminBottomSheetTag).performVerticalSwipe(-1f)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(AdminBottomSheetTag).assertDoesNotExist()
    }

    @Test
    fun invitedParticipantsIsNotEmpty_invitedTitleIsDisplayed() {
        invited = ImmutableList(listOf("id1", "id2"))
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_users_invited)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun invitedParticipantsIsEmpty_invitedTitleDoesNotExists() {
        invited = ImmutableList()
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_users_invited)
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun testIsPinLimitReached_streamPinButtonIsDisabled() {
        streams = ImmutableList(listOf(streamUiMock.copy(video = VideoUi(id = "id", isEnabled = true))))
        isPinLimitReached = true
        val description = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream_description, streamUiMock.userInfo!!.username)
        composeTestRule
            .onNodeWithContentDescription(description)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    private fun AndroidComposeTestRule<ActivityScenarioRule<ComponentActivity>, ComponentActivity>.performClickOnMoreButton() {
        val description = activity.getString(R.string.kaleyra_participants_component_show_more_actions)
        onNodeWithContentDescription(description).performClick()
    }
}