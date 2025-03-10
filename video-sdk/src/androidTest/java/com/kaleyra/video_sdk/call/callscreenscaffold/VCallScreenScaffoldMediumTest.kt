package com.kaleyra.video_sdk.call.callscreenscaffold

import org.junit.Assert.assertEquals
import org.junit.Test

internal class VCallScreenScaffoldMediumTest: VCallScreenScaffoldBaseTest() {

    @Test
    fun testBrandLogoComposableCalledWithLargeScreenAndSizeClassMedium() {
        var hasCalledBrandLogo = false
        composeTestRule.setCallScreenScaffold(
            brandLogo = {
                hasCalledBrandLogo = true
            }
        )

        composeTestRule.waitForIdle()

        assertEquals(true, hasCalledBrandLogo)
    }
}