package com.kaleyra.video_sdk.ui.call.bottomsheetnew.sheetactions

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.HSheetActions
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionExtendedWidth
import com.kaleyra.video_sdk.call.callactionnew.AnswerActionWidth
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HSheetActionsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showAnswerActionTrue_answerActionIsDisplayed() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetActions(
                actions = ImmutableList(),
                showAnswerAction = true,
                extendedAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
    }

    @Test
    fun showAnswerActionFalse_answerActionIsNotDisplayed() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetActions(
                actions = ImmutableList(),
                showAnswerAction = false,
                extendedAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertDoesNotExist()
    }

    @Test
    fun testOnAnswerActionClick() {
        var isAnswerClicked = false
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetActions(
                actions = ImmutableList(),
                showAnswerAction = true,
                extendedAnswerAction = false,
                onAnswerActionClick = { isAnswerClicked = true  },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertHasClickAction()
        composeTestRule.onNodeWithText(answerDescription).performClick()
        assertEquals(true, isAnswerClicked)
    }

    @Test
    fun testOnMoreActionClick() {
        var isMoreClicked = false
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(100.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                extendedAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = { isMoreClicked = true },
                onActionsPlaced = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(moreDescription).performClick()
        assertEquals(true, isMoreClicked)
    }

    @Test
    fun onlySomeActionsCanBeDisplayed_moreActionIsDisplayed() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                extendedAnswerAction = false,
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
            HSheetActions(
                modifier = Modifier.width(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                extendedAnswerAction = false,
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
            HSheetActions(
                modifier = Modifier.width(200.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = true,
                extendedAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
    }

    @Test
    fun testSheetContentActionsPlacing() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag4")) }
                    )
                ),
                showAnswerAction = false,
                extendedAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { }
            )
        }
        val childBounds1 = composeTestRule.onNodeWithTag("tag1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithTag("tag2").getBoundsInRoot()
        val moreChild = composeTestRule.onNodeWithContentDescription(moreDescription).getBoundsInRoot()
        childBounds2.left.assertIsEqualTo(childBounds1.right + SheetItemsSpacing, "child 2 left")
        moreChild.left.assertIsEqualTo(childBounds2.right + SheetItemsSpacing, "more child left")
    }

    @Test
    fun testOnActionsPlacedCallback() {
        var itemsCount = -1
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(150.dp),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                extendedAnswerAction = false,
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
            HSheetActions(
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
                extendedAnswerAction = false,
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
            HSheetActions(
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
                extendedAnswerAction = false,
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
            HSheetActions(
                maxActions = maxActions,
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) }
                    )
                ),
                showAnswerAction = true,
                extendedAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = {},
                onActionsPlaced = { itemsCount = it }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag2").assertIsNotDisplayed()
        composeTestRule.onNodeWithTag("tag3").assertIsNotDisplayed()
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
        assertEquals(maxActions - 1, itemsCount)
    }

    @Test
    fun extendedAnswerActionFalse_answerActionWidthIsRegular() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetActions(
                actions = ImmutableList(),
                showAnswerAction = true,
                extendedAnswerAction = false,
                onAnswerActionClick = { },
                onMoreActionClick = { },
                onActionsPlaced = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(answerDescription).assertWidthIsEqualTo(AnswerActionWidth)
    }

    @Test
    fun extendedAnswerActionTrue_answerActionWidthIsExtended() {
        val answerDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_answer)
        composeTestRule.setContent {
            HSheetActions(
                actions = ImmutableList(),
                showAnswerAction = true,
                extendedAnswerAction = true,
                onAnswerActionClick = { },
                onMoreActionClick = { },
                onActionsPlaced = { }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(answerDescription).assertWidthIsEqualTo(AnswerActionExtendedWidth)
    }
}