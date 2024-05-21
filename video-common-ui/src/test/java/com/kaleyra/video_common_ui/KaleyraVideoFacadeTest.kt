package com.kaleyra.video_common_ui

import com.kaleyra.video.Collaboration
import com.kaleyra.video_common_ui.activityclazzprovider.PhoneActivityClazzProvider
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class KaleyraVideoFacadeTest {

    @Before
    fun setup() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockk()
        mockkObject(PhoneActivityClazzProvider)
        mockkObject(Collaboration)
        every { Collaboration.create(any()) } returns mockk(relaxed = true)
        every { PhoneActivityClazzProvider.getActivityClazzConfiguration() } returns mockk(relaxed = true)
        KaleyraVideo.reset()

    }

    @Test(expected = Exception::class)
    fun testConferenceUINotReady() = runTest {
        KaleyraVideo.conference
    }

    @Test(expected = Exception::class)
    fun testConversationUINotReady() = runTest {
        KaleyraVideo.conversation
    }

    @Test
    fun testConferenceUIReady() = runTest {
        KaleyraVideo.configure(mockk(relaxed = true))
        Assert.assertEquals(true, KaleyraVideo.conference != null)
    }

    @Test
    fun testConversationUIReady() = runTest {
        KaleyraVideo.configure(mockk(relaxed = true))
        Assert.assertEquals(true, KaleyraVideo.conversation != null)
    }
}
