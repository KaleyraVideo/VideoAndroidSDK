package com.kaleyra.video_sdk.ui.call.bottomsheetm3

import com.kaleyra.video_sdk.call.bottomsheetm3.view.rowsCount
import org.junit.Assert
import org.junit.Test

class TableTests {

    @Test
    fun testTableRowsCount() {
        val rowsCount = rowsCount(6, 5)
        Assert.assertEquals(2, rowsCount)
    }
}