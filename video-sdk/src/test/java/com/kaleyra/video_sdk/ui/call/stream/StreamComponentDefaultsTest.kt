package com.kaleyra.video_sdk.ui.call.stream

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.stream.view.ThumbnailsArrangement
import com.kaleyra.video_sdk.call.stream.MaxFeaturedStreamsCompact
import com.kaleyra.video_sdk.call.stream.MaxFeaturedStreamsExpanded
import com.kaleyra.video_sdk.call.stream.StreamComponentDefaults
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class StreamComponentDefaultsTest {

    @Test
    fun windowWidthSizeClassCompact_maxFeaturedStreams_maxFeaturedStreamsCompact() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(100.dp, 600.dp))
        assertEquals(
            MaxFeaturedStreamsCompact,
            StreamComponentDefaults.maxFeaturedStreams(sizeClass)
        )
    }

    @Test
    fun windowHeightSizeClassCompact_maxFeaturedStreams_maxFeaturedStreamsCompact() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 100.dp))
        assertEquals(
            MaxFeaturedStreamsCompact,
            StreamComponentDefaults.maxFeaturedStreams(sizeClass)
        )
    }

    @Test
    fun windowSizeClassMedium_maxFeaturedStreams_maxFeaturedStreamsExpanded() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(600.dp, 480.dp))
        assertEquals(
            MaxFeaturedStreamsExpanded,
            StreamComponentDefaults.maxFeaturedStreams(sizeClass)
        )
    }

    @Test
    fun windowSizeClassExpanded_maxFeaturedStreams_maxFeaturedStreamsExpanded() {
        val sizeClass = WindowSizeClass.calculateFromSize(DpSize(840.dp, 900.dp))
        assertEquals(
            MaxFeaturedStreamsExpanded,
            StreamComponentDefaults.maxFeaturedStreams(sizeClass)
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