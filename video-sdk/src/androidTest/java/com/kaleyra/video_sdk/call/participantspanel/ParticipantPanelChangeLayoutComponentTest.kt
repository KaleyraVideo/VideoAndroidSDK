@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.participantspanel

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participantspanel.model.StreamArrangement
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantsPanelChangeLayoutComponent
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantsPanelContent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParticipantPanelChangeLayoutComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testParticipantPanelShownWithNoOneInCallChangeLayoutComponentNotDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelContent(
                inCallStreamUi = ImmutableList(listOf()),
                invitedParticipants = ImmutableList(listOf("user")),
                isLoggedUserAdmin = false,
                adminUserId = "admin",
                onPinClicked = {},
                onGridClicked = {},
                onPin = { streamUi, pinned ->},
                onMute = { streamUi, muted ->},
                onClose = {},
                streamArrangement = StreamArrangement.Grid
            )
        }
        val changeLayoutLabelText = composeTestRule.activity.getString(R.string.kaleyra_change_layout)
        composeTestRule.onNodeWithText(changeLayoutLabelText).assertDoesNotExist()
    }

    @Test
    fun testParticipantPanelChangeLayoutComponentPinButtonDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelChangeLayoutComponent(
                streamArrangement = StreamArrangement.Grid,
                onGridClicked = {},
                onPinClicked = {},
            )
        }
        val pinButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_pin_arrangement)
        composeTestRule.onNodeWithContentDescription(pinButtonContentDescription).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelChangeLayoutComponentGridButtonDisplayed() {
        composeTestRule.setContent {
            ParticipantsPanelChangeLayoutComponent(
                streamArrangement = StreamArrangement.Grid,
                onGridClicked = {},
                onPinClicked = {},
            )
        }
        val pinButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_grid_arrangement)
        composeTestRule.onNodeWithContentDescription(pinButtonContentDescription).assertIsDisplayed()
    }

    @Test
    fun testParticipantPanelChangeLayoutComponentGridButtonClickedCallbackCalled() {
        var gridClicked = false
        composeTestRule.setContent {
            ParticipantsPanelChangeLayoutComponent(
                streamArrangement = StreamArrangement.Pin,
                onGridClicked = { gridClicked = true },
                onPinClicked = {},
            )
        }
        val gridButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_grid_arrangement)
        composeTestRule.onNodeWithContentDescription(gridButtonContentDescription).performClick()
        Assert.assertEquals(true, gridClicked)
    }

    @Test
    fun testParticipantPanelChangeLayoutComponentPinButtonClickedCallbackCalled() {
        var pinClicked = false
        composeTestRule.setContent {
            ParticipantsPanelChangeLayoutComponent(
                streamArrangement = StreamArrangement.Grid,
                onGridClicked = {},
                onPinClicked = { pinClicked = true },
            )
        }
        val pinButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_pin_arrangement)
        composeTestRule.onNodeWithContentDescription(pinButtonContentDescription).performClick()
        Assert.assertEquals(true, pinClicked)
    }

    @Test
    fun testParticipantPanelChangeLayoutComponentGridButtonAlreadyClickedCallbackNotCalled() {
        var gridClicked = false
        composeTestRule.setContent {
            ParticipantsPanelChangeLayoutComponent(
                streamArrangement = StreamArrangement.Grid,
                onGridClicked = { gridClicked = true },
                onPinClicked = {},
            )
        }
        val gridButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_grid_arrangement)
        composeTestRule.onNodeWithContentDescription(gridButtonContentDescription).performClick()
        Assert.assertEquals(false, gridClicked)
    }

    @Test
    fun testParticipantPanelChangeLayoutComponentPinButtonAlreadyClickedCallbackNotCalled() {
        var pinClicked = false
        composeTestRule.setContent {
            ParticipantsPanelChangeLayoutComponent(
                streamArrangement = StreamArrangement.Pin,
                onGridClicked = {},
                onPinClicked = { pinClicked = true },
            )
        }
        val pinButtonContentDescription = composeTestRule.activity.getString(R.string.kaleyra_pin_arrangement)
        composeTestRule.onNodeWithContentDescription(pinButtonContentDescription).performClick()
        Assert.assertEquals(false, pinClicked)
    }
}