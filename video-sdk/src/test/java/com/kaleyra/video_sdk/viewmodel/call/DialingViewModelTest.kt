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

package com.kaleyra.video_sdk.viewmodel.call

import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_sdk.call.dialing.view.DialingUiState
import com.kaleyra.video_sdk.call.dialing.viewmodel.DialingViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class DialingViewModelTest: PreCallViewModelTest<DialingViewModel, DialingUiState>() {

    @Before
    override fun setUp() {
        super.setUp()
        viewModel = spyk(DialingViewModel { Configuration.Success(conferenceMock, mockk(), companyMock, MutableStateFlow(mockk())) })
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun testPreCallUiState_isConnectingUpdated() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1)
        every { participantMock1.state } returns MutableStateFlow(CallParticipant.State.InCall)
        val current = viewModel.uiState.first().isConnecting
        Assert.assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isConnecting
        Assert.assertEquals(true, new)
    }
}