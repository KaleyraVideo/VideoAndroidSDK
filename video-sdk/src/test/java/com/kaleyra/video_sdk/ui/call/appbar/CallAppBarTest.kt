package com.kaleyra.video_sdk.ui.call.appbar

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.CallAppBar
import com.kaleyra.video_sdk.call.appbar.RecordingDotTag
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var logo by mutableStateOf(Logo(light = Uri.EMPTY, dark = Uri.EMPTY))

    private var recording by mutableStateOf(false)

    private val title = "09:56"

    private val participantCount = 2

    private var isParticipantButtonClicked = false

    private var isBackPressed = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            CallAppBar(
                logo = logo,
                recording = recording,
                title = title,
                participantCount = participantCount,
                onParticipantClick = { isParticipantButtonClicked = true  },
                onBackPressed = { isBackPressed = true })
        }
    }

    @After
    fun tearDown() {
        logo = Logo(light = Uri.EMPTY, dark = Uri.EMPTY)
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
        val text = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
        val uri = Uri.parse("com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo")
        logo = Logo(uri, uri)
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
        composeTestRule.onNodeWithTag(RecordingDotTag).assertDoesNotExist()
    }

    @Test
    fun userClicksParticipantsButton_onParticipantClickInvoked() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_show_participants_descr)
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(text).performClick()
        assert(isParticipantButtonClicked)
    }
}