package com.kaleyra.video_sdk.call.pip

import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.pip.view.DefaultPipAspectRatio
import com.kaleyra.video_sdk.call.pip.view.PipStreamComponent
import com.kaleyra.video_sdk.call.stream.model.StreamPreview
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class PipStreamComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streamUiState by mutableStateOf(StreamUiState())

    private var aspectRatio: Rational = Rational(1, 1)

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PipStreamComponent(
                uiState = streamUiState,
                onPipAspectRatio = { aspectRatio = it }
            )
        }
    }

    @After
    fun tearDown() {
        streamUiState = StreamUiState()
    }

    @Test
    fun multipleStream_rationalIsDefaultPipAspectRatio() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2).toImmutableList()
        )

        assertEquals(DefaultPipAspectRatio, aspectRatio)
    }

//    No way found to run this test on instrumented test
//    @Test
//    fun singleStream_rationalIsVideoStreamViewAspectRatio() {
//        val view = spyk(VideoStreamView(composeTestRule.activity)) {
//            every { videoSize } returns MutableStateFlow(Size(500, 300))
//        }
//
//        val video = VideoUi(id = "videoId", view = ImmutableView(view))
//        val stream = defaultStreamUi(username = "mario", video = video)
//        streamUiState = StreamUiState(streams = listOf(stream).toImmutableList())
//        composeTestRule.waitForIdle()
//
//        assertEquals(Rational(5, 3), aspectRatio)
//    }

    @Test
    fun testUpToTwoStreamsAreDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3).toImmutableList()
        )
        composeTestRule.onNodeWithTag(stream1.id).assertIsDisplayed()
        composeTestRule.onNodeWithTag(stream2.id).assertIsDisplayed()
        composeTestRule.onNodeWithTag(stream3.id).assertDoesNotExist()
    }

    @Test
    fun moreThanTwoStreams_moreParticipantItemIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText(otherText).assertIsDisplayed()
    }

    @Test
    fun moreParticipantsIsDisplayed_localParticipantIsNotCounted() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        val stream3 = defaultStreamUi(username = "john")
        val stream4 = defaultStreamUi(username = "lara", mine = true)
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3, stream4).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText(otherText).assertIsDisplayed()
    }

    @Test
    fun localStreamIsNotDisplayed() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice", mine = true)
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2).toImmutableList()
        )

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertDoesNotExist()
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

        composeTestRule.onNodeWithText("mario").assertDoesNotExist()
        composeTestRule.onNodeWithText("john").assertDoesNotExist()
        composeTestRule.onNodeWithText("alice").assertIsDisplayed()
    }

    @Test
    fun pinnedStreams_onlyPinnedAreDisplayed() {
        val stream1 = defaultStreamUi(id = "id1", username = "username1")
        val stream2 = defaultStreamUi(id = "id2", username = "username2")
        val stream3 = defaultStreamUi(id = "id3", username = "username3")
        val stream4 = defaultStreamUi(id = "id4", username = "username4")
        val stream5 = defaultStreamUi(id = "id5", username = "username5")
        val stream6 = defaultStreamUi(id = "id6", username = "username6")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3, stream4, stream5, stream6).toImmutableList(),
            pinnedStreams = listOf(stream3, stream4).toImmutableList()
        )

        composeTestRule.onNodeWithText(stream3.username).assertIsDisplayed()
        composeTestRule.onNodeWithText(stream4.username).assertIsDisplayed()
    }

    @Test
    fun pinnedStreams_upToTwoPinnedAreDisplayed() {
        val stream1 = defaultStreamUi(id = "id1", username = "username1")
        val stream2 = defaultStreamUi(id = "id2", username = "username2")
        val stream3 = defaultStreamUi(id = "id3", username = "username3")
        val stream4 = defaultStreamUi(id = "id4", username = "username4")
        val stream5 = defaultStreamUi(id = "id5", username = "username5")
        val stream6 = defaultStreamUi(id = "id6", username = "username6")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2, stream3, stream4, stream5, stream6).toImmutableList(),
            pinnedStreams = listOf(stream3, stream4, stream5).toImmutableList()
        )

        composeTestRule.onNodeWithText(stream3.username).assertIsDisplayed()
        composeTestRule.onNodeWithText(stream4.username).assertIsDisplayed()
        composeTestRule.onNodeWithText(stream5.username).assertDoesNotExist()
    }

    @Test
    fun streamsAreExactlyAtMaxCapacity_moreParticipantItemDoesNotExists() {
        val stream1 = defaultStreamUi(username = "mario")
        val stream2 = defaultStreamUi(username = "alice")
        streamUiState = StreamUiState(
            streams = listOf(stream1, stream2).toImmutableList()
        )

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
    fun remoteStreamMicDisabledAndMutedForYou_mutedForIconIsDisplayed() {
        val stream1 = defaultStreamUi(username = "mario", audio = AudioUi(id = "audio", isEnabled = false, isMutedForYou = true))
        streamUiState = StreamUiState(
            streams = listOf(stream1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertIsDisplayed()
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
    fun previewStreamAudioMuted_micMutedIsDisplayed() {
        streamUiState = StreamUiState(
            preview = StreamPreview(audio = AudioUi(id = "audioId", isEnabled = false))
        )
        composeTestRule.waitForIdle()

        val muted = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(muted).assertIsDisplayed()
    }

    @Test
    fun previewStreamAudioNull_micMutedIsDisplayed() {
        streamUiState = StreamUiState(
            preview = StreamPreview(audio = null)
        )
        composeTestRule.waitForIdle()

        val muted = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(muted).assertIsDisplayed()
    }

    @Test
    fun previewStreamAudioEnabled_micMutedDoesNotExist() {
        streamUiState = StreamUiState(
            preview = StreamPreview(audio = AudioUi(id = "audioId", isEnabled = true))
        )
        composeTestRule.waitForIdle()

        val muted = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(muted).assertDoesNotExist()
    }

    @Test
    fun localScreenSharePinnedStream_streamIsNotDisplayed() {
        val stream = defaultStreamUi(id = "id1", username = "username1")
        val localScreenShareStream = defaultStreamUi(id = "id2", username = "username2", mine = true, video = VideoUi(id = "id", isScreenShare = true))
        streamUiState = StreamUiState(
            streams = listOf(stream, localScreenShareStream).toImmutableList(),
            pinnedStreams = listOf(stream, localScreenShareStream).toImmutableList()
        )

        val you = composeTestRule.activity.getString(R.string.kaleyra_stream_you)
        composeTestRule.onNodeWithText(stream.username).assertIsDisplayed()
        composeTestRule.onNodeWithText(you).assertDoesNotExist()
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

    private fun defaultStreamUi(
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