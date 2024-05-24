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

package com.kaleyra.video_sdk.ui.call.pointer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.kaleyra.video_sdk.call.pointer.model.PointerUi
import com.kaleyra.video_sdk.call.pointer.view.MovablePointerTag
import com.kaleyra.video_sdk.call.pointer.view.PointerStreamWrapper
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PointerStreamWrapperTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val pointerList = ImmutableList(
        listOf(
            PointerUi("username1", 10f, 10f),
            PointerUi("username2", 20f, 20f),
            PointerUi("username3", 30f, 30f),
        )
    )

    @Before
    fun setUp() {
        composeTestRule.setContent {
            PointerStreamWrapper(streamView = null , pointerList = pointerList) {
                Spacer(modifier = Modifier.testTag("ChildTag"))
            }
        }
    }

    @Test
    fun streamComposableDoesExists() = runTest {
        composeTestRule.onNodeWithTag("ChildTag").assertExists()
    }

    @Test
    fun checkPointerLayersCount() {
        composeTestRule.onAllNodesWithTag(MovablePointerTag).assertCountEquals(pointerList.count())
    }


}