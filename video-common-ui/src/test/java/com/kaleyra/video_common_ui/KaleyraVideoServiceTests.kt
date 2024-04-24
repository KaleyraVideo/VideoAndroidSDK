package com.kaleyra.video_common_ui

import com.kaleyra.video.State
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class KaleyraVideoServiceTests {

    @Test
    fun testConfigurationRequested() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns false
        requestConfiguration()
        coVerify { spyKaleyraVideoService.onRequestKaleyraVideoConfigure() }
    }

    @Test
    fun testKaleyraVideoAlreadyConfigured_configurationNotRequested() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns true
        requestConfiguration()
        coVerify(exactly = 0) { spyKaleyraVideoService.onRequestKaleyraVideoConfigure() }
    }

    @Test
    fun testConnectRequested() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        requestConnect("loggedUserId")
        coVerify { spyKaleyraVideoService.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoAlreadyConnected_connectNotRequested() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Connected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId"
        })
        requestConnect("loggedUserId")
        coVerify(exactly = 0) { spyKaleyraVideoService.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoAlreadyConnecting_connectNotRequested() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Connecting)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId"
        })
        requestConnect("loggedUserId")
        coVerify(exactly = 0) { spyKaleyraVideoService.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoNotConnected_connectRequested_connectNotCalled() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        val hasConnected = requestConnect("loggedUserId")
        Assert.assertEquals(false, hasConnected)
    }

    @Test
    fun testConnectRequested_otherUserConnected_false() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected) andThen MutableStateFlow(State.Connected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId2"
        })
        val hasConnected = requestConnect("loggedUserId")
        coVerify(exactly = 1) { spyKaleyraVideoService.onRequestKaleyraVideoConnect() }
        Assert.assertEquals(false, hasConnected)
        coVerify(exactly = 1) { KaleyraVideo.disconnect() }
    }

    @Test
    fun testConnectRequested_neverConnected_kaleyraVideoDisconnected() = runTest {
        val spyKaleyraVideoService = spyk<KaleyraVideoService>()
        mockkObject(KaleyraVideoService.Companion)
        coEvery { KaleyraVideoService.get() } returns spyKaleyraVideoService
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(null)
        val hasConnected = requestConnect("loggedUserId")
        coVerify(exactly = 1) { spyKaleyraVideoService.onRequestKaleyraVideoConnect() }
        Assert.assertEquals(false, hasConnected)
        coVerify(exactly = 1) { KaleyraVideo.disconnect() }
    }
}