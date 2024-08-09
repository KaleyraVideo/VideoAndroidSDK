package com.kaleyra.video_sdk.ui.call.bottomsheetnew.streammenu

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.view.streammenu.HStreamMenuContent
import com.kaleyra.video_sdk.call.bottomsheet.view.streammenu.VStreamMenuContent
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HStreamMenuContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val streamUiState = MutableStateFlow(StreamUiState())

    private val streamViewModel = mockk<StreamViewModel>(relaxed = true) {
        every { uiState } returns streamUiState
    }

    @Before
    fun setUp() {
        mockkObject(StreamViewModel)
        every { StreamViewModel.provideFactory(any()) } returns mockk {
            every { create<StreamViewModel>(any(), any()) } returns streamViewModel
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testExitFullscreen() {
        val stream = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(fullscreenStream = stream)

        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = stream.id,
                onDismiss = { },
                onFullscreen = { }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_off)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { streamViewModel.fullscreen(null) }
    }

    @Test
    fun testEnterFullscreen() {
        val stream = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(fullscreenStream = null)
        var fullscreen = false
        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = stream.id,
                onDismiss = { },
                onFullscreen = { fullscreen = true },
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_on)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { streamViewModel.fullscreen(stream.id) }
        assertEquals(true, fullscreen)
    }

    @Test
    fun fullScreeEntered_backPressed_menuDismissed() {
        val stream = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(fullscreenStream = null)
        var dismissed = false
        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = stream.id,
                onDismiss = { dismissed = true },
                onFullscreen = { }
            )
        }
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_on)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        composeTestRule.activity.onBackPressed()

        assertEquals(true, dismissed)
    }

    @Test
    fun testUnPin() {
        every { streamViewModel.maxPinnedStreams } returns 2

        val stream = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(pinnedStreams = listOf(stream).toImmutableList())

        var dismissed = false
        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = stream.id,
                onDismiss = { dismissed = true },
                onFullscreen = { }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { streamViewModel.unpin(stream.id) }
        assertEquals(true, dismissed)
    }

    @Test
    fun testPin() {
        every { streamViewModel.maxPinnedStreams } returns 2

        val stream = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState()

        var dismissed = false
        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = stream.id,
                onDismiss = { dismissed = true },
                onFullscreen = { }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertIsDisplayed()
            .performClick()

        verify(exactly = 1) { streamViewModel.pin(stream.id) }
        assertEquals(true, dismissed)
    }

    @Test
    fun testPinLimitReached_pinButtonIsNotEnabled() {
        every { streamViewModel.maxPinnedStreams } returns 1

        val stream = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(pinnedStreams = listOf(stream).toImmutableList())

        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = "id",
                onDismiss = { },
                onFullscreen = { }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun testPinLimitReached_unpinButtonIsEnabled() {
        every { streamViewModel.maxPinnedStreams } returns 1

        val stream = StreamUi(id = "streamId", username = "username")
        streamUiState.value = StreamUiState(pinnedStreams = listOf(stream).toImmutableList())

        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = stream.id,
                onDismiss = { },
                onFullscreen = { }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertHasClickAction()
            .assertIsEnabled()
    }

    @Test
    fun testCancel() {
        var dismissed = false
        composeTestRule.setContent {
            HStreamMenuContent(
                selectedStreamId = "streamId",
                onDismiss = { dismissed = true },
                onFullscreen = { }
            )
        }

        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_cancel)
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertIsDisplayed()
            .performClick()

        assertEquals(true, dismissed)
    }

    @Test
    fun testCancelActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_cancel)
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fullscreenFalse_fullscreenActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_on)
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun fullscreenFalse_minimizeActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_off)
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = true,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun pinFalse_pinActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun pinTrue_unpinActionIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = true,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testOnCancelClick() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_cancel)
        var clicked = false
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = { clicked = true },
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, clicked)
    }

    @Test
    fun testOnFullscreenClickFalse() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_on)
        var fullscreenClick: Boolean? = null
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = { fullscreenClick = it },
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(false, fullscreenClick)
    }

    @Test
    fun testOnFullscreenClickTrue() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_fullscreen_off)
        var fullscreenClick: Boolean? = null
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = true,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = { fullscreenClick = it },
                onPinClick = {}
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, fullscreenClick)
    }

    @Test
    fun testOnPinClickFalse() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        var pinClick: Boolean? = null
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = false,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = { pinClick = it }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(false, pinClick)
    }

    @Test
    fun testOnPinClickTrue() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        var pinClick: Boolean? = null
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = true,
                isPinLimitReached = false,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = { pinClick = it }
            )
        }
        composeTestRule.onNodeWithContentDescription(text).assertIsEnabled()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        Assert.assertEquals(true, pinClick)
    }

    @Test
    fun testPinLimitReached_pinActionIsNotEnabled() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_pin)
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = false,
                isPinLimitReached = true,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertHasClickAction()
            .assertIsNotEnabled()
    }

    @Test
    fun testPinLimitReached_unpinActionIsEnabled() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_unpin)
        composeTestRule.setContent {
            HStreamMenuContent(
                isFullscreen = false,
                isPinned = true,
                isPinLimitReached = true,
                onCancelClick = {},
                onFullscreenClick = {},
                onPinClick = {}
            )
        }
        composeTestRule
            .onNodeWithContentDescription(text)
            .assertHasClickAction()
            .assertIsEnabled()
    }
}