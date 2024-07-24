package com.kaleyra.video_sdk.ui.call.pip

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.model.CallAppBarUiState
import com.kaleyra.video_sdk.call.appbar.viewmodel.CallAppBarViewModel
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.call.callinfo.viewmodel.CallInfoViewModel
import com.kaleyra.video_sdk.call.pip.PipScreen
import com.kaleyra.video_sdk.call.pip.view.PipStreamComponentTag
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PipScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        mockkObject(StreamViewModel)
        mockkObject(CallAppBarViewModel)
        mockkObject(CallInfoViewModel)

        every { StreamViewModel.provideFactory(any()) } returns mockk {
            every { create<StreamViewModel>(any(), any()) } returns mockk<StreamViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(StreamUiState())
            }
        }
        every { CallAppBarViewModel.provideFactory(any()) } returns mockk {
            every { create<CallAppBarViewModel>(any(), any()) } returns mockk<CallAppBarViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(CallAppBarUiState(recording = true))
            }
        }
        every { CallInfoViewModel.provideFactory(any()) } returns mockk {
            every { create<CallInfoViewModel>(any(), any()) } returns mockk<CallInfoViewModel>(relaxed = true) {
                every { uiState } returns MutableStateFlow(CallInfoUiState(callStateUi = CallStateUi.Disconnected.Ended, displayState = TextRef.StringResource(R.string.kaleyra_call_status_connecting)))
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testPipStreamComponentIsDisplayed() {
        composeTestRule.setContent { PipScreen() }
        composeTestRule.onNodeWithTag(PipStreamComponentTag).assertIsDisplayed()
    }

    @Test
    fun testCallInfoComponentIsDisplayed() {
        composeTestRule.setContent { PipScreen() }

        val title = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val subtitle = composeTestRule.activity.getString(R.string.kaleyra_call_status_connecting)
        // check the content description because it's a TextView
        composeTestRule.onNodeWithContentDescription(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(subtitle).assertIsDisplayed()
    }

    @Test
    fun testPipRecordingComponentIsDisplayed() {
        composeTestRule.setContent { PipScreen() }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_info_rec)
        composeTestRule.onNodeWithText(text, ignoreCase = true).assertIsDisplayed()
    }

}