package com.kaleyra.video_sdk.ui.call.streamnew

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.streamnew.MaxPinnedStreamsCompact
import com.kaleyra.video_sdk.call.streamnew.MaxPinnedStreamsExpanded
import com.kaleyra.video_sdk.call.streamnew.StreamComponent
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.model.core.VideoUi
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StreamComponentViewModelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    @Test
    fun userClicksStopScreenShare_tryStopScreenShareInvoked() {
        val stream1 = StreamUi(id = "id1", username = "username", video = VideoUi(id = "screenShare", isScreenShare = true), isMine = true)
        streamUiState.value = StreamUiState(
            streams = listOf(stream1).toImmutableList(),
            pinnedStreams = listOf(stream1).toImmutableList()
        )
        composeTestRule.setContent {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = currentWindowAdaptiveInfo(),
                onStreamClick = { },
                onMoreParticipantClick = { })
        }

        val text =  composeTestRule.activity.getString(R.string.kaleyra_stream_screenshare_action)
        composeTestRule
            .onNodeWithText(text)
            .assertHasClickAction()
            .performClick()
        verify(exactly = 1) { streamViewModel.tryStopScreenShare() }
    }

    @Test
    fun compactWindowSizeClass_setMaxPinnedStreamsWithMaxPinnedStreamsCompact() {
        val configuration = Configuration().apply {
            screenWidthDp = 300
            screenHeightDp = 480
        }
        composeTestRule.setContent {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = currentWindowAdaptiveInfo(configuration),
                onStreamClick = { },
                onMoreParticipantClick = { })
        }

        verify(exactly = 1) { streamViewModel.setMaxPinnedStreams(MaxPinnedStreamsCompact) }
    }

    @Test
    fun largeWindowSizeClass_setMaxPinnedStreamsWithMaxPinnedStreamsCompact() {
        val configuration = Configuration().apply {
            screenWidthDp = 800
            screenHeightDp = 480
        }
        composeTestRule.setContent {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = currentWindowAdaptiveInfo(configuration),
                onStreamClick = { },
                onMoreParticipantClick = { })
        }

        verify(exactly = 1) { streamViewModel.setMaxPinnedStreams(MaxPinnedStreamsExpanded) }
    }

}