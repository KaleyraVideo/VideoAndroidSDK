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
import com.kaleyra.video_sdk.call.bottomsheetnew.SheetActionsSpacing
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.HSheetActions
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
                sheetState = rememberCallSheetState(),
                actions = ImmutableList(),
                showAnswerAction = true,
                onAnswerActionClick = { },
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
                sheetState = rememberCallSheetState(),
                actions = ImmutableList(),
                showAnswerAction = false,
                onAnswerActionClick = { },
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
                sheetState = rememberCallSheetState(),
                actions = ImmutableList(),
                showAnswerAction = true,
                onAnswerActionClick = { isAnswerClicked = true  },
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertHasClickAction()
        composeTestRule.onNodeWithText(answerDescription).performClick()
        assertEquals(true, isAnswerClicked)
    }

    @Test
    fun onlySomeActionsCanBeDisplayed_moreActionIsDisplayed() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(150.dp),
                sheetState = rememberCallSheetState(),
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
                sheetState = rememberCallSheetState(),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp)) },
                        { _, _ -> Spacer(Modifier.size(24.dp)) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
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
                sheetState = rememberCallSheetState(),
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
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithText(answerDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(moreDescription).assertDoesNotExist()
    }

    @Test
    fun testExpandOnMoreActionClick() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Collapsed)
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(75.dp),
                sheetState = sheetState,
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
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(moreDescription).performClick()
        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
    }

    @Test
    fun testCollapseOnMoreActionClick() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(75.dp),
                sheetState = sheetState,
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
                onActionsPlaced = { }
            )
        }
        composeTestRule.onNodeWithContentDescription(moreDescription).assertHasClickAction()
        composeTestRule.onNodeWithContentDescription(moreDescription).performClick()
        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    fun testSheetContentItemsPlacing() {
        val moreDescription = composeTestRule.activity.getString(R.string.kaleyra_call_sheet_more_actions)
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(200.dp),
                sheetState = rememberCallSheetState(),
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
                onActionsPlaced = { }
            )
        }
        val childBounds1 = composeTestRule.onNodeWithTag("tag1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithTag("tag2").getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithTag("tag3").getBoundsInRoot()
        val moreChild = composeTestRule.onNodeWithContentDescription(moreDescription).getBoundsInRoot()
        childBounds2.left.assertIsEqualTo(childBounds1.right + SheetActionsSpacing, "child 2 left")
        childBounds3.left.assertIsEqualTo(childBounds2.right + SheetActionsSpacing, "child 2 left")
        moreChild.left.assertIsEqualTo(childBounds3.right + SheetActionsSpacing, "more child left")
    }

    @Test
    fun testOnItemsPlacedCallback() {
        var itemsCount = -1
        composeTestRule.setContent {
            HSheetActions(
                modifier = Modifier.width(150.dp),
                sheetState = rememberCallSheetState(),
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
                onActionsPlaced = { itemsCount = it }
            )
        }
        composeTestRule.waitForIdle()
        assertEquals(2, itemsCount)
    }

    @Test
    fun testMaxActions() {
        var itemsCount = -1
        val maxActions = 2
        composeTestRule.setContent {
            HSheetActions(
                maxActions = maxActions,
                sheetState = rememberCallSheetState(),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) }
                    )
                ),
                showAnswerAction = false,
                onAnswerActionClick = { },
                onActionsPlaced = { itemsCount = it }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("tag1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tag3").assertIsNotDisplayed()
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
                sheetState = rememberCallSheetState(),
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag1")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag2")) },
                        { _, _ -> Spacer(Modifier.size(24.dp).testTag("tag3")) }
                    )
                ),
                showAnswerAction = true,
                onAnswerActionClick = { },
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
}