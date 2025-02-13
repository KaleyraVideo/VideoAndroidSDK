package com.kaleyra.video_sdk.ui.call.participants

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.participants.view.StreamsLayoutSelector
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StreamsLayoutSelectorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var streamsLayout by mutableStateOf(StreamsLayout.Mosaic)

    private var layoutClicked: StreamsLayout? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            StreamsLayoutSelector(
                streamsLayout = streamsLayout,
                onLayoutClick = { layoutClicked = it }
            )
        }
    }

    @After
    fun tearDown() {
        streamsLayout = StreamsLayout.Mosaic
        layoutClicked = null
    }

    @Test
    fun testGridButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mosaic)
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testPinButtonIsDisplayed() {
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_auto)
        composeTestRule.onNodeWithText(text).assertHasClickAction()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    @Test
    fun testOnClickGridButton() {
        streamsLayout = StreamsLayout.Auto
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_mosaic)
        composeTestRule.onNodeWithText(text).performClick()
        assertEquals(StreamsLayout.Mosaic, layoutClicked)
    }

    @Test
    fun testOnClickPinButton() {
        streamsLayout = StreamsLayout.Mosaic
        val text = composeTestRule.activity.getString(R.string.kaleyra_participants_component_auto)
        composeTestRule.onNodeWithText(text).performClick()
        assertEquals(StreamsLayout.Auto, layoutClicked)
    }
}