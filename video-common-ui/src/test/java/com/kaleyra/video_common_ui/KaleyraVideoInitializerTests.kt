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

class KaleyraVideoInitializerTests {

    @Test
    fun testConfigurationRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializer.instance = spyKaleyraVideoInitializer
        mockkObject(KaleyraVideoInitializer.Companion)
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns false
        requestConfiguration()
        coVerify { spyKaleyraVideoInitializer.onRequestKaleyraVideoConfigure() }
    }

    @Test
    fun testKaleyraVideoAlreadyConfigured_configurationNotRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializer.instance = spyKaleyraVideoInitializer
        mockkObject(KaleyraVideoInitializer.Companion)
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.isConfigured } returns true
        requestConfiguration()
        coVerify(exactly = 0) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConfigure() }
    }

    @Test
    fun testConnectRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializer.instance = spyKaleyraVideoInitializer
        mockkObject(KaleyraVideoInitializer.Companion)
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        requestConnect("loggedUserId")
        coVerify { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoAlreadyConnected_connectNotRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer> {
            every { KaleyraVideoInitializer.instance } returns this
        }
        mockkObject(KaleyraVideoInitializer.Companion)
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Connected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId"
        })
        requestConnect("loggedUserId")
        coVerify(exactly = 0) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoAlreadyConnecting_connectNotRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializer.instance = spyKaleyraVideoInitializer
        mockkObject(KaleyraVideoInitializer.Companion)
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Connecting)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId"
        })
        requestConnect("loggedUserId")
        coVerify(exactly = 0) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoNotConnected_connectRequested_connectNotCalled() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializer.instance = spyKaleyraVideoInitializer
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        val hasConnected = requestConnect("loggedUserId")
        Assert.assertEquals(false, hasConnected)
    }

    @Test
    fun testConnectRequested_otherUserConnected_false() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializer.instance = spyKaleyraVideoInitializer
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected) andThen MutableStateFlow(State.Connected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId2"
        })
        val hasConnected = requestConnect("loggedUserId")
        coVerify(exactly = 1) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
        Assert.assertEquals(false, hasConnected)
        coVerify(exactly = 1) { KaleyraVideo.disconnect() }
    }

    @Test
    fun testConnectRequested_neverConnected_kaleyraVideoDisconnected() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializer.instance = spyKaleyraVideoInitializer
        mockkObject(KaleyraVideo)
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(null)
        val hasConnected = requestConnect("loggedUserId")
        coVerify(exactly = 1) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
        Assert.assertEquals(false, hasConnected)
        coVerify(exactly = 1) { KaleyraVideo.disconnect() }
    }
}