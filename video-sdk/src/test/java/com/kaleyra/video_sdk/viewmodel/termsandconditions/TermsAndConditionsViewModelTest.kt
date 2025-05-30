/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.viewmodel.termsandconditions

import com.kaleyra.video.State
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConversationUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.Mocks.conversationMock
import com.kaleyra.video_sdk.termsandconditions.viewmodel.TermsAndConditionsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TermsAndConditionsViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testTermsAndConditionsUiState_isConnectedUpdated() = runTest(UnconfinedTestDispatcher()) {
        every { conferenceMock.state } returns MutableStateFlow(State.Connecting)
        val viewModel = TermsAndConditionsViewModel { Configuration.Success(conferenceMock, conversationMock, mockk(relaxed = true), MutableStateFlow(mockk())) }
        val actual = viewModel.uiState.first()
        assertEquals(false, actual.isConnected)
        every { conferenceMock.state } returns MutableStateFlow(State.Connected)
        advanceUntilIdle()
        val new = viewModel.uiState.first()
        assertEquals(true, new.isConnected)
    }

    @Test
    fun testDecline() = runTest(UnconfinedTestDispatcher()) {
        every { conferenceMock.state } returns MutableStateFlow(State.Connected)
        val viewModel = TermsAndConditionsViewModel { Configuration.Success(conferenceMock, conversationMock, mockk(relaxed = true), MutableStateFlow(mockk())) }
        viewModel.decline()
        val actual = viewModel.uiState.first()
        assertEquals(true, actual.isDeclined)
    }
}