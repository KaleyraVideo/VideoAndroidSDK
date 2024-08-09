package com.kaleyra.video_sdk.ui.call.usermessage.model

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.platform.AccessibilityManager
import com.kaleyra.video_sdk.common.snackbar.model.toMillis
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert
import org.junit.Test

class SnackbarDurationExtTest {

    @Test
    fun testSnackbarDurationLong_noAccessibilityManager_ToMillis() {
        val snackbarDuration = SnackbarDuration.Long
        val millis = snackbarDuration.toMillis(true, null)
        Assert.assertEquals(10000L, millis)
    }

    @Test
    fun testSnackbarDurationShort_noAccessibilityManager_ToMillis() {
        val snackbarDuration = SnackbarDuration.Short
        val millis = snackbarDuration.toMillis(true, null)
        Assert.assertEquals(4000L, millis)
    }

    @Test
    fun testSnackbarDurationIndefinite_noAccessibilityManager_ToMillis() {
        val snackbarDuration = SnackbarDuration.Indefinite
        val millis = snackbarDuration.toMillis(true, null)
        Assert.assertEquals(Long.MAX_VALUE, millis)
    }

    @Test
    fun testSnackbarDurationLongAndAction_withAccessibilityManager_calculateRecommendedTimeoutMillisCalled() {
        val snackbarDuration = SnackbarDuration.Long
        val accessibilityManager = mockk<AccessibilityManager>(relaxed = true)
        snackbarDuration.toMillis(true, accessibilityManager)
        verify {
            accessibilityManager.calculateRecommendedTimeoutMillis(10000L, true, true, containsControls = true)
        }
    }

    @Test
    fun testSnackbarDurationShortAndAction_withAccessibilityManager_calculateRecommendedTimeoutMillisCalled() {
        val snackbarDuration = SnackbarDuration.Short
        val accessibilityManager = mockk<AccessibilityManager>(relaxed = true)
        snackbarDuration.toMillis(true, accessibilityManager)
        verify {
            accessibilityManager.calculateRecommendedTimeoutMillis(4000L, true, true, containsControls = true)
        }
    }

    @Test
    fun testSnackbarDurationIndefiniteAndAction_withAccessibilityManager_calculateRecommendedTimeoutMillisCalled() {
        val snackbarDuration = SnackbarDuration.Indefinite
        val accessibilityManager = mockk<AccessibilityManager>(relaxed = true)
        snackbarDuration.toMillis(true, accessibilityManager)
        verify {
            accessibilityManager.calculateRecommendedTimeoutMillis(Long.MAX_VALUE, true, true, containsControls = true)
        }
    }

    @Test
    fun testSnackbarDurationLongAndNoAction_withAccessibilityManager_calculateRecommendedTimeoutMillisCalled() {
        val snackbarDuration = SnackbarDuration.Long
        val accessibilityManager = mockk<AccessibilityManager>(relaxed = true)
        snackbarDuration.toMillis(false, accessibilityManager)
        verify {
            accessibilityManager.calculateRecommendedTimeoutMillis(10000L, true, true, containsControls = false)
        }
    }

    @Test
    fun testSnackbarDurationShortAndNoAction_withAccessibilityManager_calculateRecommendedTimeoutMillisCalled() {
        val snackbarDuration = SnackbarDuration.Short
        val accessibilityManager = mockk<AccessibilityManager>(relaxed = true)
        snackbarDuration.toMillis(false, accessibilityManager)
        verify {
            accessibilityManager.calculateRecommendedTimeoutMillis(4000L, true, true, containsControls = false)
        }
    }

    @Test
    fun testSnackbarDurationIndefiniteAndNoAction_withAccessibilityManager_calculateRecommendedTimeoutMillisCalled() {
        val snackbarDuration = SnackbarDuration.Indefinite
        val accessibilityManager = mockk<AccessibilityManager>(relaxed = true)
        snackbarDuration.toMillis(false, accessibilityManager)
        verify {
            accessibilityManager.calculateRecommendedTimeoutMillis(Long.MAX_VALUE, true, true, containsControls = false)
        }
    }
}
