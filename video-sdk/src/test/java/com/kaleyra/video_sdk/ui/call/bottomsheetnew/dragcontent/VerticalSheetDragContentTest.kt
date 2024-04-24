package com.kaleyra.video_sdk.ui.call.bottomsheetnew.dragcontent

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetnew.dragcontent.SheetDragHorizontalPadding
import com.kaleyra.video_sdk.call.bottomsheetnew.dragcontent.SheetDragVerticalPadding
import com.kaleyra.video_sdk.call.bottomsheetnew.dragcontent.VerticalSheetDragContent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VerticalSheetDragContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testItemsPlacement() {
        composeTestRule.setContent {
            VerticalSheetDragContent(
                dragActions = ImmutableList(
                    listOf(
                        { _, _ -> Text("text1", modifier = Modifier.size(24.dp)) },
                        { _, _ -> Text("text2", modifier = Modifier.size(24.dp)) },
                        { _, _ -> Text("text3", modifier = Modifier.size(24.dp)) },
                        { _, _ -> Text("text4", modifier = Modifier.size(24.dp)) }
                    )
                ),
                itemsPerColumn = 2
            )
        }
        val rootBounds = composeTestRule.onRoot().getBoundsInRoot()
        val childBounds1 = composeTestRule.onNodeWithText("text1").getBoundsInRoot()
        val childBounds2 = composeTestRule.onNodeWithText("text2").getBoundsInRoot()
        val childBounds3 = composeTestRule.onNodeWithText("text3").getBoundsInRoot()
        val childBounds4 = composeTestRule.onNodeWithText("text4").getBoundsInRoot()
        childBounds1.top.assertIsEqualTo(childBounds2.bottom + SheetDragHorizontalPadding, "child 1 top bound")
        childBounds1.bottom.assertIsEqualTo(rootBounds.bottom, "child 1 bottom bound")
        childBounds2.top.assertIsEqualTo(rootBounds.top, "child 2 top bound")
        childBounds3.top.assertIsEqualTo(childBounds4.bottom + SheetDragHorizontalPadding, "child 3 top bound")
        childBounds3.bottom.assertIsEqualTo(rootBounds.bottom, "child 3 bottom bound")
        childBounds4.top.assertIsEqualTo(rootBounds.top, "child 4 top bound")
        childBounds3.left.assertIsEqualTo(childBounds1.right + SheetDragVerticalPadding, "child 3 left bound")
        childBounds4.left.assertIsEqualTo(childBounds2.right + SheetDragVerticalPadding, "child 4 left bound")
    }

    @Test
    fun testLabelFlag() {
        composeTestRule.setContent {
            VerticalSheetDragContent(
                dragActions = ImmutableList(
                    listOf(
                        { _, flag -> Assert.assertEquals(false, flag) },
                        { _, flag -> Assert.assertEquals(false, flag) },
                        { _, flag -> Assert.assertEquals(false, flag) },
                    )
                ),
                itemsPerColumn = 3
            )
        }
    }
}