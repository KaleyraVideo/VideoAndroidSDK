package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.AudioMapper.mapToAudioUi
import com.kaleyra.video_sdk.call.stream.model.AudioUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AudioMapperTest {

    private val audioMock = mockk<Input.Audio>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        with(audioMock) {
            every { id } returns "audioId"
            every { enabled } returns MutableStateFlow(true)
        }
    }

    @Test
    fun audioInputNull_mapToAudioUi_null() = runTest {
        val actual = MutableStateFlow(null).mapToAudioUi().first()
        Assert.assertEquals(null, actual)
    }

    @Test
    fun audioInputEnabledNotNull_mapToAudioUi_mappedAudioUiEnabled() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(true)
        val flow = MutableStateFlow(audioMock)
        val actual = flow.mapToAudioUi().first()
        val expected = AudioUi("audioId", isEnabled = true)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun audioInputDisabledNotNull_mapToAudioUi_mappedAudioUiDisabled() = runTest {
        every { audioMock.enabled } returns MutableStateFlow(false)
        val flow = MutableStateFlow(audioMock)
        val actual = flow.mapToAudioUi().first()
        val expected = AudioUi("audioId", isEnabled = false)
        Assert.assertEquals(expected, actual)
    }
}