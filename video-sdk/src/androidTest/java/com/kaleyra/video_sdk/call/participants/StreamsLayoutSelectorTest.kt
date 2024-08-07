package com.kaleyra.video_sdk.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.participants.view.StreamsLayoutSelector
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class StreamsLayoutSelectorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streamsLayout by mutableStateOf(StreamsLayout.Grid)

    private var enableGridLayout by mutableStateOf(true)

    private var layoutClicked: StreamsLayout? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamsLayoutSelector(
                streamsLayout = streamsLayout,
                enableGridLayout = enableGridLayout,
                onLayoutClick = { layoutClicked = it }
            )
        }
    }

    @After
    fun tearDown() {
        streamsLayout = StreamsLayout.Grid
        enableGridLayout = true
        layoutClicked = null
    }

    @Test
    fun testGridButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_grid)
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testPinButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testEnableGridLayoutFalse() {
        enableGridLayout = false
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_grid)
        composeTestRule.onNodeWithText(text).assertHasClickAction().assertIsNotEnabled()
    }

    @Test
    fun testOnClickGridButton() {
        streamsLayout = StreamsLayout.Pin
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_grid)
        composeTestRule.onNodeWithText(text).performClick()
        assertEquals(StreamsLayout.Grid, layoutClicked)
    }

    @Test
    fun testOnClickPinButton() {
        streamsLayout = StreamsLayout.Grid
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_pin)
        composeTestRule.onNodeWithText(text).performClick()
        assertEquals(StreamsLayout.Pin, layoutClicked)
    }
}