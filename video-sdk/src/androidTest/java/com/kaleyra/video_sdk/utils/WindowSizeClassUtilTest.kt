package com.kaleyra.video_sdk.utils

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.test.junit4.createComposeRule
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.currentWindowAdaptiveInfo
import com.kaleyra.video_sdk.utils.WindowSizeClassUtil.isAtLeastMediumWidth
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WindowSizeClassUtilTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun compactSize_currentWindowAdaptiveInfo_compactSizeClass() {
        composeTestRule.setContent {
            val configuration = Configuration().apply {
                screenWidthDp = 360
                screenHeightDp = 240
            }

            val info = currentWindowAdaptiveInfo(configuration)

            assertEquals(WindowWidthSizeClass.Compact, info.widthSizeClass)
            assertEquals(WindowHeightSizeClass.Compact, info.heightSizeClass)
        }
    }

    @Test
    fun mediumSize_currentWindowAdaptiveInfo_mediumSizeClass() {
        composeTestRule.setContent {
            val configuration = Configuration().apply {
                screenWidthDp = 600
                screenHeightDp = 480
            }

            val info = currentWindowAdaptiveInfo(configuration)

            assertEquals(WindowWidthSizeClass.Medium, info.widthSizeClass)
            assertEquals(WindowHeightSizeClass.Medium, info.heightSizeClass)
        }
    }

    @Test
    fun expandedSize_currentWindowAdaptiveInfo_expandedSizeClass() {
        composeTestRule.setContent {
            val configuration = Configuration().apply {
                screenWidthDp = 840
                screenHeightDp = 900
            }

            val info = currentWindowAdaptiveInfo(configuration)

            assertEquals(WindowWidthSizeClass.Expanded, info.widthSizeClass)
            assertEquals(WindowHeightSizeClass.Expanded, info.heightSizeClass)
        }
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
}