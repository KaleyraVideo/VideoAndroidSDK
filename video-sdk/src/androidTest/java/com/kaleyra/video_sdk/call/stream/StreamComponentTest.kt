package com.kaleyra.video_sdk.call.stream

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.model.MoreStreamsUserPreview
import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamPreview
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.items.MoreStreamsItemTag
import com.kaleyra.video_sdk.call.stream.view.items.StreamItemTag
import com.kaleyra.video_sdk.call.stream.model.StreamItemState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class StreamComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streamUiState by mutableStateOf(StreamUiState())

    private var selectedStreamId by mutableStateOf<String?>(null)

    private var moreParticipantClicked = false

    private var stopScreenShareClicked = false

    private var streamItemClicked: StreamItem.Stream? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamComponent(
                uiState = streamUiState,
                windowSizeClass = currentWindowAdaptiveInfo(),
                selectedStreamId = selectedStreamId,
                onStreamClick = { streamItemClicked = it },
                onStopScreenShareClick = { stopScreenShareClicked = true },
                onMoreParticipantClick = { moreParticipantClicked = true },
            )
        }
    }

    @After
    fun tearDown() {
        streamUiState = StreamUiState()
        selectedStreamId = null
        moreParticipantClicked = false
        stopScreenShareClicked = false
        streamItemClicked = null
    }

    @Test
    fun emptyStreamItems_noStreamItemIsDisplayed() {
        streamUiState = StreamUiState(streamItems = ImmutableList())
        composeTestRule.onNodeWithTag(StreamItemTag, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(MoreStreamsItemTag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun streamItem_streamItemIsDisplayed() {
        streamUiState = StreamUiState(
            streamItems = listOf(StreamItem.Stream("1", stream = streamUiMock)).toImmutableList()
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(StreamItemTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun hiddenStreamsItem_hiddenStreamsItemIsDisplayed() {
        streamUiState = StreamUiState(
            streamItems = listOf(
                StreamItem.MoreStreams(users = listOf(MoreStreamsUserPreview("1", "user", null)))
            ).toImmutableList()
        )
        composeTestRule.onNodeWithTag(MoreStreamsItemTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun isScreenShareActiveTrue_activeScreenShareIndicatorIsDisplayed() {
        streamUiState = StreamUiState(isScreenShareActive = true)

        val text =  composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_message)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun isScreenShareActiveFalse_activeScreenShareIndicatorDoesNotExists() {
        streamUiState = StreamUiState(isScreenShareActive = false)

        val text =  composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_message)
        composeTestRule.onNodeWithText(text).assertDoesNotExist()
    }

    @Test
    fun testOnHiddenStreamsClick() {
        val stream1 = defaultStreamItem(username = "mario")
        val stream2 = StreamItem.MoreStreams(
            users = listOf(
                MoreStreamsUserPreview("id1", "user1", null),
                MoreStreamsUserPreview("id2", "user2", null),
            )
        )
        streamUiState = StreamUiState(
            streamItems = listOf(stream1, stream2).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule
            .onNodeWithText(otherText)
            .assertHasClickAction()
            .performClick()
        assertEquals(true, moreParticipantClicked)
    }

    @Test
    fun testOnStopScreenShareClick() {
        streamUiState = StreamUiState(isScreenShareActive = true)

        val text =  composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_action)
        composeTestRule
            .onNodeWithText(text)
            .assertHasClickAction()
            .performClick()
        assertEquals(true, stopScreenShareClicked)
    }

    @Test
    fun testOnStreamClick() {
        val stream = defaultStreamItem(username = "luke")
        streamUiState = StreamUiState(streamItems = listOf(stream).toImmutableList())

        composeTestRule
            .onNodeWithText("luke")
            .assertHasClickAction()
            .performClick()
        assertEquals(stream, streamItemClicked)
    }

    @Test
    fun selectedStreamIsValid_nonSelectedStreamsAreDimmed() {
        val stream1 = defaultStreamItem()
        val stream2 = defaultStreamItem()
        val stream3 = defaultStreamItem()
        selectedStreamId = stream1.id
        streamUiState = StreamUiState(streamItems = listOf(stream1, stream2, stream3).toImmutableList())

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_dimmed)
        composeTestRule.onAllNodesWithContentDescription(text).assertCountEquals(2)
    }

    @Test
    fun streamMicDisabled_micDisabledIconIsDisplayed() {
        val stream1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = false))
        streamUiState = StreamUiState(
            streamItems = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertIsDisplayed()
    }

    @Test
    fun streamMicEnabled_micDisabledIconIsNotDisplayed() {
        val stream1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = true))
        streamUiState = StreamUiState(
            streamItems = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertDoesNotExist()
    }

    @Test
    fun streamAudioNull_micDisabledIconDoesNotExits() {
        val stream1 = defaultStreamItem(username = "mario", audio = null)
        streamUiState = StreamUiState(
            streamItems = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertDoesNotExist()
    }

    @Test
    fun streamMutedForYou_mutedForYouIconIsDisplayed() {
        val stream1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = true, isMutedForYou = true))
        streamUiState = StreamUiState(
            streamItems = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertIsDisplayed()
    }

    @Test
    fun streamMicDisabledAndMutedForYou_micMutedForYouIsDisplayed() {
        val stream1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = false, isMutedForYou = true))
        streamUiState = StreamUiState(
            streamItems = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertIsDisplayed()
    }

    @Test
    fun localStreamMicDisabledAndMutedForYou_micDisabledIsDisplayed() {
        val stream1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = false, isMutedForYou = true), mine = true)
        streamUiState = StreamUiState(
            streamItems = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val you = composeTestRule.activity.getString(R.string.kaleyra_stream_you)
        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText(you).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertDoesNotExist()
    }

    @Test
    fun localStream_youAsUsernameIsDisplayed() {
        val stream1 = defaultStreamItem(username = "mario", mine = true)
        streamUiState = StreamUiState(
            streamItems = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val you = composeTestRule.activity.getString(R.string.kaleyra_stream_you)
        composeTestRule.onNodeWithText(you).assertIsDisplayed()
    }

    @Test
    fun previewStreamNotNull_previewIsDisplayed() {
        val stream1 = defaultStreamItem(username = "mario")
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "previewUsername"),
            streamItems = listOf(stream1).toImmutableList()
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("previewUsername").assertDoesNotExist()
    }

    @Test
    fun previewStreamNotNull_streamIsNotDisplayed() {
        val stream1 = defaultStreamItem(username = "mario")
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "previewUsername"),
            streamItems = listOf(stream1).toImmutableList()
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("mario").assertDoesNotExist()
    }

    @Test
    fun previewIsGroupCallTrue_avatarIsNotDisplayed() {
        streamUiState = StreamUiState(
            preview = StreamPreview(isGroupCall = true, username = "mario"),
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("M").assertDoesNotExist()
    }

    @Test
    fun previewIsStartingWithVideoTrueAndVideoIsNull_avatarIsNotDisplayed() {
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "mario", video = null, isStartingWithVideo = true),
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("M").assertDoesNotExist()
    }

    @Test
    fun previewIsStartingWithVideoFalse_avatarIsDisplayed() {
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "mario", isStartingWithVideo = false),
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("M").assertIsDisplayed()
    }

    @Test
    fun previewIsStartingWithVideoTrueAndVideoIsNotNull_videoIsDisplayed() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.runOnMainSync {
            streamUiState = StreamUiState(
                preview = StreamPreview(
                    username = "mario",
                    video = VideoUi(
                        id = "videoId",
                        view = ImmutableView(VideoStreamView(instrumentation.context))
                    ),
                    isStartingWithVideo = true
                )
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("M").assertIsDisplayed()
    }

    private fun defaultStreamItem(
        id: String = UUID.randomUUID().toString(),
        username: String = "username",
        mine: Boolean = false,
        audio: AudioUi? = null,
        video: VideoUi? = null,
        avatar: ImmutableUri? = null,
        streamItemState: StreamItemState = StreamItemState.Standard
    ): StreamItem {
        return StreamItem.Stream(
            id = id,
            stream = StreamUi(
                id = id,
                username = username,
                isMine = mine,
                audio = audio,
                video = video,
                avatar = avatar
            ),
            state = streamItemState
        )
    }

}