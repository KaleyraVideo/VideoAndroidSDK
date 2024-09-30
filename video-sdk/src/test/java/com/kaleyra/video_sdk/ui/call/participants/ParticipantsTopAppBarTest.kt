package com.kaleyra.video_sdk.ui.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.view.ParticipantsTopAppBar
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
class ParticipantsTopAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var embeddedComponent by mutableStateOf(false)

    private val participantsCount = 3

    private var isCloseClicked = false

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ParticipantsTopAppBar(
                participantsCount = participantsCount,
                onBackPressed = { isCloseClicked = true },
                embeddedComponent = embeddedComponent
            )
        }
    }

    @After
    fun tearDown() {
        isCloseClicked = false
    }

    @Test
    fun testParticipantCountIsDisplayed() {
        val text = composeTestRule.activity.resources.getQuantityString(R.plurals.kaleyra_participants_component_participants, participantsCount, participantsCount)
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testCloseIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(text).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(text).assertIsDisplayed()
    }

    @Test
    fun testOnCloseClick() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_feedback_close)
        composeTestRule.onNodeWithContentDescription(text).performClick()
        assertEquals(true, isCloseClicked)
    }

    @Test
    fun embeddedComponentUserClickClose_backPressedInvoked() {
        embeddedComponent = true
        val close = composeTestRule.activity.getString(R.string.kaleyra_close)
        composeTestRule.onNodeWithContentDescription(close).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(close).performClick()
        assertEquals(true, isCloseClicked)
    }
}