package com.kaleyra.video_sdk.call.appbar

import android.net.Uri
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
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.CallAppBarComponent
import com.kaleyra.video_sdk.call.appbar.view.RecordingDotTag
import com.kaleyra.video_sdk.call.appbar.model.recording.RecordingStateUi
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CallAppBarComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var appIcon by mutableStateOf(Uri.EMPTY)

    private var recording by mutableStateOf(false)

    private var recordingStateUi by mutableStateOf(RecordingStateUi.Stopped)

    private var callStateUi: CallStateUi = CallStateUi.Connected

    private var title = "09:56"

    private val participantCount = 2

    private var isParticipantButtonClicked = false

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallAppBarComponent(
                appIcon = appIcon,
                title = title,
                automaticRecording = recording,
                recordingStateUi = recordingStateUi,
                participantCount = participantCount,
                callStateUi = callStateUi,
                onParticipantClick = { isParticipantButtonClicked = true  },
                onBackPressed = { isBackPressed = true })
        }
    }

    @After
    fun tearDown() {
        appIcon = Uri.EMPTY
        recording = false
        isBackPressed = false
        isParticipantButtonClicked = false
    }

    @Test
    fun userClicksBack_onBackPressedInvoked() {
        val close = composeTestRule.activity.getString(R.string.kaleyra_back)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assert(isBackPressed)
    }

    @Test
    fun testLogoIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_app_icon)
        appIcon = Uri.parse("https://www.example.com/image.png")
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun titleTextIsDisplayed() {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun participantCountIsDisplayed() {
        composeTestRule.onNodeWithText("$participantCount").assertIsDisplayed()
    }

    @Test
    fun recordingTrue_recordingDotIsDisplayed() {
        recording = true
        composeTestRule.onNodeWithTag(RecordingDotTag).assertIsDisplayed()
    }

    @Test
    fun recordingFalse_recordingDotDoesNotExists() {
        recording = false
        composeTestRule.onNodeWithTag(RecordingDotTag).assertIsNotDisplayed()
    }

    @Test
    fun callDisconnecting_recordingAutomatic_recordingNotDisplayed() {
        recording = false
        callStateUi = CallStateUi.Disconnecting
        composeTestRule.onNodeWithTag(RecordingDotTag).assertIsNotDisplayed()
    }

    @Test
    fun callDisconnected_recordingAutomatic_recordingNotDisplayed() {
        recording = false
        callStateUi = CallStateUi.Disconnected
        composeTestRule.onNodeWithTag(RecordingDotTag).assertIsNotDisplayed()
    }

    @Test
    fun callDisconnecting_recordingAutomatic_titleNotDisplayed() {
        recording = false
        callStateUi = CallStateUi.Disconnecting
        composeTestRule.onNodeWithTag(RecordingDotTag).assertIsNotDisplayed()
    }

    @Test
    fun callDisconnectingtitleWithCallTimer_titleNotDisplayed() {
        recording = false
        callStateUi = CallStateUi.Disconnecting
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun callDisconnected_recordingAutomatic_titleNotDisplayed() {
        recording = false
        callStateUi = CallStateUi.Disconnected
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun automaticRecording_callConnecting_RECDisplayed() {
        recording = true
        callStateUi = CallStateUi.Connecting
        val rec = composeTestRule.activity.getString(R.string.kaleyra_rec)
        composeTestRule.onNodeWithText(rec).assertIsDisplayed()
    }

    @Test
    fun automaticRecording_recordingStarted_titleShown() {
        recording = true
        callStateUi = CallStateUi.Connected
        recordingStateUi = RecordingStateUi.Started
        title = "test"
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun manualRecording_recordingStarted_redDotDisplayed() {
        recording = false
        callStateUi = CallStateUi.Connected
        recordingStateUi = RecordingStateUi.Started
        composeTestRule.onNodeWithTag(RecordingDotTag).assertIsDisplayed()
    }

    @Test
    fun manualRecording_recordingStopped_redDotNotDisplayed() {
        recording = false
        callStateUi = CallStateUi.Connected
        recordingStateUi = RecordingStateUi.Stopped
        composeTestRule.onNodeWithTag(RecordingDotTag).assertIsNotDisplayed()
    }

    @Test
    fun userClicksParticipantsButton_onParticipantClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_show_participants_descr)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        assert(isParticipantButtonClicked)
    }
}
