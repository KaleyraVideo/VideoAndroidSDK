package com.kaleyra.video_common_ui.utils

import android.os.Bundle
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BundleUtilsTest {

    @Test
    fun testBundleToJSONObjectExtension() {
        val bundle = Bundle()
        bundle.putString("a", "a")
        bundle.putInt("b", 1)

        val json = bundle.toJSONObject()

        Assert.assertEquals("a", json.getString("a"))
        Assert.assertEquals(1, json.getInt("b"))
    }
}