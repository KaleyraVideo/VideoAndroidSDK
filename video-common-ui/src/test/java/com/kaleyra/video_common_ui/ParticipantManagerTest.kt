package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video_common_ui.call.ParticipantManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParticipantManagerTest {

    private val callMock = mockk<Call>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val participantMock1 = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { participantsMock.list } returns listOf(participantMock1, participantMock2)
        every { participantMock1.userId } returns "userId1"
        every { participantMock2.userId } returns "userId2"
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

//    @Test
//    fun testBind() = runTest {
//        val participantManager = spyk(ParticipantManager(backgroundScope))
//        participantManager.bind(callMock)
//        verifyOrder {
//            participantManager.stop()
//            participantManager.keepContactDetailsUpdated(callMock)
//        }
//    }

    @Test
    fun testStop() = runTest {
        val participantManager = spyk(ParticipantManager(backgroundScope))
        participantManager.bind(callMock)
        participantManager.stop()
    }

    @Test
    fun callParticipants_keepContactDetailsUpdated_contactDetailsAreUpdated() = runTest(UnconfinedTestDispatcher()) {
        val participantManager = ParticipantManager(backgroundScope)
        participantManager.keepContactDetailsUpdated(callMock)
        coVerify(exactly = 1) { ContactDetailsManager.refreshContactDetails(*arrayOf("userId1", "userId2")) }
    }
}