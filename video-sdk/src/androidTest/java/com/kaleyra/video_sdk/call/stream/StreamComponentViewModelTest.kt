package com.kaleyra.video_sdk.call.stream

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxMosaicStreamsCompact
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxMosaicStreamsExpanded
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxPinnedStreamsCompact
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxPinnedStreamsExpanded
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxThumbnailStreams
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class StreamComponentViewModelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    @Test
    fun userClicksStopScreenShare_tryStopScreenShareInvoked() {
        streamUiState.value = StreamUiState(isScreenShareActive = true)
        composeTestRule.setContent {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = currentWindowAdaptiveInfo(LocalConfiguration.current),
                onStreamItemClick = { },
                onMoreParticipantClick = { }
            )
        }

        val text =  composeTestRule.activity.getString(R.string.kaleyra_strings_action_stop_screen_share)
        composeTestRule
            .onNodeWithText(text)
            .assertHasClickAction()
            .performClick()
        verify(exactly = 1) { streamViewModel.tryStopScreenShare() }
    }

    @Test
    fun compactWindowSizeClass_setMaxStreamsCompact() {
        val configuration = Configuration().apply {
            screenWidthDp = 300
            screenHeightDp = 480
        }
        composeTestRule.setContent {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = currentWindowAdaptiveInfo(configuration),
                onStreamItemClick = { },
                onMoreParticipantClick = { }
            )
        }

        verify(exactly = 1) {
            streamViewModel.setStreamLayoutConstraints(MaxMosaicStreamsCompact, MaxPinnedStreamsCompact, MaxThumbnailStreams)
        }
    }

    @Test
    fun largeWindowSizeClass_setMaxStreamsExpanded() {
        val configuration = Configuration().apply {
            screenWidthDp = 800
            screenHeightDp = 480
        }
        composeTestRule.setContent {
            StreamComponent(
                viewModel = streamViewModel,
                windowSizeClass = currentWindowAdaptiveInfo(configuration),
                onStreamItemClick = { },
                onMoreParticipantClick = { }
            )
        }

        verify(exactly = 1) {
            streamViewModel.setStreamLayoutConstraints(MaxMosaicStreamsExpanded, MaxPinnedStreamsExpanded, MaxThumbnailStreams)
        }
    }

}