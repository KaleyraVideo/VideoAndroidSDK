package com.kaleyra.video_sdk.ui.call.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterUiState
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionDeepFilterOptionTag
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionSettingsComponent
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionSettingsTag
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionStandardOptionTag
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoiseSuppressionComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private var requestedNoiseFilterMode: NoiseFilterModeUi? = null

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        unmockkAll()
        requestedNoiseFilterMode = null
    }

    @Test
    fun testTitleDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_info_noise_suppression)).assertIsDisplayed()
    }

    @Test
    fun testDeepFilterNetNoiseFilterOptionDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithTag(NoiseSuppressionSettingsTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai)).assertIsDisplayed()
    }

    @Test
    fun testStandardNoiseFilterOptionDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_standard)).assertIsDisplayed()
    }

    @Test
    fun testDeepFilterNetNoiseFilterRadioButtonDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithTag(NoiseSuppressionDeepFilterOptionTag).assertIsDisplayed()
    }

    @Test
    fun testDeepFilterNetNoiseFilterRadioButtonNotDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai)).assertIsNotDisplayed()
    }

    @Test
    fun testStandardNoiseFilterRadioButtonDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithTag(NoiseSuppressionStandardOptionTag).assertIsDisplayed()
    }

    @Test
    fun testDeepFilterNetNoiseFilterOptionSelected() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard)),
            currentNoiseFilterModeUi = NoiseFilterModeUi.DeepFilterAi
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithTag(NoiseSuppressionDeepFilterOptionTag).assertIsSelected()
    }

    @Test
    fun testStandardNoiseFilterOptionSelected() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard)),
            currentNoiseFilterModeUi = NoiseFilterModeUi.Standard
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithTag(NoiseSuppressionStandardOptionTag).assertIsSelected()
    }

    @Test
    fun testDeepFilterNetNoiseFilterOptionTextCLicked_deepFilterNetNoiseFilterModeSetOnViewModel() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai)).performClick()

        Assert.assertEquals(NoiseFilterModeUi.DeepFilterAi, requestedNoiseFilterMode)
    }

    @Test
    fun testStandardNoiseFilterOptionTextCLicked_standardModeSetOnViewModel() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard))
        ), { requestedNoiseFilterMode = it })}

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_standard)).performClick()

        Assert.assertEquals(NoiseFilterModeUi.Standard, requestedNoiseFilterMode)
    }
}
