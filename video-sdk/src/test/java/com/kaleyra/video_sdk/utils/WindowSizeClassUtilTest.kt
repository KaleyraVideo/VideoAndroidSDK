package com.kaleyra.video_sdk.utils

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.junit4.createComposeRule
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.hasCompactHeight
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.hasCompactWidth
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.hasExpandedWidth
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.hasMediumWidth
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isAtLeastMediumWidth
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isCompactInAnyDimension
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isLargeScreen
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class WindowSizeClassUtilTest {

    @Test
    fun compactSize_currentWindowAdaptiveInfo_compactSizeClass() {
        val configuration = Configuration().apply {
            screenWidthDp = 360
            screenHeightDp = 240
        }

        val info = currentWindowAdaptiveInfo(configuration)

        assertEquals(WindowWidthSizeClass.Compact, info.widthSizeClass)
        assertEquals(WindowHeightSizeClass.Compact, info.heightSizeClass)
    }

    @Test
    fun mediumSize_currentWindowAdaptiveInfo_mediumSizeClass() {
        val configuration = Configuration().apply {
            screenWidthDp = 600
            screenHeightDp = 480
        }

        val info = currentWindowAdaptiveInfo(configuration)

        assertEquals(WindowWidthSizeClass.Medium, info.widthSizeClass)
        assertEquals(WindowHeightSizeClass.Medium, info.heightSizeClass)
    }

    @Test
    fun expandedSize_currentWindowAdaptiveInfo_expandedSizeClass() {
        val configuration = Configuration().apply {
            screenWidthDp = 840
            screenHeightDp = 900
        }

        val info = currentWindowAdaptiveInfo(configuration)

        assertEquals(WindowWidthSizeClass.Expanded, info.widthSizeClass)
        assertEquals(WindowHeightSizeClass.Expanded, info.heightSizeClass)
    }

    @Test
    fun compactSizeWidth_isAtLeastMediumWidth_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Compact

        assertEquals(false, windowSizeClass.isAtLeastMediumWidth())
    }

    @Test
    fun mediumSizeWidth_isAtLeastMediumWidth_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Medium

        assertEquals(true, windowSizeClass.isAtLeastMediumWidth())
    }

    @Test
    fun expandedSizeWidth_isAtLeastMediumWidth_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Expanded

        assertEquals(true, windowSizeClass.isAtLeastMediumWidth())
    }

    @Test
    fun compactSizeHeight_hasCompactHeight_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Compact

        assertEquals(true, windowSizeClass.hasCompactHeight())
    }

    @Test
    fun mediumSizeHeight_hasCompactHeight_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Medium

        assertEquals(false, windowSizeClass.hasCompactHeight())
    }

    @Test
    fun expandedSizeHeight_hasCompactHeight_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Expanded

        assertEquals(false, windowSizeClass.hasCompactHeight())
    }

    @Test
    fun compactSizeWidth_hasCompactWidth_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Compact

        assertEquals(true, windowSizeClass.hasCompactWidth())
    }

    @Test
    fun mediumSizeWidth_hasCompactWidth_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Medium

        assertEquals(false, windowSizeClass.hasCompactWidth())
    }

    @Test
    fun expandedSizeWidth_hasCompactWidth_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Expanded

        assertEquals(false, windowSizeClass.hasCompactWidth())
    }

    @Test
    fun compactSizeWidth_hasMediumWidth_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Compact

        assertEquals(false, windowSizeClass.hasMediumWidth())
    }

    @Test
    fun mediumSizeWidth_hasMediumWidth_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Medium

        assertEquals(true, windowSizeClass.hasMediumWidth())
    }

    @Test
    fun expandedSizeWidth_hasMediumWidth_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Expanded

        assertEquals(false, windowSizeClass.hasMediumWidth())
    }

    @Test
    fun compactSizeWidth_hasExpandedWidth_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Compact

        assertEquals(false, windowSizeClass.hasExpandedWidth())
    }

    @Test
    fun mediumSizeWidth_hasExpandedWidth_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Medium

        assertEquals(false, windowSizeClass.hasExpandedWidth())
    }

    @Test
    fun expandedSizeWidth_hasExpandedWidth_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Expanded

        assertEquals(true, windowSizeClass.hasExpandedWidth())
    }

    @Test
    fun compactSizeWidth_isCompactInAnyDimension_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Expanded
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Compact

        assertEquals(true, windowSizeClass.isCompactInAnyDimension())
    }

    @Test
    fun compactSizeHeight_isCompactInAnyDimension_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Compact
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Expanded

        assertEquals(true, windowSizeClass.isCompactInAnyDimension())
    }

    @Test
    fun noCompactSize_isCompactInAnyDimension_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Expanded
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Medium

        assertEquals(false, windowSizeClass.isCompactInAnyDimension())
    }

    @Test
    fun compactSizeClass_isLargeScreen_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Compact
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Compact

        assertEquals(false, windowSizeClass.isLargeScreen())
    }

    @Test
    fun mediumSizeClass_isLargeScreen_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Medium
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Medium

        assertEquals(true, windowSizeClass.isLargeScreen())
    }

    @Test
    fun expandedSizeClass_isLargeScreen_true() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Expanded
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Expanded

        assertEquals(true, windowSizeClass.isLargeScreen())
    }

    @Test
    fun expandedSizeWidthAndCompactHeight_isLargeScreen_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Compact
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Expanded

        assertEquals(false, windowSizeClass.isLargeScreen())
    }

    @Test
    fun mediumSizeWidthAndCompactHeight_isLargeScreen_false() {
        val windowSizeClass = mockk<WindowSizeClass>()
        every { windowSizeClass.heightSizeClass } returns WindowHeightSizeClass.Compact
        every { windowSizeClass.widthSizeClass } returns WindowWidthSizeClass.Medium

        assertEquals(false, windowSizeClass.isLargeScreen())
    }

}