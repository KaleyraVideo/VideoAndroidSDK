package com.kaleyra.video_common_ui.notification

import android.app.Activity
import android.app.Application
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NotificationPresentationHandlerTests {

    private val mockkApplication = mockk<Application>(relaxed = true)

    @Before
    fun setup() {
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockkApplication
    }

    @Test
    fun testNotificationPresentationHandlerActivityCreated() {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        val notificationProducer = object : NotificationProducer() {}

        notificationProducer.onActivityCreated(notificationPresentationHandler, mockk())

        Assert.assertTrue(notificationProducer.notificationPresentationHandler != null)
    }

    @Test
    fun testNotificationPresentationHandlerRetrievedInActivityResumed() {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        val notificationProducer = object : NotificationProducer() {}

        notificationProducer.onActivityResumed(notificationPresentationHandler)

        Assert.assertTrue(notificationProducer.notificationPresentationHandler != null)
    }

    @Test
    fun testNotificationProducerStopNotificationPresentationHandlerSetToNull() {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        val notificationProducer = object : NotificationProducer() {}
        notificationProducer.onActivityResumed(notificationPresentationHandler)
        Assert.assertTrue(notificationProducer.notificationPresentationHandler != null)

        notificationProducer.stop()

        Assert.assertTrue(notificationProducer.notificationPresentationHandler == null)
    }

    @Test
    fun testNotificationPresentationHandlerSetToNullInActivityDestroyed() {
        val notificationPresentationHandler = spyk<Activity>(moreInterfaces = arrayOf(NotificationPresentationHandler::class))
        val notificationProducer = object : NotificationProducer() {}
        notificationProducer.onActivityResumed(notificationPresentationHandler)
        Assert.assertTrue(notificationProducer.notificationPresentationHandler != null)

        notificationProducer.onActivityDestroyed(notificationPresentationHandler)

        Assert.assertTrue(notificationProducer.notificationPresentationHandler == null)
    }

    @Test
    fun testNotificationProducerStartActivityCallbacksRegistered() {
        val notificationProducer = object : NotificationProducer() {}

        notificationProducer.bind(mockk())

        verify { mockkApplication.registerActivityLifecycleCallbacks(any()) }
    }

    @Test
    fun testNotificationProducerStopActivityCallbacksUnRegistered() {
        val notificationProducer = object : NotificationProducer() {}

        notificationProducer.stop()

        verify { mockkApplication.unregisterActivityLifecycleCallbacks(any()) }
    }
}
