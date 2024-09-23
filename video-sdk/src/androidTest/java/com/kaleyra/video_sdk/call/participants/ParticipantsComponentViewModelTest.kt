package com.kaleyra.video_sdk.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class ParticipantsComponentViewModelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val participantsUiState = MutableStateFlow(ParticipantsUiState())

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val participantsViewModel = mockk<ParticipantsViewModel>(relaxed = true) {
        every { uiState } returns participantsUiState
    }

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    @Test
    fun userClicksGridLayout_pinnedStreamsAreCleaned() {
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_grid)
        composeTestRule.onNodeWithText(text).performClick()

        verify(exactly = 1) { streamViewModel.unpinAll() }
    }

    @Test
    fun userClicksPinLayout_firstNotMineStreamIsPinned() {
        val stream1 = StreamUi(id = "id1", username = "username1", isMine = true)
        val stream2 = StreamUi(id = "id2", username = "username2")
        val stream3 = StreamUi(id = "id3", username = "username3")
        streamUiState.value = StreamUiState(
            streams = ImmutableList(listOf(stream1, stream2, stream3))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(text).performClick()

        verify(exactly = 1) { streamViewModel.pin(stream2.id) }
    }

    @Test
    fun localScreenEnabled_gridLayoutButtonIsDisabled() {
        val stream = StreamUi(id = "id1", username = "username", isMine = true, video = VideoUi(id = "id", isScreenShare = true))
        streamUiState.value = StreamUiState(
            streams = ImmutableList(listOf(stream)),
            pinnedStreams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_grid)
        composeTestRule.onNodeWithText(text).assertHasClickAction().assertIsNotEnabled()
    }

    @Test
    fun userClicksMuteStream_muteStreamAudioIsInvoked() {
        val stream = StreamUi(id = "id1", username = "username1", audio = AudioUi(id = "audioId", isMutedForYou = false))
        streamUiState.value = StreamUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you_description, stream.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { participantsViewModel.muteStreamAudio(stream.id) }
    }

    @Test
    fun userClicksUnmuteStream_muteStreamAudioIsInvoked() {
        val stream = StreamUi(id = "id1", username = "username1", audio = AudioUi(id = "audioId", isMutedForYou = true))
        streamUiState.value = StreamUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you_description, stream.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { participantsViewModel.muteStreamAudio(stream.id) }
    }

    @Test
    fun userClicksDisableMicOnLocalCameraStream_toggleMicInvoked() {
        val stream = StreamUi(id = CameraStreamConstants.CAMERA_STREAM_ID, username = "username1", isMine = true, audio = AudioUi(id = "audioId", isEnabled = true))
        streamUiState.value = StreamUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_disable_microphone_description, stream.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { participantsViewModel.toggleMic(any()) }
    }

    @Test
    fun userClicksPinStream_streamPinIsInvoked() {
        every { streamViewModel.maxPinnedStreams } returns 2

        val stream = StreamUi(id = "id1", username = "username1")
        streamUiState.value = StreamUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream_description, stream.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { streamViewModel.pin(stream.id) }
    }

    @Test
    fun userClicksUnpinStream_streamUnpinIsInvoked() {
        val stream = StreamUi(id = "id1", username = "username1")
        streamUiState.value = StreamUiState(
            streams = ImmutableList(listOf(stream)),
            pinnedStreams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream_description, stream.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { streamViewModel.unpin(stream.id) }
    }


    @Test
    fun testPinLimitReached_pinButtonIsNotEnabled() {
        every { streamViewModel.maxPinnedStreams } returns 1

        val stream1 = StreamUi(id = "streamId1", username = "username")
        val stream2 = StreamUi(id = "streamId2", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(stream1, stream2).toImmutableList(),
            pinnedStreams = listOf(stream2).toImmutableList()
        )

        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream_description, stream1.username)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun testPinLimitReached_unpinButtonIsEnabled() {
        every { streamViewModel.maxPinnedStreams } returns 1

        val stream1 = StreamUi(id = "streamId1", username = "username")
        val stream2 = StreamUi(id = "streamId2", username = "username")
        streamUiState.value = StreamUiState(
            streams = listOf(stream1, stream2).toImmutableList(),
            pinnedStreams = listOf(stream2).toImmutableList()
        )

        composeTestRule.setContent {
            ParticipantsComponent(
                participantsViewModel = participantsViewModel,
                streamViewModel = streamViewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream_description, stream2.username)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertHasClickAction()
            .assertIsEnabled()
    }
}