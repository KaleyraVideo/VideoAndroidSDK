package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.CameraMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.MicMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.viewmodel.InputMessageViewModel
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyCameraEnabled
import com.kaleyra.video_sdk.call.mapper.InputMapper.isMyMicEnabled
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
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

    @Before
    fun setup() {
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
    fun cameraEnabled_inputMessageFlow_InputMessageCameraEnabled() = runTest {
        every { call.isMyCameraEnabled() } returns MutableStateFlow(true)
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(CameraMessage.Enabled, inputMessage)
    }

    @Test
    fun cameraDisabled_inputMessageFlow_InputMessageCameraDisabled() = runTest {
        every { call.isMyCameraEnabled() } returns MutableStateFlow(false)
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(CameraMessage.Disabled, inputMessage)
    }

    @Test
    fun micEnabled_inputMessageFlow_InputMessageMicEnabled() = runTest {
        every { call.isMyMicEnabled() } returns MutableStateFlow(true)
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(MicMessage.Enabled, inputMessage)
    }

    @Test
    fun micDisabled_inputMessageFlow_InputMessageMicDisabled() = runTest {
        every { call.isMyMicEnabled() } returns MutableStateFlow(false)
        var inputMessage: InputMessage? = null
        viewModel.inputMessage.onEach { inputMessage = it }.launchIn(backgroundScope)
        advanceUntilIdle()
        Assert.assertEquals(MicMessage.Disabled, inputMessage)
    }
}