/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.view.Watermark
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WatermarkTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var watermarkInfo by mutableStateOf(WatermarkInfo(null, null))

    @Before
    fun setUp() {
        composeTestRule.setContent {
            Watermark(watermarkInfo = watermarkInfo)
        }
    }

    @After
    fun tearDown() {
        watermarkInfo = WatermarkInfo(null, null)
    }

    @Test
    fun logoWithValidUri_watermarkImageIsDisplayed() {
        val uri = Uri.parse("com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo")
        watermarkInfo = WatermarkInfo(logo = Logo(uri, uri))
        composeTestRule.onNodeWithContentDescription(findLogo()).assertIsDisplayed()
    }

    @Test
    fun logoWithEmptyUriAndTextNotNull_textIsDisplayed() {
        watermarkInfo = WatermarkInfo(logo = Logo(Uri.EMPTY, Uri.EMPTY), text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun logoNullAndTextNotNull_textIsDisplayed() {
        watermarkInfo = WatermarkInfo(logo = null, text = "text")
        composeTestRule.onNodeWithText("text").assertIsDisplayed()
    }

    @Test
    fun testLogoExpandsToMaxWidth() {
        val uri = Uri.parse("com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo_clipped")
        watermarkInfo = WatermarkInfo(logo = Logo(uri, uri))
        composeTestRule.onNodeWithContentDescription(findLogo()).assertWidthIsEqualTo(300.dp)
    }

    @Test
    fun testLogoExpandsToMaxHeight() {
        val uri = Uri.parse("com.kaleyra.collaboration_suite_phone_ui.test.R.drawable.kaleyra_logo")
        watermarkInfo = WatermarkInfo(logo = Logo(uri, uri))
        composeTestRule.onNodeWithContentDescription(findLogo()).assertHeightIsEqualTo(80.dp)
    }

    private fun findLogo() = composeTestRule.activity.getString(R.string.kaleyra_company_logo)
}