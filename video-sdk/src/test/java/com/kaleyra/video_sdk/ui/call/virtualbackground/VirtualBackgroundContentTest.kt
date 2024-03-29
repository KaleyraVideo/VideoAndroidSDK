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

package com.kaleyra.video_sdk.ui.call.virtualbackground

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.view.VirtualBackgroundContent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class VirtualBackgroundContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private var items by mutableStateOf(ImmutableList(listOf<VirtualBackgroundUi>()))

    private var backgroundClicked: VirtualBackgroundUi? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            VirtualBackgroundContent(
                items = items,
                currentBackground = VirtualBackgroundUi.None,
                onItemClick = { backgroundClicked = it }
            )
        }
    }

    @After
    fun tearDown() {
        items = ImmutableList(listOf())
        backgroundClicked = null
    }

    @Test
    fun noVirtualBackground_noVirtualBackgroundItemDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundUi.None))
        val none = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_none)
        composeTestRule.onNodeWithText(none).assertIsDisplayed()
    }

    @Test
    fun blurVirtualBackground_blurVirtualBackgroundItemDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundUi.Blur("id")))
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).assertIsDisplayed()
    }

    @Test
    fun imageVirtualBackground_imageVirtualBackgroundItemDisplayed() {
        items = ImmutableList(listOf(VirtualBackgroundUi.Image("id")))
        val image = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_image)
        composeTestRule.onNodeWithText(image).assertIsDisplayed()
    }

    @Test
    fun userClicksOnItem_onItemClickInvoked() {
        items = ImmutableList(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("id")))
        val blur = composeTestRule.activity.getString(R.string.kaleyra_virtual_background_blur)
        composeTestRule.onNodeWithText(blur).performClick()
        Assert.assertEquals(VirtualBackgroundUi.Blur("id"), backgroundClicked)
    }
}

