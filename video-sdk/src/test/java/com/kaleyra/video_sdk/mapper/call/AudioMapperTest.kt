package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_common_ui.call.CameraStreamConstants.CAMERA_STREAM_ID
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.mapper.InputMapper
import com.kaleyra.video_common_ui.mapper.InputMapper.toMyCameraStream
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.AudioMapper.mapToAudioUi
import com.kaleyra.video_sdk.call.mapper.AudioMapper.toMyCameraStreamAudioUi
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AudioMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val audioMock = mockk<Input.Audio>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        with(audioMock) {
            every { id } returns "audioId"
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
    }

    @Test
    fun audioInputNull_mapToAudioUi_null() = runTest {
        val actual = MutableStateFlow(null).mapToAudioUi().first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun audioInputNotNull_mapToAudioUi_mappedAudioUi() = runTest {
        val flow = MutableStateFlow(audioMock)
        val actual = flow.mapToAudioUi().first()
        val expected = AudioUi(id = "audioId", isEnabled = true)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun disabledAudioInput_mapToAudioUi_isMutedForYouIsTrue() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(Input.Enabled.None)
        val flow = MutableStateFlow(audioMock)
        val actual = flow.mapToAudioUi().first()
        val expected = AudioUi(id = "audioId", isEnabled = false, isMutedForYou = true)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun audioInputEnabledUpdated_mapToAudioUi_audioUiUpdated() = runTest {
        val enabledFlow: MutableStateFlow<Input.Enabled> = MutableStateFlow(Input.Enabled.None)
        every { audioMock.enabled } returns enabledFlow
        val flow = MutableStateFlow(audioMock)

        val actual = flow.mapToAudioUi().first()
        val expected = AudioUi(id = "audioId", isEnabled = false, isMutedForYou = true)
        Assert.assertEquals(expected, actual)

        enabledFlow.value = Input.Enabled.Both

        val new = flow.mapToAudioUi().first()
        val newExpected = AudioUi(id = "audioId", isEnabled = true, isMutedForYou = false)
        Assert.assertEquals(newExpected, new)
    }

    @Test
    fun audioLevel1f_mapToAudioUi_AudioUiLevel1f() = runTest {
        every { audioMock.level } returns MutableStateFlow(1f)
        val flow = MutableStateFlow(audioMock)
        val audioUi = flow.mapToAudioUi().first()
        Assert.assertEquals(1f, audioUi!!.level)
    }

    @Test
    fun cameraStreamAudioEnabled_toMyCameraAudioUi_cameraAudioUi() = runTest {
        mockkObject(InputMapper)
        val callMock = mockk<Call>(relaxed = true)
        val audioMock = mockk<Input.Audio>(relaxed = true)
        with(audioMock) {
            every { id } returns "audioId"
            every { enabled } returns MutableStateFlow(Input.Enabled.Both)
        }
        val stream = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(audioMock)
        }
        every { callMock.toMyCameraStream() } returns flowOf(stream)
        val actual = callMock.toMyCameraStreamAudioUi().first()
        val expected = AudioUi("audioId", isEnabled = true)
        Assert.assertEquals(expected, actual)
        unmockkObject(InputMapper)
    }

    @Test
    fun cameraStreamAudioDisabled_toMyCameraAudioUi_cameraAudioUi() = runTest {
        mockkObject(InputMapper)
        val callMock = mockk<Call>(relaxed = true)
        val audioMock = mockk<Input.Audio>(relaxed = true)
        with(audioMock) {
            every { id } returns "audioId"
            every { enabled } returns MutableStateFlow(Input.Enabled.None)
        }
        val stream = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(audioMock)
        }
        every { callMock.toMyCameraStream() } returns flowOf(stream)
        val actual = callMock.toMyCameraStreamAudioUi().first()
        val expected = AudioUi("audioId", isEnabled = false, isMutedForYou = true)
        Assert.assertEquals(expected, actual)
        unmockkObject(InputMapper)
    }

    @Test
    fun cameraStreamAudioEnabledLocally_toMyCameraAudioUi_cameraAudioUi() = runTest {
        mockkObject(InputMapper)
        val callMock = mockk<Call>(relaxed = true)
        val audioMock = mockk<Input.Audio>(relaxed = true)
        with(audioMock) {
            every { id } returns "audioId"
            every { enabled } returns MutableStateFlow(Input.Enabled.Local)
        }
        val stream = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(audioMock)
        }
        every { callMock.toMyCameraStream() } returns flowOf(stream)
        val actual = callMock.toMyCameraStreamAudioUi().first()
        val expected = AudioUi("audioId", isEnabled = false, isMutedForYou = false)
        Assert.assertEquals(expected, actual)
        unmockkObject(InputMapper)
    }

    @Test
    fun cameraStreamAudioEnabledRemotely_toMyCameraAudioUi_cameraAudioUi() = runTest {
        mockkObject(InputMapper)
        val callMock = mockk<Call>(relaxed = true)
        val audioMock = mockk<Input.Audio>(relaxed = true)
        with(audioMock) {
            every { id } returns "audioId"
            every { enabled } returns MutableStateFlow(Input.Enabled.Remote)
        }
        val stream = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(audioMock)
        }
        every { callMock.toMyCameraStream() } returns flowOf(stream)
        val actual = callMock.toMyCameraStreamAudioUi().first()
        val expected = AudioUi("audioId", isEnabled = true, isMutedForYou = true)
        Assert.assertEquals(expected, actual)
        unmockkObject(InputMapper)
    }

    @Test
    fun cameraStreamAudioNull_toMyCameraAudioUi_null() = runTest {
        mockkObject(InputMapper)
        val callMock = mockk<Call>(relaxed = true)
        val stream = mockk<Stream.Mutable>(relaxed = true) {
            every { id } returns CAMERA_STREAM_ID
            every { audio } returns MutableStateFlow(null)
        }
        every { callMock.toMyCameraStream() } returns flowOf(stream)
        val actual = callMock.toMyCameraStreamAudioUi().first()
        Assert.assertEquals(null, actual)
        unmockkObject(InputMapper)
    }
}
