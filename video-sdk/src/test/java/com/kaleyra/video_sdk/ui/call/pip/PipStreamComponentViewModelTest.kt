package com.kaleyra.video_sdk.ui.call.pip

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PipStreamComponentViewModelTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    @Test
    fun testSetMaxStreamForPipMode() {
        composeTestRule.setContent {
            PipStreamComponent(
                viewModel = streamViewModel,
                onPipAspectRatio = { },
            )
        }

        verify(exactly = 1) {
            streamViewModel.setStreamLayoutConstraints(2, 2,0)
        }
    }

    @Test
    fun testSwitchToPipStreamLayout() {
        var displayPipComponent by mutableStateOf(true)
        composeTestRule.setContent {
            if (displayPipComponent) {
                PipStreamComponent(
                    viewModel = streamViewModel,
                    onPipAspectRatio = { },
                )
            }
        }

        verify(exactly = 1) { streamViewModel.switchToPipStreamLayout() }

        displayPipComponent = false
        composeTestRule.waitForIdle()

        verify(exactly = 1) { streamViewModel.switchToDefaultStreamLayout() }
    }
}