package com.kaleyra.video_sdk.ui.call.callinfo

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.call.callinfo.view.CallInfo
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoSubtitleTestTag
import com.kaleyra.video_sdk.call.callinfo.view.CallInfoTitleTestTag
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallInfoTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var callInfoUiState by mutableStateOf(CallInfoUiState())

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallInfo(callInfoUiState = callInfoUiState)
        }
    }

    @After
    fun tearDown() {
        callInfoUiState = CallInfoUiState()
        unmockkAll()
    }

    @Test
    fun callInfo_titleShown() {
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertExists()
    }

    @Test
    fun callInfo_subtitleShown() {
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertExists()
    }

    @Test
    fun callUiStateConnecting_noDisplayNames_callDisplayStateTitleShown() {
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_connecting)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = listOf(),
            displayState = textRef,
            callStateUi = CallStateUi.Connecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateConnecting_withDisplayNames_titleAndSubtitlesShown() {
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_connecting)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = listOf("user1"),
            displayState = textRef,
            callStateUi = CallStateUi.Connecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("user1").assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateDialing_noDisplayNames_callDisplayStateTitleShown() {
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_dialing)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = listOf(),
            displayState = textRef,
            callStateUi = CallStateUi.Connecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateDialing_withDisplayNames_titleAndSubtitlesShown() {
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_dialing)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = listOf("user1"),
            displayState = textRef,
            callStateUi = CallStateUi.Connecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("user1").assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateRinging_noDisplayNames_callDisplayStateTitleShown() {
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_dialing)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = listOf(),
            displayState = textRef,
            callStateUi = CallStateUi.Connecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateRinging_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val textRef = TextRef.PluralResource(R.plurals.kaleyra_call_incoming_status_ringing, callee.size)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Ringing
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("user1").assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateRinging_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val textRef = TextRef.PluralResource(R.plurals.kaleyra_call_incoming_status_ringing, callee.size)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Ringing
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callee.joinToString(", ")).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateRingingRemotely_noDisplayNames_callDisplayStateTitleShown() {
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_ringing)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = listOf(),
            displayState = textRef,
            callStateUi = CallStateUi.RingingRemotely
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateRingingRemotely_withDisplayNames_callDisplayStateTitleShown() {
        val callee = listOf("user1", "user2")
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_ringing)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.RingingRemotely
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callee.joinToString(", ")).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateConnected_withNoDisplayName_noTitleAndSubtitleDisplayed() {
        callInfoUiState = CallInfoUiState(
            displayNames = listOf(),
            displayState = null,
            callStateUi = CallStateUi.Connected
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
    }

    @Test
    fun callUiStateConnected_withDisplayNames_noTitleAndSubtitleDisplayed() {
        callInfoUiState = CallInfoUiState(
            displayNames = listOf("user1", "user2"),
            displayState = null,
            callStateUi = CallStateUi.Connected
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
    }

    @Test
    fun callUiStateDisconnecting_withNoDisplayName_noTitleAndSubtitleDisplayed() {
        callInfoUiState = CallInfoUiState(
            displayNames = listOf(),
            displayState = null,
            callStateUi = CallStateUi.Disconnecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
    }

    @Test
    fun callUiStateDisconnecting_withDisplayNames_noTitleAndSubtitleDisplayed() {
        callInfoUiState = CallInfoUiState(
            displayNames = listOf("user1", "user2"),
            displayState = null,
            callStateUi = CallStateUi.Disconnecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
    }

    @Test
    fun callUiStateReconnecting_noDisplayNames_callDisplayStateTitleShown() {
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_ringing)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = listOf(),
            displayState = textRef,
            callStateUi = CallStateUi.Reconnecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateReconnecting_withDisplayNames_callDisplayStateTitleShown() {
        val callee = listOf("user1", "user2")
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_ringing)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Reconnecting
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateDeclined_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.PluralResource(R.plurals.kaleyra_call_status_declined, callee.size)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.Declined
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateDeclined_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.PluralResource(R.plurals.kaleyra_call_status_declined, callee.size)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.Declined
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateLineBusy_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_ended_line_busy)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.LineBusy
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateLineBusy_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_ended_line_busy)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.LineBusy
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateNoAnswer_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.PluralResource(R.plurals.kaleyra_call_status_no_answer, callee.size)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.Timeout
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateNoAnswer_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.PluralResource(R.plurals.kaleyra_call_status_no_answer, callee.size)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.Timeout
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateLineAnsweredOnAnotherDevice_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_answered_on_other_device)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateAnsweredOnAnotherDevice_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        val textRef = TextRef.StringResource(R.string.kaleyra_call_status_answered_on_other_device)
        val displayState = textRef.resolve(composeTestRule.activity)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = textRef,
            callStateUi = CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
        composeTestRule.onNodeWithText(displayState).assertIsDisplayed()
    }

    @Test
    fun callUiStateError_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Error
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateError_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Error
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateErrorServer_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Error.Server
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateErrorServer_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Error.Server
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateErrorUnknown_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Error.Unknown
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateErrorUnknown_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Error.Unknown
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateEnded_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateEnded_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateHungUp_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.HungUp
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateHungUp_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.HungUp
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateKicked_withOneDisplayName_titleAndSubtitlesShown() {
        val callee = listOf("user1")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Kicked("adminName")
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }

    @Test
    fun callUiStateKicked_withMoreDisplayNames_titleAndSubtitlesShown() {
        val callee = listOf("user1", "user2")
        val callEnded = composeTestRule.activity.getString(R.string.kaleyra_call_status_ended)
        callInfoUiState = CallInfoUiState(
            displayNames = callee,
            displayState = null,
            callStateUi = CallStateUi.Disconnected.Ended.Kicked("adminName")
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CallInfoTitleTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CallInfoSubtitleTestTag).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(callEnded).assertIsDisplayed()
    }
}