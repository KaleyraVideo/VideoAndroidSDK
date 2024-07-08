package com.kaleyra.video_sdk.call.streamnew

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.model.core.AudioUi
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
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

    private var highlightedStreamId by mutableStateOf<String?>(null)

    private var maxFeaturedStreams by mutableStateOf(Int.MAX_VALUE)

    private var moreParticipantClicked = false

    private var stopScreenShareClicked = false

    private var streamClicked: StreamUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamComponent(
                streamUiState = streamUiState,
                windowSizeClass = currentWindowAdaptiveInfo(),
                highlightedStreamId = highlightedStreamId,
                onStreamClick = { streamClicked = it },
                onStopScreenShareClick = { stopScreenShareClicked = true },
                onMoreParticipantClick = { moreParticipantClicked = true },
                maxFeaturedStreams = maxFeaturedStreams
            )
        }
    }

    @After
    fun tearDown() {
        streamUiState = StreamUiState()
        highlightedStreamId = null
        maxFeaturedStreams = Int.MAX_VALUE
        moreParticipantClicked = false
        stopScreenShareClicked = false
        streamClicked = null
    }

    @Test
    fun testStreamsAreDisplayedUpToMaxFeaturedStreams() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3).toImmutableList()
        )
        maxFeaturedStreams = 2
        composeTestRule.onNodeWithTag(stream1.id).assertIsDisplayed()
        composeTestRule.onNodeWithTag(stream2.id).assertIsDisplayed()
        composeTestRule.onNodeWithTag(stream3.id).assertDoesNotExist()
    }

    @Test
    fun maxFeaturedStreamsIsExceed_moreParticipantItemIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3).toImmutableList()
        )
        maxFeaturedStreams = 2

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText(otherText).assertIsDisplayed()
    }

    @Test
    fun maxThumbnailStreamsIsExceed_moreParticipantItemIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        val stream4 = defaultStreamUi(username = "francesca")
        val stream5 = defaultStreamUi(username = "luke")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3, stream4, stream5).toImmutableList(),
            pinnedStreams = listOf(stream1).toImmutableList(),
        )

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("alice").assertIsDisplayed()
        composeTestRule.onNodeWithText("john").assertIsDisplayed()
        composeTestRule.onNodeWithText("F").assertIsDisplayed()
        composeTestRule.onNodeWithText("L").assertIsDisplayed()
        composeTestRule.onNodeWithText(otherText).assertIsDisplayed()
    }


    @Test
    fun fullscreenIsSet_fullscreenStreamIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3).toImmutableList(),
            fullscreenStream = stream2
        )

        val fullscreenDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_fullscreen)
        composeTestRule.onNodeWithText("mario").assertDoesNotExist()
        composeTestRule.onNodeWithText("john").assertDoesNotExist()
        composeTestRule.onNodeWithText("alice").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(fullscreenDescription).assertIsDisplayed()
    }

    @Test
    fun pinnedStreams_onlyPinnedAndThumbnailStreamsAreDisplayed() {
        val stream1 = defaultStreamUi(id = "id1")
        val stream2 = defaultStreamUi(id = "id2")
        val stream3 = defaultStreamUi(id = "id3")
        val stream4 = defaultStreamUi(id = "id4")
        val stream5 = defaultStreamUi(id = "id5")
        val stream6 = defaultStreamUi(id = "id6")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3, stream4, stream5, stream6).toImmutableList(),
            pinnedStreams = listOf(stream1, stream2).toImmutableList()
        )

        val pinText = composeTestRule.activity.getString(R.string.kaleyra_stream_pin)
        // Check pinned streams
        composeTestRule.onAllNodesWithContentDescription(pinText).assertCountEquals(2)
        composeTestRule.onAllNodesWithContentDescription(pinText)[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithContentDescription(pinText)[1].assertIsDisplayed()
        // Check thumbnail streams
        composeTestRule.onNodeWithTag(stream3.id).assertIsDisplayed()
        composeTestRule.onNodeWithTag(stream4.id).assertIsDisplayed()
        composeTestRule.onNodeWithTag(stream5.id).assertIsDisplayed()
        composeTestRule.onNodeWithTag(stream6.id).assertDoesNotExist()
    }

    @Test
    fun localScreenShareStream_screenShareItemIsDisplayed() {
        val stream1 = defaultStreamUi(id = "id1", video = VideoUi(id = "screenShare", isScreenShare = true), mine = true)
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList(),
            pinnedStreams = listOf(stream1).toImmutableList()
        )

        val text =  composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_message)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(stream1.id)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun localScreenShareStream_screenShareItemIsNotClickable() {
        val stream1 = defaultStreamUi(id = "id1", video = VideoUi(id = "screenShare", isScreenShare = true), mine = true)
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList(),
            pinnedStreams = listOf(stream1).toImmutableList()
        )

        composeTestRule
            .onNodeWithTag(stream1.id)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun testOnMoreParticipantClick() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3).toImmutableList()
        )
        maxFeaturedStreams = 2

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
        val stream1 = defaultStreamUi(id = "id1", video = VideoUi(id = "screenShare", isScreenShare = true), mine = true)
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList(),
            pinnedStreams = listOf(stream1).toImmutableList()
        )

        val text =  composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_action)
        composeTestRule
            .onNodeWithText(text)
            .assertHasClickAction()
            .performClick()
        assertEquals(true, stopScreenShareClicked)
    }

    @Test
    fun testOnStreamClick() {
        val stream = defaultStreamUi(username = "luke")
        streamUiState = StreamUiState(streams = listOf(stream).toImmutableList())

        composeTestRule
            .onNodeWithText("luke")
            .assertHasClickAction()
            .performClick()
        assertEquals(stream, streamClicked)
    }

    @Test
    fun highlightedStreamIsValid_highlightedStreamIsDisplayed() {
        val stream = defaultStreamUi()
        highlightedStreamId = stream.id
        streamUiState = StreamUiState(streams = listOf(stream).toImmutableList())

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_selected)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun fullscreenStream_moreParticipantItemDoesNotExists() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "luke")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3).toImmutableList(),
            fullscreenStream = stream1
        )

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 3)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertDoesNotExist()
        composeTestRule.onNodeWithText("L").assertDoesNotExist()
        composeTestRule.onNodeWithText(otherText).assertDoesNotExist()
    }

    @Test
    fun streamsAreExactlyAtMaxCapacity_moreParticipantItemDoesNotExists() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2).toImmutableList()
        )
        maxFeaturedStreams = 2

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("alice").assertIsDisplayed()
        composeTestRule.onNodeWithText(otherText).assertDoesNotExist()
    }

    @Test
    fun streamMicDisabled_micDisabledIconIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = AudioUi(id = "audio", isEnabled = false))
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertIsDisplayed()
    }

    @Test
    fun streamMicEnabled_micDisabledIconIsNotDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = AudioUi(id = "audio", isEnabled = true))
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertDoesNotExist()
    }

    @Test
    fun streamAudioNull_micDisabledIconIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = null)
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertIsDisplayed()
    }

    @Test
    fun streamMutedForYou_mutedForYouIconIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = AudioUi(id = "audio", isEnabled = true, isMutedForYou = true))
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertIsDisplayed()
    }

    @Test
    fun streamMicDisabledAndMutedForYou_micDisabledIconIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = AudioUi(id = "audio", isEnabled = false, isMutedForYou = true))
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertDoesNotExist()
    }

    @Test
    fun localStream_youAsUsernameIsDisplayed() {

    }

    fun defaultStreamUi(
        id: String = UUID.randomUUID().toString(),
        username: String = "username",
        mine: Boolean = false,
        audio: AudioUi? = null,
        video: VideoUi? = null,
        avatar: ImmutableUri? = null
    ): StreamUi {
        return StreamUi(
            id = id,
            username = username,
            isMine = mine,
            audio = audio,
            video = video,
            avatar = avatar
        )
    }

}