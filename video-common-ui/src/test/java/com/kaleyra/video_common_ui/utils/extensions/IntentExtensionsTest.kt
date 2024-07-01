package com.kaleyra.video_common_ui.utils.extensions

import android.content.Intent
import com.kaleyra.video_common_ui.NavBackComponent
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class IntentExtensionsTest {

    @Test
    fun testCallNavBackComponentFlagAdded() {
        val intent = Intent()

        intent.addBackButtonFlag(NavBackComponent.CALL)

        Assert.assertEquals(NavBackComponent.CALL.name, intent.getStringExtra(KALEYRA_NAV_BACK_KEY))
    }

    @Test
    fun testChatNavBackComponentFlagAdded() {
        val intent = Intent()

        intent.addBackButtonFlag(NavBackComponent.CHAT)

        Assert.assertEquals(NavBackComponent.CHAT.name, intent.getStringExtra(KALEYRA_NAV_BACK_KEY))
    }
}
