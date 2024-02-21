package com.kaleyra.video_common_ui.connectionservice

import android.content.Context
import android.os.Build
import com.kaleyra.video_common_ui.ConnectionServiceSetting
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.hasConnectionServicePermissions
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class ConnectionServiceUtilsTest {

    private val contextMock = mockk<Context>()

    @Before
    fun setUp() {
        mockkObject(ConnectionServiceUtils, ContextRetainer, ContextExtensions, KaleyraVideo)
        every { ContextRetainer.context } returns contextMock
        every { contextMock.hasConnectionServicePermissions() } returns true
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns true
        every { KaleyraVideo.conference.connectionServiceSetting } returns ConnectionServiceSetting.Default
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun api26_isConnectionServiceSupported_true() {
        assertEquals(true, ConnectionServiceUtils.isConnectionServiceSupported)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    fun api25_isConnectionServiceSupported_false() {
        assertEquals(false, ConnectionServiceUtils.isConnectionServiceSupported)
    }

    @Test
    fun connectionServiceDefaultSetting_isConnectionServiceEnabled_true() {
        every { KaleyraVideo.conference.connectionServiceSetting } returns ConnectionServiceSetting.Default
        assertEquals(true, ConnectionServiceUtils.isConnectionServiceEnabled)
    }

    @Test
    fun connectionServiceFallbackSetting_isConnectionServiceEnabled_true() {
        every { KaleyraVideo.conference.connectionServiceSetting } returns ConnectionServiceSetting.Fallback
        assertEquals(true, ConnectionServiceUtils.isConnectionServiceEnabled)
    }

    @Test
    fun connectionServiceDisabled_isConnectionServiceEnabled_false() {
        every { KaleyraVideo.conference.connectionServiceSetting } returns ConnectionServiceSetting.Disabled
        assertEquals(false, ConnectionServiceUtils.isConnectionServiceEnabled)
    }

    @Test
    fun isConnectionServiceSupportedTrue_isConnectionServiceEnabled_true() {
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns true
        assertEquals(true, ConnectionServiceUtils.isConnectionServiceEnabled)
    }

    @Test
    fun isConnectionServiceSupportedFalse_isConnectionServiceEnabled_false() {
        every { ConnectionServiceUtils.isConnectionServiceSupported } returns false
        assertEquals(false, ConnectionServiceUtils.isConnectionServiceEnabled)
    }

    @Test
    fun hasConnectionServicePermissionsTrue_isConnectionServiceEnabled_true() {
        every { contextMock.hasConnectionServicePermissions() } returns true
        assertEquals(true, ConnectionServiceUtils.isConnectionServiceEnabled)
    }

    @Test
    fun hasConnectionServicePermissionsFalse_isConnectionServiceEnabled_false() {
        every { contextMock.hasConnectionServicePermissions() } returns false
        assertEquals(false, ConnectionServiceUtils.isConnectionServiceEnabled)
    }
}