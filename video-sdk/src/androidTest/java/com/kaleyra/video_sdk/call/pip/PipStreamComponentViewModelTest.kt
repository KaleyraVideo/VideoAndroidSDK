package com.kaleyra.video_sdk.call.pip

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.kaleyra.video_sdk.call.pip.view.PipStreamComponent
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class PipStreamComponentViewModelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    @Test
    fun setMaxStreamForPipMode() {
        composeTestRule.setContent {
            PipStreamComponent(
                viewModel = streamViewModel,
                onPipAspectRatio = { },
            )
        }

        verify(exactly = 1) {
            streamViewModel.setMaxMosaicStreams(2)
            streamViewModel.setMaxPinnedStreams(2)
            streamViewModel.setMaxThumbnailStreams(0)
        }
    }
}