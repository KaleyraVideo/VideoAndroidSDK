package com.kaleyra.video_sdk.mapper.call

import androidx.compose.runtime.MutableState
import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.mapper.AudioMapper.mapToAudioUi
import com.kaleyra.video_sdk.call.stream.model.core.AudioUi
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
}