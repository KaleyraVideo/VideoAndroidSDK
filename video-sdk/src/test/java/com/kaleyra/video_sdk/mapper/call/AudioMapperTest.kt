package com.kaleyra.video_sdk.mapper.call

import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.AudioMapper.mapToAudioUi
import com.kaleyra.video_sdk.call.streamnew.model.core.AudioUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

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
    fun audioInputNotNull_mapToAudioUi_mappedAudioUi() = runTest {
        val flow = MutableStateFlow(audioMock)
        val actual = flow.mapToAudioUi().first()
        val expected = AudioUi(id = "audioId", isEnabled = true)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun audioInputEnabledUpdated_mapToAudioUi_isEnabledFlagUpdated() = runTest {
        val enabledFlow = MutableStateFlow(false)
        every { audioMock.enabled } returns enabledFlow
        val flow = MutableStateFlow(audioMock)

        val actual = flow.mapToAudioUi().first()
        val expected = AudioUi(id = "audioId", isEnabled = false)
        Assert.assertEquals(expected, actual)

        enabledFlow.value = true

        val new = flow.mapToAudioUi().first()
        val newExpected = AudioUi(id = "audioId", isEnabled = true)
        Assert.assertEquals(newExpected, new)
    }
}