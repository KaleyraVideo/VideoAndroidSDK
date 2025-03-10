package com.kaleyra.video_sdk.ui.call.stream

import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.model.core.VideoUi
import com.kaleyra.video_sdk.call.stream.utils.hasVideo
import com.kaleyra.video_sdk.call.stream.utils.isSpeaking
import com.kaleyra.video_sdk.call.stream.utils.isVideoEnabled
import com.kaleyra.video_sdk.call.stream.utils.isLocalScreenShare
import com.kaleyra.video_sdk.call.stream.utils.isMyCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteCameraStream
import com.kaleyra.video_sdk.call.stream.utils.isRemoteScreenShare
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.user.UserInfo
import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class StreamUIExtensionsTest {

    @Test
    fun `isSpeaking returns true when audio level is above zero`() {
        val streamUi = StreamUi(
            id = "1",
            audio = AudioUi(
                id = "a1",
                isEnabled = true,
                isSpeaking = true
            )
        )

        val result = streamUi.isSpeaking()

        Assert.assertTrue(result)
    }

    @Test
    fun `isSpeaking returns false when audio level is zero`() {
        val streamUi = StreamUi(
            id = "1",
            audio = AudioUi(
                id = "a1",
                isEnabled = true,
                isSpeaking = false
            )
        )

        val result = streamUi.isSpeaking()

        Assert.assertFalse(result)
    }

    @Test
    fun `isSpeaking returns false when audio is null`() {
        val streamUi = StreamUi(
            id = "1",
            audio = null
        )

        val result = streamUi.isSpeaking()

        Assert.assertFalse(result)
    }

    @Test
    fun `isSpeaking returns true when audio is disabled and audio level is above zero`() {
        val streamUi = StreamUi(
            id = "1",
            audio = AudioUi(
                id = "a1",
                isEnabled = false,
                isSpeaking = true
            )
        )

        val result = streamUi.isSpeaking()

        Assert.assertTrue(result)
    }

    @Test
    fun `hasVideo returns true when video is not null`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), video = VideoUi("1"))
        Assert.assertTrue(streamUi.hasVideo())
    }

    @Test
    fun `hasVideo returns false when video is null`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())))
        Assert.assertFalse(streamUi.hasVideo())
    }

    @Test
    fun `isVideoEnabled returns false when video is null`() {
        val streamUi = StreamUi("1", video = null)
        Assert.assertFalse(streamUi.isVideoEnabled())
    }

    @Test
    fun `isVideoEnabled returns true when video is not null and is enabled`() {
        val streamUi = StreamUi("1", video = VideoUi("v1", isEnabled = true))
        Assert.assertTrue(streamUi.isVideoEnabled())
    }

    @Test
    fun `isVideoEnabled returns false when video is not null and is not enabled`() {
        val streamUi = StreamUi("1", video = VideoUi("v1", isEnabled = false))
        Assert.assertFalse(streamUi.isVideoEnabled())
    }

    @Test
    fun `isRemoteScreenShare returns true when is not mine, has video, and is screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false, video = VideoUi("1", isScreenShare = true))
        Assert.assertTrue(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isRemoteScreenShare returns false when is mine`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isRemoteScreenShare returns false when video is null`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false)
        Assert.assertFalse(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isRemoteScreenShare returns false when not screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isRemoteScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns true when is mine, has video, and is screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true, video = VideoUi("1", isScreenShare = true))
        Assert.assertTrue(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns false when not mine`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns false when video is null`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true)
        Assert.assertFalse(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isLocalScreenShare returns false when not screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isLocalScreenShare())
    }

    @Test
    fun `isMyCameraStream returns true when is mine, has video, and is not screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true, video = VideoUi("1", isScreenShare = false))
        Assert.assertTrue(streamUi.isMyCameraStream())
    }

    @Test
    fun `isMyCameraStream returns false when not mine`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isMyCameraStream())
    }

    @Test
    fun `isMyCameraStream returns false when video is null`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true)
        Assert.assertFalse(streamUi.isMyCameraStream())
    }

    @Test
    fun `isMyCameraStream returns false when is screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isMyCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns true when not mine, has video, and is not screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false, video = VideoUi("1", isScreenShare = false))
        Assert.assertTrue(streamUi.isRemoteCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns false when is mine`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = true, video = VideoUi("1", isScreenShare = false))
        Assert.assertFalse(streamUi.isRemoteCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns false when video is null`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false)
        Assert.assertFalse(streamUi.isRemoteCameraStream())
    }

    @Test
    fun `isRemoteCameraStream returns false when is screen share`() {
        val streamUi = StreamUi("1", UserInfo("userId", "user1", ImmutableUri(mockk())), isMine = false, video = VideoUi("1", isScreenShare = true))
        Assert.assertFalse(streamUi.isRemoteCameraStream())
    }
}
