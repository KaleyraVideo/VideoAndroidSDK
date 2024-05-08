package com.kaleyra.video_sdk.call.bottomsheetnew.sheetactions.sheetitemslayout

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class VSheetItemsLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNoItems() {
        composeTestRule.setContent {
            VSheetItemsLayout {}
        }
    }

    @Test
    fun testMaxItems() {
        val maxItems = 2
        var itemsPlaced = -1
        composeTestRule.setContent {
            VSheetItemsLayout(
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
    fun testLayoutWidth() {
        composeTestRule.setContent {
            VSheetItemsLayout {
                Spacer(Modifier.width(20.dp))
                Spacer(Modifier.width(10.dp))
                Spacer(Modifier.width(30.dp))
            }
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        rootBounds.width.assertIsEqualTo(30.dp, "root width")
    }

    @Test
    fun testItemsPlacement() {
        val spacing = 26.dp
        var itemsCount = -1
        composeTestRule.setContent {
            VSheetItemsLayout(
                verticalItemSpacing = spacing,
                onItemsPlaced = { itemsCount = it }
            ) {
                Text("text1")
                Text("text2")
                Text("text3")
            }
        }
        val parentBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithText("text1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText("text2").getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithText("text3").getBoundsInRoot()
        childBounds1.bottom.assertIsEqualTo(parentBounds.bottom, "child 1 top bound")
        childBounds2.bottom.assertIsEqualTo(childBounds1.top - spacing, "child 2 top bound")
        childBounds3.bottom.assertIsEqualTo(childBounds2.top - spacing, "child 3 top bound")
        assertEquals(3, itemsCount)
    }

    @Test
    fun testNoSpacingAddedAtTheTop() {
        val spacing = 26.dp
        composeTestRule.setContent {
            VSheetItemsLayout(verticalItemSpacing = spacing) {
                Text("text1")
                Text("text2")
                Text("text3")
            }
        }
        val childBounds = composeTestRule.onNodeWithText("text3").getBoundsInRoot()
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        childBounds.top.assertIsEqualTo(rootBounds.top, "last child top bound")
    }

    @Test
    fun testOnItemsPlaced() {
        val layoutHeight = 75.dp
        var itemsCount = -1
        composeTestRule.setContent {
            VSheetItemsLayout(
                modifier = Modifier.height(layoutHeight),
                verticalItemSpacing = 0.dp,
                onItemsPlaced = { itemsCount = it }
            ) {
                repeat(5) {
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
        assertEquals(3, itemsCount)
    }
}