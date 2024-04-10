package com.kaleyra.video_sdk.call.bottomsheetnew

import android.content.res.Resources
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.performHorizontalSwipe
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class VerticalCallBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sheetHandleTag = "SheetHandleTag"

    private val sheetContentTag = "SheetContentTag"

    private val sheetContentWidth = 50.dp

    @Test
    fun testSwipeUpHandle() {
        val sheetState = CallSheetState()
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performHorizontalSwipe(-500)
        composeTestRule.waitForIdle()
        Assert.assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
        Assert.assertEquals(0f, sheetState.offset)
    }

    @Test
    fun testSwipeDownHandle() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performHorizontalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetContentWidth.value * Resources.getSystem().displayMetrics.density
        Assert.assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        Assert.assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testSwipeDownBottomSheetContent() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).performHorizontalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetContentWidth.value * Resources.getSystem().displayMetrics.density
        Assert.assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        Assert.assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testCollapseBottomSheetOnScrimClick() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onRoot().performClick()
        composeTestRule.waitForIdle()
        val offset = sheetContentWidth.value * Resources.getSystem().displayMetrics.density
        Assert.assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        Assert.assertEquals(offset, sheetState.offset, .5f)
    }

    private fun ComposeContentTestRule.setCallBottomSheet(sheetState: CallSheetState) {
        setContent {
            VerticalCallBottomSheet(
                sheetState = sheetState,
                sheetContent = {
                    Spacer(
                        Modifier
                            .fillMaxHeight()
                            .width(sheetContentWidth)
                            .testTag(sheetContentTag))
                },
                sheetDragHandle = {
                    Spacer(
                        Modifier
                            .fillMaxHeight()
                            .width(30.dp)
                            .testTag(sheetHandleTag)
                    )
                }
            ) {
                Spacer(Modifier.fillMaxHeight().width(80.dp))
            }
        }
    }
}