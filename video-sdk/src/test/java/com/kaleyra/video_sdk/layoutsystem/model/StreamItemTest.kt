package com.kaleyra.video_sdk.layoutsystem.model

import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItem
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.layoutsystem.model.StreamItemState
import org.junit.Assert
import org.junit.Test

class StreamItemTest {

    @Test
    fun `isPinned returns true when StreamItem is Stream and state is Featured Pinned`() {
        val streamItem = StreamItem.Stream(
            id = "1",
            stream = StreamUi("1", "user1"),
            state = StreamItemState.Featured.Pinned
        )
        Assert.assertTrue(streamItem.isPinned())
    }

    @Test
    fun `isPinned returns false when StreamItem is Stream and state is not Featured Pinned`() {
        val streamItem = StreamItem.Stream(
            id = "1",
            stream = StreamUi("1", "user1"),
            state = StreamItemState.Standard
        )
        Assert.assertFalse(streamItem.isPinned())
    }

    @Test
    fun `isPinned returns false when StreamItem is HiddenStreams`() {
        val streamItem = StreamItem.MoreStreams(users = emptyList())
        Assert.assertFalse(streamItem.isPinned())
    }

    @Test
    fun `isFullscreen returns true when StreamItem is Stream and state is Featured Fullscreen`() {
        val streamItem = StreamItem.Stream(
            id = "1",
            stream = StreamUi("1", "user1"),
            state = StreamItemState.Featured.Fullscreen
        )
        Assert.assertTrue(streamItem.isFullscreen())
    }

    @Test
    fun `isFullscreen returns false when StreamItem is Stream and state is not Featured Fullscreen`() {
        val streamItem = StreamItem.Stream(
            id = "1",
            stream = StreamUi("1", "user1"),
            state = StreamItemState.Standard
        )
        Assert.assertFalse(streamItem.isFullscreen())
    }

    @Test
    fun `isFullscreen returns false when StreamItem is HiddenStreams`() {
        val streamItem = StreamItem.MoreStreams(users = emptyList())
        Assert.assertFalse(streamItem.isFullscreen())
    }

    @Test
    fun `hasVideoEnabled returns true when StreamItem is Stream and video isEnabled is true`() {
        val streamItem = StreamItem.Stream(
            id = "1",
            stream = StreamUi("1", "user1", video = VideoUi(id = "1", isEnabled = true))
        )
        Assert.assertTrue(streamItem.hasVideoEnabled())
    }

    @Test
    fun `hasVideoEnabled returns false when StreamItem is Stream and video isEnabled is false`() {
        val streamItem = StreamItem.Stream(
            id = "1",
            stream = StreamUi("1", "user1", video = VideoUi(id = "1", isEnabled = false))
        )
        Assert.assertFalse(streamItem.hasVideoEnabled())
    }

    @Test
    fun `hasVideoEnabled returns false when StreamItem is HiddenStreams`() {
        val streamItem = StreamItem.MoreStreams(users = emptyList())
        Assert.assertFalse(streamItem.hasVideoEnabled())
    }
}