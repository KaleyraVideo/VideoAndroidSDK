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

package com.kaleyra.video_sdk.ui.call.snackbarm3

import androidx.activity.ComponentActivity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.common.snackbar.view.UserMessageSnackbarActionConfig
import com.kaleyra.video_sdk.common.snackbar.view.UserMessageSnackbarM3
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UserMessageSnackbarM3Test {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun testDismissClicked() {
        var dismissClicked = false
        composeTestRule.setContent {
            UserMessageSnackbarM3(
                iconPainter =  painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
                message = "title",
                onDismissClick = { dismissClicked = true }
            )
        }
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.kaleyra_strings_action_close)).performClick()
        Assert.assertEquals(true, dismissClicked)
    }

    @Test
    fun testActionClicked() {
        val actionTextRef = TextRef.StringResource(R.string.kaleyra_user_message_pin)
        val actionLabel = actionTextRef.resolve(composeTestRule.activity)
        var actionClicked = false
        composeTestRule.setContent {
            UserMessageSnackbarM3(
                iconPainter =  painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
                message = "title",
                onDismissClick = { },
                actionConfig = UserMessageSnackbarActionConfig(actionTextRef, onActionClick =  { actionClicked = true })
            )
        }
        composeTestRule.onNodeWithText(actionLabel).performClick()
        Assert.assertEquals(true, actionClicked)
    }
}