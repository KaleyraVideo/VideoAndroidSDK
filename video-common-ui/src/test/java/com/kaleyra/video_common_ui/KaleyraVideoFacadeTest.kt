package com.kaleyra.video_common_ui

import android.app.Application
import com.kaleyra.video.Collaboration
import com.kaleyra.video_common_ui.activityclazzprovider.PhoneActivityClazzProvider
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.asCoroutineDispatcher
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class KaleyraVideoFacadeTest {

//    @Before
//    fun setup() {
//        mockkStatic("com.kaleyra.video_common_ui.KaleyraVideoInitializationProviderKt")
//        mockkStatic(::requestConfiguration)
//        mockkStatic("kotlinx.coroutines.ExecutorsKt")
//        mockkStatic(Executors::class)
//        every { Executors.newSingleThreadExecutor() } returns mockk {
//            every { this@mockk.asCoroutineDispatcher() } returns object : ExecutorCoroutineDispatcher() {
//                override val executor: Executor = Executor { }
//                override fun close() = Unit
//                override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
//            }
//        }
//        every { requestConfiguration() } returns false
//        mockkObject(ContextRetainer)
//        every { ContextRetainer.context } returns mockk<Application>(relaxed = true)
//        mockkObject(PhoneActivityClazzProvider)
//        mockkObject(Collaboration)
//        every { Collaboration.create(any()) } returns mockk(relaxed = true)
//        every { PhoneActivityClazzProvider.getActivityClazzConfiguration() } returns mockk(relaxed = true)
//        KaleyraVideo.reset()
//    }
//
//    @Test(expected = Exception::class)
//    fun testConferenceUINotReady() {
//        KaleyraVideo.conference
//    }
//
//    @Test(expected = Exception::class)
//    fun testConversationUINotReady() {
//        KaleyraVideo.conversation
//    }
//
//    @Test
//    fun conversationUI_requestConfigurationRequested() {
//        KaleyraVideo.configure(mockk(relaxed = true))
//        KaleyraVideo.conversation
//        verify { requestConfiguration() }
//    }
//
//    @Test
//    fun collaborationState_requestConfigurationRequested() {
//        KaleyraVideo.configure(mockk(relaxed = true))
//        KaleyraVideo.state
//        verify { requestConfiguration() }
//    }
//
//    @Test
//    fun collaborationSynchronization_requestConfigurationRequested() {
//        KaleyraVideo.configure(mockk(relaxed = true))
//        KaleyraVideo.synchronization
//        verify { requestConfiguration() }
//    }
//
//    @Test
//    fun collaborationConnectedUser_requestConfigurationRequested() {
//        KaleyraVideo.configure(mockk(relaxed = true))
//        KaleyraVideo.connectedUser
//        verify { requestConfiguration() }
//    }
//
//    @Test
//    fun testConferenceUIReady() {
//        KaleyraVideo.configure(mockk(relaxed = true))
//        Assert.assertEquals(true, KaleyraVideo.conference != null)
//    }
//
//    @Test
//    fun testConversationUIReady() {
//        KaleyraVideo.configure(mockk(relaxed = true))
//        Assert.assertEquals(true, KaleyraVideo.conversation != null)
//    }

}
