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

package com.kaleyra.video_sdk

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.utils.BottomSheetStateExtensions.TargetStateFractionThreshold
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetState
import com.kaleyra.video_sdk.call.bottomsheet.BottomSheetValue
import com.kaleyra.video_sdk.call.utils.BottomSheetStateExtensions.isCollapsed
import com.kaleyra.video_sdk.call.utils.BottomSheetStateExtensions.isCollapsing
import com.kaleyra.video_sdk.call.utils.BottomSheetStateExtensions.isHalfExpanding
import com.kaleyra.video_sdk.call.utils.BottomSheetStateExtensions.isHidden
import com.kaleyra.video_sdk.call.utils.BottomSheetStateExtensions.isNotDraggableDown
import com.kaleyra.video_sdk.call.utils.BottomSheetStateExtensions.isSheetFullScreen
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalMaterialApi::class)
class BottomSheetStateExtensionsTest {

    private val bottomSheetStateMock = mockk<BottomSheetState>(relaxed = true)

    @Test
    fun sheetHiddenAndFractionIs1_isHidden_true() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Hidden
        every { bottomSheetStateMock.progress.fraction } returns 1f
        assertEquals(true, bottomSheetStateMock.isHidden().value)
    }

    @Test
    fun sheetHiddenAndFractionIsLessThan1_isHidden_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Hidden
        every { bottomSheetStateMock.progress.fraction } returns .5f
        assertEquals(false, bottomSheetStateMock.isHidden().value)
    }

    @Test
    fun sheetCollapsed_isHidden_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Collapsed
        assertEquals(false, bottomSheetStateMock.isHidden().value)
    }

    @Test
    fun sheetHalfExpanded_isHidden_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.HalfExpanded
        assertEquals(false, bottomSheetStateMock.isHidden().value)
    }

    @Test
    fun sheetExpanded_isHidden_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Expanded
        assertEquals(false, bottomSheetStateMock.isHidden().value)
    }

    @Test
    fun sheetCollapsedAndFractionIs1_isCollapsed_true() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Collapsed
        every { bottomSheetStateMock.progress.fraction } returns 1f
        assertEquals(true, bottomSheetStateMock.isCollapsed().value)
    }

    @Test
    fun sheetCollapsedAndFractionIsLessThan1_isCollapsed_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Collapsed
        every { bottomSheetStateMock.progress.fraction } returns .5f
        assertEquals(false, bottomSheetStateMock.isCollapsed().value)
    }

    @Test
    fun sheetHidden_isCollapsed_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Hidden
        assertEquals(false, bottomSheetStateMock.isCollapsed().value)
    }

    @Test
    fun sheetHalfExpanded_isCollapsed_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.HalfExpanded
        assertEquals(false, bottomSheetStateMock.isCollapsed().value)
    }

    @Test
    fun sheetExpanded_isCollapsed_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Expanded
        assertEquals(false, bottomSheetStateMock.isCollapsed().value)
    }

    @Test
    fun sheetHidden_isNotDraggableDown_true() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Hidden
        assertEquals(true, bottomSheetStateMock.isNotDraggableDown().value)
    }

    @Test
    fun sheetCollapsed_isNotDraggableDown_true() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Collapsed
        assertEquals(true, bottomSheetStateMock.isNotDraggableDown().value)
    }

    @Test
    fun sheetHalfExpandedAndNotCollapsable_isNotDraggableDown_true() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.HalfExpanded
        every { bottomSheetStateMock.isCollapsable } returns false
        assertEquals(true, bottomSheetStateMock.isNotDraggableDown().value)
    }

    @Test
    fun sheetHalfExpandedAndCollapsable_isNotDraggableDown_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.HalfExpanded
        every { bottomSheetStateMock.isCollapsable } returns true
        assertEquals(false, bottomSheetStateMock.isNotDraggableDown().value)
    }

    @Test
    fun sheetExpanded_isNotDraggableDown_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Expanded
        assertEquals(false, bottomSheetStateMock.isNotDraggableDown().value)
    }

    @Test
    fun sheetCollapsedAndOverCollapsingFractionThreshold_isCollapsing_true() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Collapsed
        every { bottomSheetStateMock.progress.fraction } returns TargetStateFractionThreshold
        assertEquals(true, bottomSheetStateMock.isCollapsing().value)
    }

    @Test
    fun sheetCollapsedAndUnderCollapsingFractionThreshold_isCollapsing_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Collapsed
        every { bottomSheetStateMock.progress.fraction } returns TargetStateFractionThreshold - .5f
        assertEquals(false, bottomSheetStateMock.isCollapsing().value)
    }

    @Test
    fun sheetHalfExpanded_isCollapsing_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.HalfExpanded
        assertEquals(false, bottomSheetStateMock.isCollapsing().value)
    }

    @Test
    fun sheetExpanded_isCollapsing_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Expanded
        assertEquals(false, bottomSheetStateMock.isCollapsing().value)
    }

    @Test
    fun sheetHidden_isCollapsing_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Hidden
        assertEquals(false, bottomSheetStateMock.isCollapsing().value)
    }

    @Test
    fun sheetHalfExpandedAndOverHalfExpandingFractionThreshold_isHalfExpanding_true() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.HalfExpanded
        every { bottomSheetStateMock.progress.fraction } returns TargetStateFractionThreshold
        assertEquals(true, bottomSheetStateMock.isHalfExpanding().value)
    }

    @Test
    fun sheetHalfExpandedAndUnderHalfExpandingFractionThreshold_isHalfExpanding_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.HalfExpanded
        every { bottomSheetStateMock.progress.fraction } returns TargetStateFractionThreshold - .5f
        assertEquals(false, bottomSheetStateMock.isHalfExpanding().value)
    }

    @Test
    fun sheetCollapsed_isHalfExpanding_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Collapsed
        assertEquals(false, bottomSheetStateMock.isHalfExpanding().value)
    }

    @Test
    fun sheetExpanded_isHalfExpanding_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Expanded
        assertEquals(false, bottomSheetStateMock.isHalfExpanding().value)
    }

    @Test
    fun sheetHidden_isHalfExpanding_false() {
        every { bottomSheetStateMock.targetValue } returns BottomSheetValue.Hidden
        assertEquals(false, bottomSheetStateMock.isHalfExpanding().value)
    }

    @Test
    fun sheetOffsetGreaterThanThreshold_isSheetFullScreen_false() {
        val densityMock = mockk<Density>()
        val offsetThreshold = 100.dp
        with(densityMock) {
            every { offsetThreshold.toPx() } returns 100f
        }
        every { bottomSheetStateMock.offset.value } returns 150f
        assertEquals(false, bottomSheetStateMock.isSheetFullScreen(offsetThreshold, densityMock).value)
    }

    @Test
    fun sheetOffsetGreaterThanThreshold_isSheetFullScreen_true() {
        val densityMock = mockk<Density>()
        val offsetThreshold = 100.dp
        with(densityMock) {
            every { offsetThreshold.toPx() } returns 100f
        }
        every { bottomSheetStateMock.offset.value } returns 50f
        assertEquals(true, bottomSheetStateMock.isSheetFullScreen(offsetThreshold, densityMock).value)
    }
}