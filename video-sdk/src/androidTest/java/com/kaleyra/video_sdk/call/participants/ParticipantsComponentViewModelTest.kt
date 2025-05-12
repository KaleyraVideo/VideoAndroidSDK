package com.kaleyra.video_sdk.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.model.ParticipantsUiState
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.user.UserInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class ParticipantsComponentViewModelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val uiState = MutableStateFlow(ParticipantsUiState())

    private val viewModel = mockk<ParticipantsViewModel>(relaxed = true) {
        every { uiState } returns this@ParticipantsComponentViewModelTest.uiState
    }

    @Test
    fun userClicksMosaicLayout_switchToManualLayout() {
        composeTestRule.setContent {
            ParticipantsComponent(
                viewModel = viewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mosaic)
        composeTestRule.onNodeWithText(text).performClick()

        verify(exactly = 1) { viewModel.switchToManualLayout() }
    }

    @Test
    fun userClicksAutoLayout_switchToAutoLayout() {
        composeTestRule.setContent {
            ParticipantsComponent(
                viewModel = viewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_auto)
        composeTestRule.onNodeWithText(text).performClick()

        verify(exactly = 1) { viewModel.switchToAutoLayout() }
    }

    @Test
    fun userClicksMuteStream_muteStreamAudioIsInvoked() {
        val stream = StreamUi(id = "id1", userInfo = UserInfo("userId", "username1", ImmutableUri()), audio = AudioUi(id = "audioId", isMutedForYou = false))
        uiState.value = ParticipantsUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                viewModel = viewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mute_for_you_description, stream.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { viewModel.muteStreamAudio(stream.id) }
    }

    @Test
    fun userClicksUnmuteStream_muteStreamAudioIsInvoked() {
        val stream = StreamUi(id = "id1", userInfo = UserInfo("userId", "username1", ImmutableUri()), audio = AudioUi(id = "audioId", isMutedForYou = true))
        uiState.value = ParticipantsUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                viewModel = viewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unmute_for_you_description, stream.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { viewModel.muteStreamAudio(stream.id) }
    }

    @Test
    fun userClicksDisableMicOnLocalCameraStream_toggleMicInvoked() {
        val stream = StreamUi(id = CameraStreamConstants.CAMERA_STREAM_ID, userInfo = UserInfo("userId", "username1", ImmutableUri()), isMine = true, audio = AudioUi(id = "audioId", isEnabled = true))
        uiState.value = ParticipantsUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                viewModel = viewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_strings_info_disable_participant_microphone, stream.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { viewModel.toggleMic(any()) }
    }

    @Test
    fun userClicksPinStream_streamPinIsInvoked() {
        val stream = StreamUi(id = "id1", userInfo = UserInfo("userId", "username1", ImmutableUri()))
        uiState.value = ParticipantsUiState(
            streams = ImmutableList(listOf(stream))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                viewModel = viewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin_stream_description, stream.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { viewModel.pinStream(stream.id) }
    }

    @Test
    fun userClicksUnpinStream_streamUnpinIsInvoked() {
        val stream = StreamUi(id = "id1", userInfo = UserInfo("userId", "username1", ImmutableUri()))
        uiState.value = ParticipantsUiState(
            streams = ImmutableList(listOf(stream)),
            pinnedStreamIds = ImmutableList(listOf(stream.id))
        )
        composeTestRule.setContent {
            ParticipantsComponent(
                viewModel = viewModel,
                onDismiss = {}
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_unpin_stream_description, stream.userInfo!!.username)
        composeTestRule.onNodeWithContentDescription(text).performClick()

        verify(exactly = 1) { viewModel.unpinStream(stream.id) }
    }
}