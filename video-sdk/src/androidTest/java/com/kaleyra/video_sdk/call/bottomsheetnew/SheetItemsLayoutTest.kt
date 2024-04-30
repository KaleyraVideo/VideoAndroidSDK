package com.kaleyra.video_sdk.call.bottomsheetnew

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SheetItemsLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNoItems() {
        composeTestRule.setContent {
            SheetItemsLayout {}
        }
    }

    @Test
    fun testMaxItems() {
        val maxItems = 2
        var itemsPlaced = -1
        composeTestRule.setContent {
            SheetItemsLayout(
                maxItems = maxItems,
                onItemsPlaced = { itemsPlaced = it }
            ) {
                Text("text1")
                Text("text2")
                Text("text3")
                Text("text4")
            }
        }
        composeTestRule.onNodeWithText("text1").assertIsDisplayed()
        composeTestRule.onNodeWithText("text2").assertIsDisplayed()
        composeTestRule.onNodeWithText("text3").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("text4").assertIsNotDisplayed()
        assertEquals(maxItems, itemsPlaced)
    }

    @Test
    fun testLayoutHeight() {
        composeTestRule.setContent {
            SheetItemsLayout {
                Spacer(Modifier.height(20.dp))
                Spacer(Modifier.height(10.dp))
                Spacer(Modifier.height(30.dp))
            }
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        rootBounds.height.assertIsEqualTo(30.dp, "root height")
    }

    @Test
    fun testItemsPlacement() {
        val spacing = 26.dp
        composeTestRule.setContent {
            SheetItemsLayout(
                horizontalItemSpacing = spacing
            ) {
                Text("text1")
                Text("text2")
                Text("text3")
            }
        }
        val childBounds1 = composeTestRule.onNodeWithText("text1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText("text2").getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithText("text3").getBoundsInRoot()
        childBounds1.left.assertIsEqualTo(0.dp, "child 1 left bound")
        childBounds2.left.assertIsEqualTo(childBounds1.right + spacing, "child 2 left bound")
        childBounds3.left.assertIsEqualTo(childBounds2.right + spacing, "child 3 left bound")
    }

    @Test
    fun testNoSpacingAddedAtTheEnd() {
        val spacing = 26.dp
        composeTestRule.setContent {
            SheetItemsLayout(
                horizontalItemSpacing = spacing
            ) {
                Text("text1")
                Text("text2")
                Text("text3")
            }
        }
        val childBounds = composeTestRule.onNodeWithText("text3").getBoundsInRoot()
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        childBounds.right.assertIsEqualTo(rootBounds.right, "last child right bound")
    }

    @Test
    fun testOnItemsPlaced() {
        val layoutWidth = 75.dp
        var itemsPlaced = -1
        composeTestRule.setContent {
            SheetItemsLayout(
                modifier = Modifier.width(layoutWidth),
                horizontalItemSpacing = 0.dp,
                onItemsPlaced = { itemsPlaced = it }
            ) {
                repeat(5) {
                    Spacer(Modifier.width(24.dp))
                }
            }
        }
        assertEquals(3, itemsPlaced)
    }
}