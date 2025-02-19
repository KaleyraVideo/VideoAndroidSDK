@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_common_ui

import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video.Collaboration
import com.kaleyra.video.configuration.Configuration
import com.kaleyra.video_common_ui.activityclazzprovider.PhoneActivityClazzProvider
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
class KaleyraVideoFacadeTest {

    private val configuration = mockk<Configuration>(relaxed = true)

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        ContextRetainer().create(ApplicationProvider.getApplicationContext())
        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoInitializationProviderKt")
        mockkStatic(::requestConfiguration)
        mockkStatic("kotlinx.coroutines.ExecutorsKt")
        mockkStatic(Executors::class)
        every { Executors.newSingleThreadExecutor() } returns mockk {
            every { this@mockk.asCoroutineDispatcher() } returns object : ExecutorCoroutineDispatcher() {
                override val executor: Executor = Executor { command -> command.run() }
                override fun close() = Unit
                override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
            }
        }
        every { requestConfiguration() } returns false
        mockkObject(PhoneActivityClazzProvider)
        mockkObject(Collaboration)
        every { Collaboration.create(any()) } returns mockk(relaxed = true)
        every { PhoneActivityClazzProvider.getActivityClazzConfiguration() } returns mockk(relaxed = true)

        mockkObject(com.kaleyra.video_common_ui.notification.NotificationManager)
    }

    @After
    fun tearDown() {
        KaleyraVideo.reset()
    }

    @Test(expected = Exception::class)
    fun testConferenceUINotReady() {
        KaleyraVideo.conference
    }

    @Test(expected = Exception::class)
    fun testConversationUINotReady() {
        KaleyraVideo.conversation
    }

    @Test
    fun conversationUI_requestConfigurationRequested() {
        KaleyraVideo.configure(configuration)
        KaleyraVideo.conversation
        verify { requestConfiguration() }
    }

        @Test
    fun collaborationState_requestConfigurationRequested() {
        KaleyraVideo.configure(configuration)
        KaleyraVideo.state
        verify { requestConfiguration() }
    }

    @Test
    fun collaborationSynchronization_requestConfigurationRequested() {
        KaleyraVideo.configure(configuration)
        KaleyraVideo.synchronization
        verify { requestConfiguration() }
    }

    @Test
    fun collaborationConnectedUser_requestConfigurationRequested() {
        KaleyraVideo.configure(configuration)
        KaleyraVideo.connectedUser
        verify { requestConfiguration() }
    }

    @Test
    fun testNotificationsCanceledOnReset() {
        var notificationsCancelled = false
        every { com.kaleyra.video_common_ui.notification.NotificationManager.cancelAll() } answers {
            notificationsCancelled = true
            mockk()
        }
        KaleyraVideo.configure(configuration)
        KaleyraVideo.reset()
        println("assert")
        Assert.assertEquals(true, notificationsCancelled)
    }

    @Test
    fun testNotificationsCanceledDisconnect() {
        var notificationsCancelled = false
        every { com.kaleyra.video_common_ui.notification.NotificationManager.cancelAll() } answers {
            notificationsCancelled = true
            mockk()
        }
        KaleyraVideo.configure(configuration)
        KaleyraVideo.disconnect()
        Assert.assertEquals(true, notificationsCancelled)
    }

    @Test
    fun testConferenceUIReady() {
        KaleyraVideo.configure(configuration)
        Assert.assertEquals(true, KaleyraVideo.conference != null)
    }

    @Test
    fun testConversationUIReady() {
        KaleyraVideo.configure(configuration)
        Assert.assertEquals(true, KaleyraVideo.conversation != null)
    }

    companion object {
        
        @JvmStatic
        @AfterClass
        fun tearDownAfterClass() {
            unmockkAll()
        }
    }
}
