package com.kaleyra.video_common_ui

import com.kaleyra.video.Collaboration
import com.kaleyra.video_common_ui.activityclazzprovider.PhoneActivityClazzProvider
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
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

    @Before
    fun setup() {
        mockkStatic(Executors::class)
        mockkStatic("kotlinx.coroutines.ExecutorsKt")
        every { Executors.newSingleThreadExecutor() } returns mockk {
            every { this@mockk.asCoroutineDispatcher() } returns object : ExecutorCoroutineDispatcher() {
                override val executor: Executor = Executor { }
                override fun close() = Unit
                override fun dispatch(context: CoroutineContext, block: Runnable) = block.run()
            }
        }
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockk()
        mockkObject(PhoneActivityClazzProvider)
        mockkObject(Collaboration)
        every { Collaboration.create(any()) } returns mockk(relaxed = true)
        every { PhoneActivityClazzProvider.getActivityClazzConfiguration() } returns mockk(relaxed = true)
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
    fun testConferenceUIReady() {
        KaleyraVideo.configure(mockk(relaxed = true))
        Assert.assertEquals(true, KaleyraVideo.conference != null)
    }

    @Test
    fun testConversationUIReady() {
        KaleyraVideo.configure(mockk(relaxed = true))
        Assert.assertEquals(true, KaleyraVideo.conversation != null)
    }
}
