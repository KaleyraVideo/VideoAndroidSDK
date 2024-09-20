package com.kaleyra.video_common_ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import com.kaleyra.video.State
import com.kaleyra.video_common_ui.KaleyraVideoInitializationProvider.Companion.KALEYRA_VIDEO_INITIALIZER
import com.kaleyra.video_common_ui.utils.instantiateClassWithEmptyConstructor
import io.mockk.verify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class KaleyraVideoInitializerTests {

    @Before
    fun setup() {
        mockkObject(KaleyraVideo)
    }

    @Test
    fun testConfigurationRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.isConfigured } returns false
        requestConfiguration()
        verify { spyKaleyraVideoInitializer.onRequestKaleyraVideoConfigure() }
    }

    @Test
    fun testKaleyraVideoAlreadyConfigured_configurationNotRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.isConfigured } returns true
        requestConfiguration()
        verify(exactly = 0) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConfigure() }
    }

    @Test
    fun testConnectRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        requestConnect("loggedUserId")
        verify { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoAlreadyConnected_connectNotRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.state } returns MutableStateFlow(State.Connected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId"
        })
        requestConnect("loggedUserId")
        verify(exactly = 0) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoAlreadyConnecting_connectNotRequested() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.state } returns MutableStateFlow(State.Connecting)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId"
        })
        requestConnect("loggedUserId")
        verify(exactly = 0) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
    }

    @Test
    fun testKaleyraVideoNotConnected_connectRequested_connectNotCalled() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        val hasConnected = requestConnect("loggedUserId")
        Assert.assertEquals(false, hasConnected)
    }

    @Test
    fun testConnectRequested_otherUserConnected_false() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected) andThen MutableStateFlow(State.Connected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(mockk {
            every { userId } returns "loggedUserId2"
        })
        val hasConnected = requestConnect("loggedUserId")
        verify(exactly = 1) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
        Assert.assertEquals(false, hasConnected)
        verify(exactly = 1) { KaleyraVideo.disconnect() }
    }

    @Test
    fun testConnectRequested_neverConnected_kaleyraVideoDisconnected() = runTest {
        val spyKaleyraVideoInitializer = spyk<KaleyraVideoInitializer>()
        KaleyraVideoInitializationProvider.kaleyraVideoInitializer = spyKaleyraVideoInitializer
        every { KaleyraVideo.state } returns MutableStateFlow(State.Disconnected)
        every { KaleyraVideo.connectedUser } returns MutableStateFlow(null)
        val hasConnected = requestConnect("loggedUserId")
        verify(exactly = 1) { spyKaleyraVideoInitializer.onRequestKaleyraVideoConnect() }
        Assert.assertEquals(false, hasConnected)
        verify(exactly = 1) { KaleyraVideo.disconnect() }
    }

    @Test
    fun testKaleyraVideoInitializerInstantiated() {
        val kaleyraVideoInitializer = object : KaleyraVideoInitializer() {
            override fun onRequestKaleyraVideoConfigure() = Unit
            override fun onRequestKaleyraVideoConnect() = Unit
        }
        mockkStatic("com.kaleyra.video_common_ui.utils.ClassUtilsKt")
        every { instantiateClassWithEmptyConstructor<KaleyraVideoInitializer>(any()) } returns  kaleyraVideoInitializer
        val classpath = "classPath"
        val metaDataMock = mockk<Bundle>(relaxed = true)
        every { metaDataMock.getString(KALEYRA_VIDEO_INITIALIZER) } returns classpath
        val applicationInfoMock = ApplicationInfo()
        applicationInfoMock.metaData = metaDataMock
        val packageManagerMock = mockk<PackageManager>(relaxed = true)
        every { packageManagerMock.getApplicationInfo(any<String>(), any<Int>()) } returns applicationInfoMock
        val contextMock = mockk<Context>(relaxed = true)
        every { contextMock.packageManager } returns packageManagerMock

        val kaleyraVideoInitializationProvider = KaleyraVideoInitializationProvider()
        kaleyraVideoInitializationProvider.create(contextMock)

        Assert.assertEquals(kaleyraVideoInitializer, KaleyraVideoInitializationProvider.kaleyraVideoInitializer)
    }

    @Test
    fun kaleyraVideoInitializerMissingManifestMetadata_kaleyraVideoInitializerNotInstantiated() {
        val kaleyraVideoInitializer = object : KaleyraVideoInitializer() {
            override fun onRequestKaleyraVideoConfigure() = Unit
            override fun onRequestKaleyraVideoConnect() = Unit
        }
        mockkStatic("com.kaleyra.video_common_ui.utils.ClassUtilsKt")
        every { instantiateClassWithEmptyConstructor<KaleyraVideoInitializer>(any()) } returns  kaleyraVideoInitializer
        val metaDataMock = mockk<Bundle>(relaxed = true)
        every { metaDataMock.getString(KALEYRA_VIDEO_INITIALIZER) } returns null
        val applicationInfoMock = ApplicationInfo()
        applicationInfoMock.metaData = metaDataMock
        val packageManagerMock = mockk<PackageManager>(relaxed = true)
        every { packageManagerMock.getApplicationInfo(any<String>(), any<Int>()) } returns applicationInfoMock
        val contextMock = mockk<Context>(relaxed = true)
        every { contextMock.packageManager } returns packageManagerMock

        val kaleyraVideoInitializationProvider = KaleyraVideoInitializationProvider()
        kaleyraVideoInitializationProvider.create(contextMock)

        Assert.assertEquals(null, KaleyraVideoInitializationProvider.kaleyraVideoInitializer)
    }

    companion object {
        @JvmStatic
        @AfterClass
        fun tearDown() {
            unmockkAll()
        }
    }
}