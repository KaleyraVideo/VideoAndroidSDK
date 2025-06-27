@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundBlurOptionTag
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundImageOptionTag
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundNoneOptionTag
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundSettingsComponent
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundSettingsTag
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VirtualBackgroundSettingsComponentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    var requestedVirtualBackgroundUi: VirtualBackgroundUi? = null

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
        unmockkAll()
        requestedVirtualBackgroundUi = null
    }

    @Test
    fun testNoVirtualBackground_cameraSettingsNotDisplayed() = runTest {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(
                VirtualBackgroundUiState(),
                { requestedVirtualBackgroundUi = it })
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_info_camera_settings)).assertDoesNotExist()
    }

    @Test
    fun testOnlyNoneVirtualBackground_cameraSettingsNotDisplayed() = runTest {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(
                VirtualBackgroundUiState(backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None))),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_info_camera_settings)).assertDoesNotExist()
    }

    @Test
    fun testVirtualBackgroundsPresent_cameraSettingsDisplayed() = runTest {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(
                VirtualBackgroundUiState(
                    backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Image("blur"), VirtualBackgroundUi.Blur("blur")))),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithTag(VirtualBackgroundSettingsTag).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_common_ui.R.string.kaleyra_strings_info_camera_settings)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_virtual_background_blur)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_virtual_background_image)).assertIsDisplayed()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(com.kaleyra.video_sdk.R.string.kaleyra_virtual_background_none)).assertIsDisplayed()
    }

    @Test
    fun testNoneVirtualBackgroundClicked_noneCameraEffectsSet() {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(VirtualBackgroundUiState(
                backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Image("image"), VirtualBackgroundUi.Blur("blur")))),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithTag(VirtualBackgroundNoneOptionTag).performClick()

        Assert.assertEquals(VirtualBackgroundUi.None, requestedVirtualBackgroundUi)
    }

    @Test
    fun testBlurVirtualBackgroundClicked_blurCameraEffectsSet() {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(VirtualBackgroundUiState(
                backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Image("image"), VirtualBackgroundUi.Blur("blur")))),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithTag(VirtualBackgroundBlurOptionTag).performClick()
        Assert.assertEquals(VirtualBackgroundUi.Blur("blur"), requestedVirtualBackgroundUi)
    }

    @Test
    fun testImageVirtualBackgroundClicked_imageCameraEffectsSet() {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(VirtualBackgroundUiState(
                backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Image("image"), VirtualBackgroundUi.Blur("blur")))),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithTag(VirtualBackgroundImageOptionTag).performClick()
        Assert.assertEquals(VirtualBackgroundUi.Image("image"), requestedVirtualBackgroundUi)
    }

    @Test
    fun testVirtualBackgroundBlurOptionSelected() {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(VirtualBackgroundUiState(
                backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Image("image"), VirtualBackgroundUi.Blur("blur"))),
                currentBackground = VirtualBackgroundUi.Blur("blur")),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithTag(VirtualBackgroundBlurOptionTag).assertIsSelected()
    }

    @Test
    fun testVirtualBackgroundImageOptionSelected() {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(VirtualBackgroundUiState(
                backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Image("image"), VirtualBackgroundUi.Blur("blur"))),
                currentBackground = VirtualBackgroundUi.Image("image")),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithTag(VirtualBackgroundImageOptionTag).assertIsSelected()
    }

    @Test
    fun testVirtualBackgroundNoneOptionSelected() {
        composeTestRule.setContent {
            VirtualBackgroundSettingsComponent(VirtualBackgroundUiState(
                backgroundList = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Image("image"), VirtualBackgroundUi.Blur("blur"))),
                currentBackground = VirtualBackgroundUi.None),
                { requestedVirtualBackgroundUi = it }
            )
        }

        composeTestRule.onNodeWithTag(VirtualBackgroundNoneOptionTag).assertIsSelected()
    }
}
