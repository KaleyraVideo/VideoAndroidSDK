package com.kaleyra.video_sdk.ui.call.bottomsheetnew.sheetactions

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.VSheetActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VSheetActionsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showAnswerActionTrue_answerActionIsDisplayed() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            VSheetActions(
                actions = ImmutableList(),
                showAnswerAction = true,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(answerDescription).assertIsDisplayed()
    }

    @Test
    fun showAnswerActionFalse_answerActionIsNotDisplayed() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            VSheetActions(
                actions = ImmutableList(),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(answerDescription).assertDoesNotExist()
    }

    @Test
    fun testOnAnswerActionClick() {
        var isAnswerClicked = false
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            VSheetActions(
                actions = ImmutableList(),
                showAnswerAction = true,
                onAnswerActionClick = { isAnswerClicked = true  },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(answerDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(answerDescription).performClick()
        assertEquals(true, isAnswerClicked)
    }

    @Test
    fun testOnMoreActionClick() {
        var isMoreClicked = false
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            VSheetActions(
                modifier = Modifier.height(100.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = { isMoreClicked = true },
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(moreDescription).performClick()
        assertEquals(true, isMoreClicked)
    }

    @Test
    fun onlySomeActionsCanBeDisplayed_moreActionIsDisplayed() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            VSheetActions(
                modifier = Modifier.height(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertIsDisplayed()
    }

    @Test
    fun allActionsCanBeDisplayed_moreActionDoesNotExists() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            VSheetActions(
                modifier = Modifier.height(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
    }

    @Test
    fun answerActionIsDisplayed_moreActionIsNotDisplayed() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            VSheetActions(
                modifier = Modifier.height(200.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = true,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(answerDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
    }

    @Test
    fun testSheetContentActionsPlacing() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            VSheetActions(
                modifier = Modifier.height(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag4")) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        val childBounds1 = composeTestRule.onNodeWithTag("tag1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithTag("tag2").getBoundsInRoot()
        val moreChild = composeTestRule.onNodeWithContentDescription(moreDescription).getBoundsInRoot()
        childBounds2.bottom.assertIsEqualTo(childBounds1.top - SheetItemsSpacing, "child 2 bottom")
        moreChild.bottom.assertIsEqualTo(childBounds2.top - SheetItemsSpacing, "more child top")
    }

    @Test
    fun testOnActionsPlacedCallback() {
        var itemsCount = -1
        composeTestRule.setContent {
            VSheetActions(
                modifier = Modifier.height(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { itemsCount = it }
            )
        }
        composeTestRule.waitForIdle()
        assertEquals(2, itemsCount)
    }

    @Test
    fun testMaxActionsLessThanActualActions() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        var itemsCount = -1
        val maxActions = 3
        composeTestRule.setContent {
            VSheetActions(
                maxActions = maxActions,
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag4")) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { itemsCount = it }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag3").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("tag4").assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertIsDisplayed()
        assertEquals(maxActions - 1, itemsCount)
    }

    @Test
    fun testMaxActionsEqualToActualActions() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        var itemsCount = -1
        val maxActions = 4
        composeTestRule.setContent {
            VSheetActions(
                maxActions = maxActions,
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag4")) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { itemsCount = it }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag3").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag4").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
        assertEquals(maxActions, itemsCount)
    }

    @Test
    fun answerActionIsDisplayed_actionsAreOneLess() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        var itemsCount = -1
        val maxActions = 2
        composeTestRule.setContent {
            VSheetActions(
                maxActions = maxActions,
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) }
                    )
                ),
                showAnswerAction = true,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { itemsCount = it }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag2").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("tag3").assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(answerDescription).assertIsDisplayed()
        assertEquals(maxActions - 1, itemsCount)
    }

}