package com.kaleyra.video_sdk.call.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterUiState
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionDeepFilterOptionTag
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionNoneOptionTag
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
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_info_noise_suppression)).assertIsDisplayed()
    }

    @Test
    fun testDeepFilterNetNoiseFilterOptionDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithTag(NoiseSuppressionSettingsTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai)).assertIsDisplayed()
    }

    @Test
    fun testStandardNoiseFilterOptionDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_standard)).assertIsDisplayed()
    }

    @Test
    fun testDisableNoiseFilterOptionDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_none)).assertIsDisplayed()
    }

    @Test
    fun testDeepFilterNetNoiseFilterRadioButtonDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai)).assertIsDisplayed()
    }

    @Test
    fun testStandardNoiseFilterRadioButtonDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_standard)).assertIsDisplayed()
    }

    @Test
    fun testDisableNoiseFilterRadioButtonDisplayed() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_none)).assertIsDisplayed()
    }

    @Test
    fun testDeepFilterNetNoiseFilterOptionSelected() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None)),
            currentNoiseFilterModeUi = NoiseFilterModeUi.DeepFilterAi
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithTag(NoiseSuppressionDeepFilterOptionTag).assertIsSelected()
    }

    @Test
    fun testStandardNoiseFilterOptionSelected() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None)),
            currentNoiseFilterModeUi = NoiseFilterModeUi.Standard
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithTag(NoiseSuppressionStandardOptionTag).assertIsSelected()
    }

    @Test
    fun tesNoneNoiseFilterOptionSelected() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None)),
            currentNoiseFilterModeUi = NoiseFilterModeUi.None
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithTag(NoiseSuppressionNoneOptionTag).assertIsSelected()
    }

    @Test
    fun testDeepFilterNetNoiseFilterOptionTextCLicked_deepFilterNetNoiseFilterModeSetOnViewModel() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai)).performClick()

        Assert.assertEquals(NoiseFilterModeUi.DeepFilterAi, requestedNoiseFilterMode)
    }

    @Test
    fun testStandardNoiseFilterOptionTextCLicked_standardModeSetOnViewModel() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_standard)).performClick()

        Assert.assertEquals(NoiseFilterModeUi.Standard, requestedNoiseFilterMode)
    }

    @Test
    fun testDisableNoiseFilterOptionTextCLicked_disableNoiseFilterModeSetOnViewModel() {
        composeTestRule.setContent { NoiseSuppressionSettingsComponent(NoiseFilterUiState(
            supportedNoiseFilterModesUi = ImmutableList(listOf(NoiseFilterModeUi.DeepFilterAi, NoiseFilterModeUi.Standard, NoiseFilterModeUi.None))
        )) { requestedNoiseFilterMode = it } }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_action_noise_suppression_none)).performClick()

        Assert.assertEquals(NoiseFilterModeUi.None, requestedNoiseFilterMode)
    }
}
