package com.kaleyra.video_sdk.ui.call.stream

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
import androidx.test.platform.app.InstrumentationRegistry
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.StreamComponent
import com.kaleyra.video_sdk.call.stream.model.StreamPreview
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class StreamComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streamUiState by mutableStateOf(StreamUiState())

    private var selectedStreamId by mutableStateOf<String?>(null)

    private var maxFeaturedStreams by mutableStateOf(Int.MAX_VALUE)

    private var moreParticipantClicked = false

    private var stopScreenShareClicked = false

    private var streamClicked: StreamUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamComponent(
                uiState = streamUiState,
                windowSizeClass = currentWindowAdaptiveInfo(),
                selectedStreamId = selectedStreamId,
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
        selectedStreamId = null
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
    fun selectedStreamIsValid_nonSelectedStreamsAreDimmed() {
        val stream1 = defaultStreamUi()
        val stream2 = defaultStreamUi()
        val stream3 = defaultStreamUi()
        selectedStreamId = stream1.id
        streamUiState = StreamUiState(streams = listOf(stream1, stream2, stream3).toImmutableList())

        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_dimmed)
        composeTestRule.onAllNodesWithContentDescription(text).assertCountEquals(2)
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
    fun streamMicDisabledAndMutedForYou_micMutedForYouIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = AudioUi(id = "audio", isEnabled = false, isMutedForYou = true))
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertIsDisplayed()
    }

    @Test
    fun localStreamMicDisabledAndMutedForYou_micDisabledIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = AudioUi(id = "audio", isEnabled = false, isMutedForYou = true), mine = true)
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
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
        val stream1 = defaultStreamUi(username = "mario", mine = true)
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val you = composeTestRule.activity.getString(R.string.kaleyra_stream_you)
        composeTestRule.onNodeWithText(you).assertIsDisplayed()
    }

    @Test
    fun previewStreamNotNull_previewIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "previewUsername"),
            streams = listOf(stream1).toImmutableList()
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("previewUsername").assertDoesNotExist()
    }

    @Test
    fun previewStreamNotNull_streamIsNotDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "previewUsername"),
            streams = listOf(stream1).toImmutableList()
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

    fun participantItemIsAddedAfterwards_onClickBehaviourIsSetCorrectly() {
        val stream1 = defaultStreamUi()
        val stream2 = defaultStreamUi()
        val stream3 = defaultStreamUi()
        streamUiState = StreamUiState(streams = listOf(stream1, stream2, stream3).toImmutableList())
        composeTestRule.waitForIdle()

        maxFeaturedStreams = 2
        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText(otherText).performClick()

        assertEquals(true, moreParticipantClicked)
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