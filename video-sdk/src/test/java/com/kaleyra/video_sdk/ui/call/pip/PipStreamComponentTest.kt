package com.kaleyra.video_sdk.ui.call.pip

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
import com.kaleyra.video_sdk.call.stream.model.HiddenStreamUserPreview
import com.kaleyra.video_sdk.call.stream.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.StreamPreview
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.model.core.streamUiMock
import com.kaleyra.video_sdk.call.stream.view.items.HiddenStreamsItemTag
import com.kaleyra.video_sdk.call.stream.view.items.StreamItemTag
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamItemState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
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
    fun emptyStreamItems_noStreamItemIsDisplayed() {
        streamUiState = StreamUiState(streamItems = ImmutableList())
        composeTestRule.onNodeWithTag(StreamItemTag, useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag(HiddenStreamsItemTag, useUnmergedTree = true).assertDoesNotExist()
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
                StreamItem.HiddenStreams(users = listOf(HiddenStreamUserPreview("1", "user", null)))
            ).toImmutableList()
        )
        composeTestRule.onNodeWithTag(HiddenStreamsItemTag, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun multipleStream_rationalIsDefaultPipAspectRatio() {
        val streamItem1 = defaultStreamItem(username = "mario")
        val streamItem2 = defaultStreamItem(username = "alice")
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem1, streamItem2).toImmutableList()
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
//        val stream = defaultStreamItem(username = "mario", video = video)
//        streamUiState = StreamUiState(streams = listOf(stream).toImmutableList())
//        composeTestRule.waitForIdle()
//
//        assertEquals(Rational(5, 3), aspectRatio)
//    }

    @Test
    fun streamsAreExactlyAtMaxCapacity_moreParticipantItemDoesNotExists() {
        val streamItem1 = defaultStreamItem(username = "mario")
        val streamItem2 = defaultStreamItem(username = "alice")
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem1, streamItem2).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val otherText = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, 2)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithText("alice").assertIsDisplayed()
        composeTestRule.onNodeWithText(otherText).assertDoesNotExist()
    }

    @Test
    fun streamMicDisabled_micDisabledIconIsDisplayed() {
        val streamItem1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = false))
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertIsDisplayed()
    }

    @Test
    fun streamMicEnabled_micDisabledIconIsNotDisplayed() {
        val streamItem1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = true))
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertDoesNotExist()
    }

    @Test
    fun streamAudioNull_micDisabledIconDoesNotExists() {
        val streamItem1 = defaultStreamItem(username = "mario", audio = null)
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val micDisabledDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(micDisabledDescription).assertDoesNotExist()
    }

    @Test
    fun streamMutedForYou_mutedForYouIconIsDisplayed() {
        val streamItem1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = true, isMutedForYou = true))
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem1).toImmutableList()
        )

        composeTestRule.waitForIdle()

        val mutedForYouDescription = composeTestRule.activity.getString(R.string.kaleyra_stream_muted_for_you)
        composeTestRule.onNodeWithText("mario").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(mutedForYouDescription).assertIsDisplayed()
    }

    @Test
    fun remoteStreamMicDisabledAndMutedForYou_mutedForIconIsDisplayed() {
        val streamItem1 = defaultStreamItem(username = "mario", audio = AudioUi(id = "audio", isEnabled = false, isMutedForYou = true))
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem1).toImmutableList()
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
        val streamItem1 = defaultStreamItem(username = "mario")
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "previewUsername"),
            streamItems = listOf(streamItem1).toImmutableList()
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
    fun previewStreamAudioNull_micMutedDoesNotExists() {
        streamUiState = StreamUiState(
            preview = StreamPreview(audio = null)
        )
        composeTestRule.waitForIdle()

        val muted = composeTestRule.activity.getString(R.string.kaleyra_stream_mic_disabled)
        composeTestRule.onNodeWithContentDescription(muted).assertDoesNotExist()
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
    fun localStream_streamIsDisplayed() {
        val streamItem = defaultStreamItem(id = "id1", username = "username1")
        val localScreenShareStream = defaultStreamItem(id = "id2", username = "username2", mine = true)
        streamUiState = StreamUiState(
            streamItems = listOf(streamItem, localScreenShareStream).toImmutableList(),
        )

        val you = composeTestRule.activity.getString(R.string.kaleyra_stream_you)
        composeTestRule.onNodeWithText("username1").assertIsDisplayed()
        composeTestRule.onNodeWithText(you).assertIsDisplayed()
    }

    @Test
    fun previewStreamNotNull_streamIsNotDisplayed() {
        val streamItem1 = defaultStreamItem(username = "mario")
        streamUiState = StreamUiState(
            preview = StreamPreview(username = "previewUsername"),
            streamItems = listOf(streamItem1).toImmutableList()
        )
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("mario").assertDoesNotExist()
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
