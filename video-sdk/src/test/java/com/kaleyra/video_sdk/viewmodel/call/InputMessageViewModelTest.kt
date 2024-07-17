package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.CameraMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.MicMessage
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyMicEnabled
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InputMessageViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: InputMessageViewModel

    private val call = mockk<CallUI>(relaxed = true)

    private val conference = mockk<ConferenceUI>()

    private val preferredType = MutableStateFlow(Call.PreferredType.audioVideo())

    @Before
    fun setup() {
        every { call.state } returns MutableStateFlow(Call.State.Connected)
        every { call.preferredType } returns preferredType
        viewModel = InputMessageViewModel {
            CollaborationViewModel.Configuration.Success(conference, mockk(), mockk(relaxed = true), MutableStateFlow(mockk()))
        }
        mockkObject(com.kaleyra.video_sdk.call.mapper.InputMapper)
        with(conference) {
            every { call } returns MutableSharedFlow<CallUI>(replay = 1).apply {
                tryEmit(this@InputMessageViewModelTest.call)
            }
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun cameraEnabledOnCallDisconnecting_inputMessageFlow_InputMessagesDropped() = runTest {
        every { call.state } returns MutableStateFlow(Call.State.Disconnecting)
        every { call.isMyCameraEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }

    @Test
    fun cameraEnabledOnCallDisconnected_inputMessageFlow_InputMessagesDropped() = runTest {
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { call.isMyCameraEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }

    @Test
    fun microphoneEnabledOnCallDisconnecting_inputMessageFlow_InputMessagesDropped() = runTest {
        every { call.state } returns MutableStateFlow(Call.State.Disconnecting)
        every { call.isMyMicEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }

    @Test
    fun microphoneEnabledOnCallDisconnected_inputMessageFlow_InputMessagesDropped() = runTest {
        every { call.state } returns MutableStateFlow(Call.State.Disconnected)
        every { call.isMyMicEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }

    @Test
    fun cameraEnabledFirstTime_inputMessageFlow_InputMessagesDropped() = runTest {
        every { call.isMyCameraEnabled() } returns flow {
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }

    @Test
    fun microphoneEnabledFirstTime_inputMessageFlow_InputMessagesDropped() = runTest {
        every { call.isMyMicEnabled() } returns flow {
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }

    @Test
    fun cameraEnabled_inputMessageFlow_InputMessageCameraEnabled() = runTest {
        every { call.isMyCameraEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(CameraMessage.Enabled, inputMessage)
    }

    @Test
    fun cameraDisabled_inputMessageFlow_InputMessageCameraDisabled() = runTest {
        every { call.isMyCameraEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(CameraMessage.Disabled, inputMessage)
    }

    @Test
    fun micEnabled_inputMessageFlow_InputMessageMicEnabled() = runTest {
        every { call.isMyMicEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
            emit(true)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(MicMessage.Enabled, inputMessage)
    }

    @Test
    fun micDisabled_inputMessageFlow_InputMessageMicDisabled() = runTest {
        every { call.isMyMicEnabled() } returns flow {
            emit(false)
            emit(true)
            emit(false)
        }
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(MicMessage.Disabled, inputMessage)
    }

    @Test
    fun cameraEnabledAfterPreferredTypeUpdated_inputMessageFlow_InputMessageCameraDropped() = runTest {
        val preferredType = MutableStateFlow(Call.PreferredType.audioUpgradable())
        every { call.preferredType } returns preferredType
        every { call.isMyCameraEnabled() } returns flow {
            emit(false)
            emit(true)
        }
        preferredType.emit(Call.PreferredType.audioVideo())
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }

    @Test
    fun microphoneEnabledAfterPreferredTypeUpdated_inputMessageFlow_InputMessageMicrophoneDropped() = runTest {
        val preferredType = MutableStateFlow(Call.PreferredType.audioUpgradable())
        every { call.preferredType } returns preferredType
        every { call.isMyMicEnabled() } returns flow {
            emit(false)
            emit(true)
        }
        preferredType.emit(Call.PreferredType.audioVideo())
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(null, inputMessage)
    }
}