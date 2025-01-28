package com.kaleyra.video_common_ui

import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Stream
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions
import com.kaleyra.video_extension_audio.extensions.CollaborationAudioExtensions.currentAudioOutputDevice
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StreamsAudioOutputManagerTest {

    private val callMock = mockk<Call>(relaxed = true)

    private val audioMock1 = mockk<Input.Audio>(relaxed = true)

    private val audioMock2 = mockk<Input.Audio>(relaxed = true)

    private val streamMock1 = mockk<Stream.Mutable>()

    private val streamMock2 = mockk<Stream.Mutable>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    @Before
    fun setUp() {
        mockkObject(CollaborationAudioExtensions)
        every { callMock.participants } returns MutableStateFlow(mockk {
            every { others } returns listOf(participantMock1, participantMock2)
        })
        every { participantMock1.streams } returns MutableStateFlow(listOf(streamMock1))
        every { participantMock2.streams } returns MutableStateFlow(listOf(streamMock2))
        every { streamMock1.audio } returns MutableStateFlow(audioMock1)
        every { streamMock2.audio } returns MutableStateFlow(audioMock2)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testBind() = runTest {
        val streamsAudioManager = spyk(StreamsAudioManager(backgroundScope))
        streamsAudioManager.bind(callMock)
        verifyOrder {
            streamsAudioManager.stop()
            streamsAudioManager.disableStreamAudioOnMute(callMock)
        }
    }

    @Test
    fun testStop() = runTest {
        val streamsAudioManager = spyk(StreamsAudioManager(this))
        streamsAudioManager.bind(callMock)
        streamsAudioManager.stop()
    }

    @Test
    fun testDisableStreamsAudioOnMute() = runTest(UnconfinedTestDispatcher()) {
        val deviceFlow = MutableStateFlow<AudioOutputDevice>(AudioOutputDevice.Loudspeaker())
        every { callMock.currentAudioOutputDevice } returns deviceFlow
        val streamsAudioManager = StreamsAudioManager(backgroundScope)
        streamsAudioManager.disableStreamAudioOnMute(callMock)
        deviceFlow.value = AudioOutputDevice.None()
        verify(exactly = 1) { audioMock1.tryDisable() }
        verify(exactly = 1) { audioMock2.tryDisable() }
    }

    @Test
    fun `restore participants audio if previous device was none`() = runTest(UnconfinedTestDispatcher()) {
        val deviceFlow = MutableStateFlow<AudioOutputDevice>(AudioOutputDevice.None())
        every { callMock.currentAudioOutputDevice } returns deviceFlow
        val streamsAudioManager = StreamsAudioManager(backgroundScope)
        streamsAudioManager.disableStreamAudioOnMute(callMock)
        deviceFlow.value = AudioOutputDevice.Loudspeaker()
        verify(exactly = 1) { audioMock1.tryEnable() }
        verify(exactly = 1) { audioMock2.tryEnable() }
    }
}