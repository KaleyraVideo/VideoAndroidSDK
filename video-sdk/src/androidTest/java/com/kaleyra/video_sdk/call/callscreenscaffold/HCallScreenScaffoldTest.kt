package com.kaleyra.video_sdk.call.callscreenscaffold

import android.content.res.Resources
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEqualTo
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetValue
import com.kaleyra.video_sdk.performHorizontalSwipe
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HCallScreenScaffoldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sheetHandleTag = "SheetHandleTag"

    private val sheetDragContentTag = "sheetDragContentTag"

    private val sheetContentTag = "SheetContentTag"

    private val sheetDragContentWidth = 50.dp

    @Test
    fun testTopAppBarIsDisplayed() {
        val appBarText = "appBarText"
        composeTestRule.setCallScreenScaffold(
            topAppBar = { Text(appBarText) }
        )
        composeTestRule.onNodeWithText(appBarText).assertIsDisplayed()
    }

    @Test
    fun testContentIsDisplayed() {
        val contentText = "contentText"
        composeTestRule.setCallScreenScaffold(
            content = { Text(contentText) }
        )
        composeTestRule.onNodeWithText(contentText).assertIsDisplayed()
    }

    @Test
    fun testPaddingValues() {
        val topBarText = "topBarText"
        val contentText = "contentText"
        val paddingValues = PaddingValues(start = 10.dp, top = 12.dp, end = 14.dp, bottom = 8.dp)
        composeTestRule.setCallScreenScaffold(
            paddingValues = paddingValues,
            topAppBar = {
                Text(topBarText, Modifier.fillMaxWidth().height(48.dp))
            },
            content = {
                Text(contentText, Modifier.fillMaxSize())
            }
        )
        val rootWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val rootHeight = composeTestRule.onRoot().getBoundsInRoot().height

        val appBarTop = composeTestRule.onNodeWithText(topBarText).getBoundsInRoot().top
        val appBarLeft = composeTestRule.onNodeWithText(topBarText).getBoundsInRoot().left

        val contentLeft = composeTestRule.onNodeWithText(contentText).getBoundsInRoot().left
        val contentBottom = composeTestRule.onNodeWithText(contentText).getBoundsInRoot().bottom

        val sheetRight = composeTestRule.onNodeWithTag(sheetContentTag).getBoundsInRoot().right

        val topPadding = paddingValues.calculateTopPadding()
        val leftPadding = paddingValues.calculateLeftPadding(LayoutDirection.Ltr)
        val rightPadding = paddingValues.calculateRightPadding(LayoutDirection.Ltr)
        appBarTop.assertIsEqualTo(topPadding, "app bar top padding")
        appBarLeft.assertIsEqualTo(leftPadding, "app bar left padding")
        contentLeft.assertIsEqualTo(0.dp, "content left padding")
        contentBottom.assertIsEqualTo(rootHeight, "content bottom padding")
        sheetRight.assertIsEqualTo(rootWidth - rightPadding, "sheet right padding")
    }

    @Test
    fun testContentPaddingValues() {
        val topBarHeight = 48.dp
        val topBarPadding = 10.dp
        var paddingValues: PaddingValues? = null
        composeTestRule.setCallScreenScaffold(
            topAppBar = {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = topBarPadding)
                        .height(topBarHeight)
                )
            },
            paddingValues = PaddingValues(0.dp),
            content = { paddingValues = it }
        )
        val sheetLeft = composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).getBoundsInRoot().left
        val sheetRight = composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).getBoundsInRoot().right
        val sheetWidth = sheetRight - sheetLeft

        val topPadding = paddingValues!!.calculateTopPadding()
        val rightPadding = paddingValues!!.calculateRightPadding(LayoutDirection.Ltr)
        topPadding.assertIsEqualTo(topBarHeight + topBarPadding, "topPadding")
        rightPadding.assertIsEqualTo(sheetWidth, "rightPadding")
    }

    @Test
    fun testSwipeUpHandle() {
        val sheetState = CallSheetState()
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performHorizontalSwipe(-500)
        composeTestRule.waitForIdle()
        Assert.assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
        Assert.assertEquals(0f, sheetState.offset)
    }

    @Test
    fun testSwipeUpSheetContent() {
        val sheetState = CallSheetState()
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).performHorizontalSwipe(-500)
        composeTestRule.waitForIdle()
        Assert.assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
        Assert.assertEquals(0f, sheetState.offset)
    }

    @Test
    fun testSwipeDownHandle() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performHorizontalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetDragContentWidth.value * Resources.getSystem().displayMetrics.density
        Assert.assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        Assert.assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testSwipeDownBottomSheetContent() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetDragContentTag, useUnmergedTree = true).performHorizontalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetDragContentWidth.value * Resources.getSystem().displayMetrics.density
        Assert.assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        Assert.assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testCollapseBottomSheetOnScrimClick() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        val contentText = "contentText"
        composeTestRule.setCallScreenScaffold(
            sheetState,
            content = { Text(contentText) }
        )
        composeTestRule.onNodeWithText(contentText).performClick()
        composeTestRule.waitForIdle()
        val offset = sheetDragContentWidth.value * Resources.getSystem().displayMetrics.density
        Assert.assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        Assert.assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testContentFillMaxSize() {
        val contentText = "contentText"
        composeTestRule.setCallScreenScaffold(
            content = {
                Text(contentText, Modifier.fillMaxSize())
            },
            paddingValues = PaddingValues(0.dp)
        )
        val contentWidth = composeTestRule.onNodeWithText(contentText).getBoundsInRoot().width
        val contentHeight = composeTestRule.onNodeWithText(contentText).getBoundsInRoot().height

        val rootWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val rootHeight = composeTestRule.onRoot().getBoundsInRoot().height

        contentWidth.assertIsEqualTo(rootWidth, "content width")
        contentHeight.assertIsEqualTo(rootHeight, "content height")
    }

    @Test
    fun testBottomSheetCollapseWidth() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Collapsed)
        composeTestRule.setCallScreenScaffold(sheetState, paddingValues = PaddingValues(0.dp))
        val sheetContentBounds = composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetHandleBounds = composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetContentHeight = sheetContentBounds.width
        val sheetHandleHeight = sheetHandleBounds.width

        val sheetHeight = sheetContentBounds.right - sheetHandleBounds.left
        sheetHeight.assertIsEqualTo(sheetContentHeight + sheetHandleHeight, "sheet width")
    }

    @Test
    fun testBottomSheetExpandedWidth() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallScreenScaffold(sheetState, paddingValues = PaddingValues(0.dp))
        val sheetContentBounds = composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetDragContentBounds = composeTestRule.onNodeWithTag(sheetDragContentTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetHandleBounds = composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetContentHeight = sheetContentBounds.width
        val sheetDragContentHeight = sheetDragContentBounds.width
        val sheetHandleHeight = sheetHandleBounds.width

        val sheetHeight = sheetContentBounds.right - sheetHandleBounds.left
        sheetHeight.assertIsEqualTo(sheetContentHeight + sheetDragContentHeight + sheetHandleHeight, "sheet width")
    }

    @Test
    fun testBrandLogoComposableCalled() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        var hasCalledBrandLogo = false
        composeTestRule.setCallScreenScaffold(
            sheetState = sheetState,
            brandLogo = {
                hasCalledBrandLogo = true
            }
        )

        composeTestRule.waitForIdle()

        assertEquals(true, hasCalledBrandLogo)
    }

    private fun ComposeContentTestRule.setCallScreenScaffold(
        sheetState: CallSheetState = CallSheetState(),
        topAppBar: @Composable () -> Unit = {},
        paddingValues: PaddingValues = CallScreenScaffoldDefaults.PaddingValues,
        content: @Composable (PaddingValues) -> Unit = {},
        brandLogo: @Composable (BoxScope) -> Unit = {},
    ) {
        setContent {
            HCallScreenScaffold(
                sheetState = sheetState,
                topAppBar = topAppBar,
                sheetContent = {
                    Spacer(
                        Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .testTag(sheetContentTag)
                    )
                },
                sheetDragContent = {
                    Spacer(
                        Modifier
                            .fillMaxHeight()
                            .width(sheetDragContentWidth)
                            .testTag(sheetDragContentTag))
                },
                sheetDragHandle = {
                    Spacer(
                        Modifier
                            .fillMaxHeight()
                            .width(30.dp)
                            .testTag(sheetHandleTag)
                    )
                },
                paddingValues = paddingValues,
                content = content,
                brandLogo = brandLogo,
            )
        }
    }
}