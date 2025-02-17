package com.kaleyra.video_sdk.layout

import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.utils.hasVideo
import com.kaleyra.video_sdk.call.stream.utils.isLocalScreenShare
import com.kaleyra.video_sdk.call.stream.utils.isMyCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import org.junit.Assert
import org.junit.Test

class StreamUIExtensionsTest {

    @Test
    fun `hasVideo returns true when video is not null`() {
        val streamUi = StreamUi("1", "user1", video = VideoUi("1"))
        Assert.assertTrue(streamUi.hasVideo())
    }

    @Test
    fun `hasVideo returns false when video is null`() {
        val streamUi = StreamUi("1", "user1")
        Assert.assertFalse(streamUi.hasVideo())
    }

    @Test
    fun `isRemoteScreenShare returns true when is not mine, has video, and is screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = false, video = VideoUi("1", isScreenShare = true))
        Assert.assertTrue(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isRemoteScreenShare returns false when is mine`() {
        val streamUi = StreamUi("1", "user1", isMine = true, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isRemoteScreenShare returns false when video is null`() {
        val streamUi = StreamUi("1", "user1", isMine = false)
        Assert.assertFalse(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isRemoteScreenShare returns false when not screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = false, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns true when is mine, has video, and is screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = true, video = VideoUi("1", isScreenShare = true))
        Assert.assertTrue(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns false when not mine`() {
        val streamUi = StreamUi("1", "user1", isMine = false, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns false when video is null`() {
        val streamUi = StreamUi("1", "user1", isMine = true)
        Assert.assertFalse(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns false when not screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = true, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isMyCameraStream returns true when is mine, has video, and is not screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = true, video = VideoUi("1", isScreenShare = false))
        Assert.assertTrue(streamUi.isMyCameraStream())
    }

    @Test
    fun `isMyCameraStream returns false when not mine`() {
        val streamUi = StreamUi("1", "user1", isMine = false, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isMyCameraStream())
    }

    @Test
    fun `isMyCameraStream returns false when video is null`() {
        val streamUi = StreamUi("1", "user1", isMine = true)
        Assert.assertFalse(streamUi.isMyCameraStream())
    }

    @Test
    fun `isMyCameraStream returns false when is screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = true, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isMyCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns true when not mine, has video, and is not screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = false, video = VideoUi("1", isScreenShare = false))
        Assert.assertTrue(streamUi.isRemoteCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns false when is mine`() {
        val streamUi = StreamUi("1", "user1", isMine = true, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isRemoteCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns false when video is null`() {
        val streamUi = StreamUi("1", "user1", isMine = false)
        Assert.assertFalse(streamUi.isRemoteCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns false when is screen share`() {
        val streamUi = StreamUi("1", "user1", isMine = false, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isRemoteCameraStream())
    }

}