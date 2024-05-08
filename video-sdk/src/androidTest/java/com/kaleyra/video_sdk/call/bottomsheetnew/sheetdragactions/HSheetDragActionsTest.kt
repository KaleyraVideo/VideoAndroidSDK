package com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragactions

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HSheetDragActionsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testItemsPlacement() {
        composeTestRule.setContent {
            HSheetDragActions(
                actions = ImmutableList(
                    listOf(
                        { _, _ -> Text("text1", modifier = Modifier.size(24.dp)) },
                        { _, _ -> Text("text2", modifier = Modifier.size(24.dp)) },
                        { _, _ -> Text("text3", modifier = Modifier.size(24.dp)) },
                        { _, _ -> Text("text4", modifier = Modifier.size(24.dp)) }
                    )
                ),
                itemsPerRow = 2
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithText("text1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText("text2").getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithText("text3").getBoundsInRoot()
        val childBounds4 = composeTestRule.onNodeWithText("text4").getBoundsInRoot()
        childBounds1.left.assertIsEqualTo(rootBounds.left, "child 1 left bound")
        childBounds2.left.assertIsEqualTo(childBounds1.right + HSheetDragHorizontalPadding, "child 2 left bound")
        childBounds2.right.assertIsEqualTo(rootBounds.right, "child 2 right bound")
        childBounds3.left.assertIsEqualTo(rootBounds.left, "child 3 left bound")
        childBounds3.top.assertIsEqualTo(childBounds1.bottom + VSheetDragVerticalPadding, "child 3 top bound")
        childBounds4.left.assertIsEqualTo(childBounds3.right + HSheetDragHorizontalPadding, "child 4 left bound")
        childBounds4.right.assertIsEqualTo(rootBounds.right, "child 4 right bound")
        childBounds4.top.assertIsEqualTo(childBounds2.bottom + VSheetDragVerticalPadding, "child 4 top bound")
    }

    @Test
    fun testLastItemOnOneRow() {
        composeTestRule.setContent {
            HSheetDragActions(
                actions = ImmutableList(
                    listOf(
                        { _, modifier-> Text("text1", modifier) },
                        { _, modifier -> Text("text2", modifier) },
                    )
                ),
                itemsPerRow = 3
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithText("text1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText("text2").getBoundsInRoot()
        childBounds1.left.assertIsEqualTo(rootBounds.left, "child 1 left bound")
        childBounds2.left.assertIsEqualTo(childBounds1.right + HSheetDragHorizontalPadding, "child 2 left bound")
        childBounds2.right.assertIsEqualTo(rootBounds.right, "child 2 right bound")
    }

    @Test
    fun testLabelFlag() {
        composeTestRule.setContent {
            HSheetDragActions(
                actions = ImmutableList(
                    listOf(
                        { flag, _ -> assertEquals(flag, true) },
                        { flag, _ -> assertEquals(flag, true) },
                        { flag, _ -> assertEquals(flag, true) },
                    )
                ),
                itemsPerRow = 3
            )
        }
    }
}