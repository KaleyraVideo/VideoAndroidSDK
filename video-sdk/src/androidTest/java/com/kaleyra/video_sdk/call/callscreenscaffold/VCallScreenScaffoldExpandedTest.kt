package com.kaleyra.video_sdk.call.callscreenscaffold

import org.junit.Assert
import org.junit.Test

internal class VCallScreenScaffoldExpandedTest : VCallScreenScaffoldBaseTest() {

    @Test
    fun testBrandLogoComposableCalledWithExpandedSizeClass() {
        var hasCalledBrandLogo = false
        composeTestRule.setCallScreenScaffold(
            brandLogo = {
                hasCalledBrandLogo = true
            }
        )

        composeTestRule.waitForIdle()

        Assert.assertEquals(true, hasCalledBrandLogo)
    }
}