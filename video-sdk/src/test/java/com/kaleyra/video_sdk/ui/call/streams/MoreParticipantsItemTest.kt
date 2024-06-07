package com.kaleyra.video_sdk.ui.call.streams

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.streamnew.view.items.MoreParticipantsItem
import com.kaleyra.video_sdk.call.streamnew.view.items.NonDisplayedParticipantData
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MoreParticipantsItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var nonDisplayedParticipantList by mutableStateOf(ImmutableList<NonDisplayedParticipantData>())

    @Before
    fun setUp() {
        composeTestRule.setContent {
            MoreParticipantsItem(nonDisplayedParticipantList = nonDisplayedParticipantList)
        }
    }

    @After
    fun tearDown() {
        nonDisplayedParticipantList = ImmutableList()
    }

    @Test
    fun testAvatarIsDisplayed() {
        nonDisplayedParticipantList = ImmutableList(
            listOf(NonDisplayedParticipantData("id1", "john", null))
        )
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
    }

    @Test
    fun testMaxThreeAvatarsAreDisplayed() {
        nonDisplayedParticipantList = ImmutableList(
            listOf(
                NonDisplayedParticipantData("id1", "john", null),
                NonDisplayedParticipantData("id2", "mary", null),
                NonDisplayedParticipantData("id3", "alice", null),
                NonDisplayedParticipantData("id4", "harry", null)
            )
        )
        composeTestRule.onNodeWithText("J").assertIsDisplayed()
        composeTestRule.onNodeWithText("M").assertIsDisplayed()
        composeTestRule.onNodeWithText("A").assertIsDisplayed()
        composeTestRule.onNodeWithText("H").assertDoesNotExist()
    }

    @Test
    fun testOthersCountIsDisplayed() {
        nonDisplayedParticipantList = ImmutableList(
            listOf(
                NonDisplayedParticipantData("id1", "john", null),
                NonDisplayedParticipantData("id2", "mary", null)
            )
        )
        val text = composeTestRule.activity.getString(R.string.kaleyra_stream_other_participants, nonDisplayedParticipantList.count())
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }
}
