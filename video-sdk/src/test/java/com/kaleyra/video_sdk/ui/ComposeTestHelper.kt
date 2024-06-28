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

package com.kaleyra.video_sdk.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.unit.Dp
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.kaleyra.video_sdk.R

internal fun SemanticsNodeInteraction.performDoubleClick() {
    performTouchInput {
        doubleClick()
    }
}

internal fun SemanticsNodeInteraction.performScrollUp() {
    performTouchInput {
        this.swipe(
            start = this.center,
            end = Offset(this.center.x, this.center.y + 500),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.performVerticalSwipe(value: Int) {
    performTouchInput {
        swipe(
            start = Offset(centerX, top),
            end = Offset(centerX, top + value),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.performHorizontalSwipe(value: Int) {
    performTouchInput {
        swipe(
            start = Offset(left, centerY),
            end = Offset(left + value, centerY),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.performVerticalSwipe(amount: Float) {
    performTouchInput {
        val startY = top
        val endY = top - amount * height
         swipe(
            start = Offset(center.x, startY),
            end = Offset(center.x, endY),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.performHorizontalSwipe(amount: Float) {
    performTouchInput {
        val startX = left
        val endX = left - amount * width
        swipe(
            start = Offset(startX, center.y),
            end = Offset(endX, center.y),
            durationMillis = 200
        )
    }
}

internal fun SemanticsNodeInteraction.assertLeftPositionInRootIsEqualTo(
    expectedRight: Dp,
    tolerance: Dp = Dp(.5f)
): SemanticsNodeInteraction {
    getUnclippedBoundsInRoot().left.assertIsEqualTo(expectedRight, "left", tolerance)
    return this
}

internal fun SemanticsNodeInteraction.assertTopPositionInRootIsEqualTo(
    expectedRight: Dp,
    tolerance: Dp = Dp(.5f)
): SemanticsNodeInteraction {
    getUnclippedBoundsInRoot().top.assertIsEqualTo(expectedRight, "top", tolerance)
    return this
}

internal fun SemanticsNodeInteraction.assertRightPositionInRootIsEqualTo(
    expectedRight: Dp,
    tolerance: Dp = Dp(.5f)
): SemanticsNodeInteraction {
    getUnclippedBoundsInRoot().right.assertIsEqualTo(expectedRight, "right", tolerance)
    return this
}

internal fun SemanticsNodeInteraction.assertBottomPositionInRootIsEqualTo(
    expectedBottom: Dp,
    tolerance: Dp = Dp(.5f)
): SemanticsNodeInteraction {
    getUnclippedBoundsInRoot().bottom.assertIsEqualTo(expectedBottom, "bottom", tolerance)
    return this
}

internal fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.findBackButton(): SemanticsNodeInteraction {
    val back = activity.getString(R.string.kaleyra_back)
    return onNodeWithContentDescription(back)
}
internal fun <T: ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<T>, T>.findAvatar(): SemanticsNodeInteraction {
    val avatar = activity.getString(R.string.kaleyra_avatar)
    return onNodeWithContentDescription(avatar)
}

internal inline fun <reified A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>.pressBack() {
    activityRule.scenario.onActivity { activity ->
        activity.onBackPressedDispatcher.onBackPressed()
    }
}