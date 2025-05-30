package com.kaleyra.video_sdk.ui.call.callscreenscaffold

import android.content.res.Resources
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import com.kaleyra.video_sdk.call.callscreenscaffold.CallScreenScaffoldDefaults
import com.kaleyra.video_sdk.call.callscreenscaffold.VCallScreenScaffold
import com.kaleyra.video_sdk.ui.performVerticalSwipe
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class VCallScreenScaffoldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sheetHandleTag = "SheetHandleTag"

    private val sheetDragContentTag = "sheetDragContentTag"

    private val sheetContentTag = "SheetContentTag"

    private val sheetWidth = 200.dp

    private val sheetDragContentHeight = 50.dp

    @Test
    fun testTopAppBarIsDisplayed() {
        val appBarText = "appBarText"
        composeTestRule.setCallScreenScaffold(
            topAppBar = { Text(appBarText) }
        )
        composeTestRule.onNodeWithText(appBarText).assertIsDisplayed()
    }

    @Test
    fun testSheetPanelContentIsDisplayed() {
        val panelText = "panelText"
        composeTestRule.setCallScreenScaffold(
            panelContent = { Text(panelText) }
        )
        composeTestRule.onNodeWithText(panelText).assertIsDisplayed()
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
    fun testSheetPanelIsAboveSheet() {
        val panelText = "panelText"
        composeTestRule.setCallScreenScaffold(
            panelContent = { Text(panelText) }
        )
        composeTestRule.onNodeWithText(panelText).assertIsDisplayed()

        val panelTop = composeTestRule.onNodeWithText(panelText).getBoundsInRoot().top
        val handleTop = composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).getBoundsInRoot().top
        assert(panelTop < handleTop)
    }

    @Test
    fun testPaddingValues() {
        val topBarText = "topBarText"
        val contentText = "contentText"
        val paddingValues = PaddingValues(start = 10.dp, top = 12.dp, end = 14.dp, bottom = 8.dp)
        composeTestRule.setCallScreenScaffold(
            paddingValues = paddingValues,
            topAppBar = {
                Text(topBarText,
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp))
            },
            content = {
                Text(contentText, Modifier.fillMaxSize())
            }
        )
        val rootWidth = composeTestRule.onRoot().getBoundsInRoot().width
        val rootHeight = composeTestRule.onRoot().getBoundsInRoot().height

        val appBarTop = composeTestRule.onNodeWithText(topBarText).getBoundsInRoot().top
        val appBarLeft = composeTestRule.onNodeWithText(topBarText).getBoundsInRoot().left
        val appBarRight = composeTestRule.onNodeWithText(topBarText).getBoundsInRoot().right

        val contentLeft = composeTestRule.onNodeWithText(contentText).getBoundsInRoot().left
        val contentRight = composeTestRule.onNodeWithText(contentText).getBoundsInRoot().right

        val sheetBottom = composeTestRule.onNodeWithTag(sheetContentTag).getBoundsInRoot().bottom

        val topPadding = paddingValues.calculateTopPadding()
        val bottomPadding = paddingValues.calculateBottomPadding()
        val leftPadding = paddingValues.calculateLeftPadding(LayoutDirection.Ltr)
        val rightPadding = paddingValues.calculateRightPadding(LayoutDirection.Ltr)
        appBarTop.assertIsEqualTo(topPadding, "app bar top padding")
        appBarLeft.assertIsEqualTo(leftPadding, "app bar left padding")
        appBarRight.assertIsEqualTo(rootWidth - rightPadding, "app bar right padding")
        contentLeft.assertIsEqualTo(0.dp, "content left padding")
        contentRight.assertIsEqualTo(rootWidth, "content right padding")
        sheetBottom.assertIsEqualTo(rootHeight - bottomPadding, "sheet bottom padding")
    }

    @Test
    fun testContentPaddingValues() {
        val topBarHeight = 48.dp
        val tapBarPadding = 10.dp
        var paddingValues: PaddingValues? = null
        composeTestRule.setCallScreenScaffold(
            topAppBar = {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = tapBarPadding)
                        .height(topBarHeight))
            },
            paddingValues = PaddingValues(0.dp),
            content = { paddingValues = it }
        )
        val sheetTop = composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).getBoundsInRoot().top
        val sheetBottom = composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).getBoundsInRoot().bottom
        val sheetHeight = sheetBottom - sheetTop

        val topPadding = paddingValues!!.calculateTopPadding()
        val bottomPadding = paddingValues!!.calculateBottomPadding()
        topPadding.assertIsEqualTo(topBarHeight + tapBarPadding, "topPadding")
        bottomPadding.assertIsEqualTo(sheetHeight, "bottomPadding")
    }

    @Test
    fun testSwipeUpHandle() {
        val sheetState = CallSheetState()
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performVerticalSwipe(-500)
        composeTestRule.waitForIdle()
        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
        assertEquals(0f, sheetState.offset)
    }

    @Test
    fun testSwipeUpSheetContent() {
        val sheetState = CallSheetState()
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).performVerticalSwipe(-500)
        composeTestRule.waitForIdle()
        assertEquals(CallSheetValue.Expanded, sheetState.currentValue)
        assertEquals(0f, sheetState.offset)
    }

    @Test
    fun testSwipeDownHandle() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).performVerticalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetDragContentHeight.value * Resources.getSystem().displayMetrics.density
        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        assertEquals(offset, sheetState.offset, .5f)
    }

    @Test
    fun testSwipeDownBottomSheetContent() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallScreenScaffold(sheetState)
        composeTestRule.onNodeWithTag(sheetDragContentTag, useUnmergedTree = true).performVerticalSwipe(500)
        composeTestRule.waitForIdle()
        val offset = sheetDragContentHeight.value * Resources.getSystem().displayMetrics.density
        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        assertEquals(offset, sheetState.offset, .5f)
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
        val offset = sheetDragContentHeight.value * Resources.getSystem().displayMetrics.density
        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
        assertEquals(offset, sheetState.offset, .5f)
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
    fun testBottomSheetCollapseHeight() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Collapsed)
        composeTestRule.setCallScreenScaffold(sheetState, paddingValues = PaddingValues(0.dp))
        val sheetContentBounds = composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetHandleBounds = composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetContentHeight = sheetContentBounds.height
        val sheetHandleHeight = sheetHandleBounds.height

        val sheetHeight = sheetContentBounds.bottom - sheetHandleBounds.top
        sheetHeight.assertIsEqualTo(sheetContentHeight + sheetHandleHeight, "sheet height")
    }

    @Test
    fun testBottomSheetExpandedHeight() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallScreenScaffold(sheetState, paddingValues = PaddingValues(0.dp))
        val sheetContentBounds = composeTestRule.onNodeWithTag(sheetContentTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetDragContentBounds = composeTestRule.onNodeWithTag(sheetDragContentTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetHandleBounds = composeTestRule.onNodeWithTag(sheetHandleTag, useUnmergedTree = true).getBoundsInRoot()
        val sheetContentHeight = sheetContentBounds.height
        val sheetDragContentHeight = sheetDragContentBounds.height
        val sheetHandleHeight = sheetHandleBounds.height

        val sheetHeight = sheetContentBounds.bottom - sheetHandleBounds.top
        sheetHeight.assertIsEqualTo(sheetContentHeight + sheetDragContentHeight + sheetHandleHeight, "sheet height")
    }

    @Test
    fun testBottomSheetCollapsedWhenMovingToPanelVisualization() {
        val sheetState = CallSheetState(initialValue = CallSheetValue.Expanded)
        composeTestRule.setCallScreenScaffold(
            sheetState = sheetState,
            panelContent = { }
        )

        assertEquals(CallSheetValue.Collapsed, sheetState.currentValue)
    }

    @Test
    @Config(qualifiers = "w480dp-h840dp")
    fun testBrandLogoComposableCalled() {
        var hasCalledBrandLogo = false
        composeTestRule.setCallScreenScaffold(
            brandLogo = {
                hasCalledBrandLogo = true
            }
        )

        composeTestRule.waitForIdle()

        assertEquals(true, hasCalledBrandLogo)
    }

    @Test
    @Config(qualifiers = "w480dp-h840dp")
    fun testBrandLogoComposableCalledWithLargeScreenAndSizeClassMedium() {
        var hasCalledBrandLogo = false
        composeTestRule.setCallScreenScaffold(
            windowSizeClass = mockk {
                every { widthSizeClass } returns WindowWidthSizeClass.Medium
            },
            brandLogo = {
                hasCalledBrandLogo = true
            }
        )

        composeTestRule.waitForIdle()

        assertEquals(true, hasCalledBrandLogo)
    }

    @Test
    @Config(qualifiers = "w480dp-h840dp")
    fun testBrandLogoComposableCalledWithExpandedSizeClass() {
        var hasCalledBrandLogo = false
        composeTestRule.setCallScreenScaffold(
            windowSizeClass = mockk {
                every { widthSizeClass } returns WindowWidthSizeClass.Expanded
            },
            brandLogo = {
                hasCalledBrandLogo = true
            }
        )

        composeTestRule.waitForIdle()

        assertEquals(true, hasCalledBrandLogo)
    }

    @Test
    @Config(qualifiers = "w100dp-h200dp")
    fun testBrandLogoComposableNotCalledOnSmallScreen() {
        var hasCalledBrandLogo = false
        composeTestRule.setCallScreenScaffold(
            brandLogo = {
                hasCalledBrandLogo = true
            }
        )

        composeTestRule.waitForIdle()

        assertEquals(false, hasCalledBrandLogo)
    }

    private fun ComposeContentTestRule.setCallScreenScaffold(
        sheetState: CallSheetState = CallSheetState(),
        windowSizeClass: WindowSizeClass = mockk(relaxed = true),
        topAppBar: @Composable () -> Unit = {},
        panelContent: @Composable (ColumnScope.() -> Unit)? = null,
        paddingValues: PaddingValues = CallScreenScaffoldDefaults.PaddingValues,
        content: @Composable (PaddingValues) -> Unit = {},
        brandLogo: @Composable (BoxScope) -> Unit = {},
    ) {
        setContent {
            VCallScreenScaffold(
                sheetState = sheetState,
                windowSizeClass = windowSizeClass,
                topAppBar = topAppBar,
                sheetPanelContent = panelContent,
                sheetContent = {
                    Spacer(
                        Modifier
                            .width(sheetWidth)
                            .height(80.dp)
                            .testTag(sheetContentTag)
                    )
                },
                sheetDragContent = {
                    Spacer(
                        Modifier
                            .width(100.dp)
                            .height(sheetDragContentHeight)
                            .testTag(sheetDragContentTag))
                },
                sheetDragHandle = {
                    Spacer(
                        Modifier
                            .width(150.dp)
                            .height(30.dp)
                            .testTag(sheetHandleTag)
                    )
                },
                paddingValues = paddingValues,
                brandLogo = brandLogo,
                content = content
            )
        }
    }
}