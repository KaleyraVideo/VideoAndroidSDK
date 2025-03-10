@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.kaleyra.video_sdk.call.callscreenscaffold

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheet.CallSheetState
import org.junit.Rule

@OptIn(ExperimentalMaterial3Api::class)
internal abstract class VCallScreenScaffoldBaseTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    val sheetHandleTag = "SheetHandleTag"

    val sheetDragContentTag = "sheetDragContentTag"

    val sheetContentTag = "SheetContentTag"

    val sheetWidth = 200.dp

    val sheetDragContentHeight = 50.dp

    protected fun ComposeContentTestRule.setCallScreenScaffold(
        sheetState: CallSheetState = CallSheetState(),
        topAppBar: @Composable () -> Unit = {},
        panelContent: @Composable (ColumnScope.() -> Unit)? = null,
        paddingValues: PaddingValues = CallScreenScaffoldDefaults.PaddingValues,
        content: @Composable (PaddingValues) -> Unit = {},
        brandLogo: @Composable (BoxScope) -> Unit = {},
    ) {
        setContent {
            VCallScreenScaffold(
                sheetState = sheetState,
                windowSizeClass = calculateWindowSizeClass(composeTestRule.activity),
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