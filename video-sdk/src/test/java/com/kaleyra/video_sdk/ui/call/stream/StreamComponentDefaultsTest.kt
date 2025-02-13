package com.kaleyra.video_sdk.ui.call.stream

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.stream.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxMosaicStreamsCompact
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxMosaicStreamsExpanded
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxPinnedStreamsCompact
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults.MaxPinnedStreamsExpanded
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class StreamComponentDefaultsTest {

    @Test
    fun windowWidthSizeClassCompact_maxPinnedStreamsFor_maxMosaicStreamsCompact() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(100.dp, 600.dp))
        assertEquals(
            MaxPinnedStreamsCompact,
            StreamComponentDefaults.maxPinnedStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowHeightSizeClassCompact_maxPinnedStreamsFor_maxMosaicStreamsCompact() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 100.dp))
        assertEquals(
            MaxPinnedStreamsCompact,
            StreamComponentDefaults.maxPinnedStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowSizeClassMedium_maxPinnedStreamsFor_maxMosaicStreamsExpanded() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 480.dp))
        assertEquals(
            MaxPinnedStreamsExpanded,
            StreamComponentDefaults.maxPinnedStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowSizeClassExpanded_maxPinnedStreamsFor_maxMosaicStreamsExpanded() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 900.dp))
        assertEquals(
            MaxPinnedStreamsExpanded,
            StreamComponentDefaults.maxPinnedStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowWidthSizeClassCompact_maxMosaicStreamsFor_maxMosaicStreamsCompact() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(100.dp, 600.dp))
        assertEquals(
            MaxMosaicStreamsCompact,
            StreamComponentDefaults.maxMosaicStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowHeightSizeClassCompact_maxMosaicStreamsFor_maxMosaicStreamsCompact() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 100.dp))
        assertEquals(
            MaxMosaicStreamsCompact,
            StreamComponentDefaults.maxMosaicStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowSizeClassMedium_maxMosaicStreamsFor_maxMosaicStreamsExpanded() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 480.dp))
        assertEquals(
            MaxMosaicStreamsExpanded,
            StreamComponentDefaults.maxMosaicStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowSizeClassExpanded_maxMosaicStreamsFor_maxMosaicStreamsExpanded() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 900.dp))
        assertEquals(
            MaxMosaicStreamsExpanded,
            StreamComponentDefaults.maxMosaicStreamsFor(sizeClass)
        )
    }

    @Test
    fun windowHeightSizeClassCompact_thumbnailsArrangementFor_thumbnailsArrangementEnd() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 100.dp))
        assertEquals(
            ThumbnailsArrangement.End,
            StreamComponentDefaults.thumbnailsArrangementFor(sizeClass)
        )
    }

    @Test
    fun windowWidthSizeClassMedium_thumbnailsArrangementFor_thumbnailsArrangementStart() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 480.dp))
        assertEquals(
            ThumbnailsArrangement.Start,
            StreamComponentDefaults.thumbnailsArrangementFor(sizeClass)
        )
    }

    @Test
    fun windowHeightSizeClassMedium_thumbnailsArrangementFor_thumbnailsArrangementBottom() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(100.dp, 480.dp))
        assertEquals(
            ThumbnailsArrangement.Bottom,
            StreamComponentDefaults.thumbnailsArrangementFor(sizeClass)
        )
    }

    @Test
    fun windowWidthSizeClassExpanded_thumbnailsArrangementFor_thumbnailsArrangementStart() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 480.dp))
        assertEquals(
            ThumbnailsArrangement.Start,
            StreamComponentDefaults.thumbnailsArrangementFor(sizeClass)
        )
    }
}