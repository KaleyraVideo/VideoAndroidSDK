package com.kaleyra.video_sdk.ui.call.bottomsheetnew

import android.content.res.Resources
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheet
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetValue
import com.kaleyra.video_sdk.ui.performVerticalSwipe
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallBottomSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sheetHandleTag = "SheetHandleTag"

    private val sheetContentTag = "SheetContentTag"

    private val sheetContentHeight = 50.dp

    @Test
    fun testSwipeUpHandle() {
        val sheetState = CallSheetState()
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performVerticalSwipe(-500)
        composeTestRule.waitForIdle()
        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
        assertEquals(0f, sheetState.offset)
    }

    @Test
    fun testSwipeDownHandle() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performVerticalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetContentHeight.value * Resources.getSystem().displayMetrics.density
        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testSwipeDownBottomSheetContent() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).performVerticalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetContentHeight.value * Resources.getSystem().displayMetrics.density
        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testCollapseBottomSheetOnScrimClick() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallBottomSheet(sheetState)
        composeTestRule.onRoot().performClick()
        composeTestRule.waitForIdle()
        val offset = sheetContentHeight.value * Resources.getSystem().displayMetrics.density
        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        assertEquals(offset, sheetState.offset, .5f)
    }

    private fun ComposeContentTestRule.setCallBottomSheet(sheetState: CallSheetState) {
        setContent {
            CallBottomSheet(
                sheetState = sheetState,
                sheetContent = {
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .height(sheetContentHeight)
                            .testTag(sheetContentTag))
                },
                sheetDragHandle = {
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .testTag(sheetHandleTag)
                    )
                }
            ) {
                Spacer(Modifier.fillMaxWidth().height(80.dp))
            }
        }
    }
}