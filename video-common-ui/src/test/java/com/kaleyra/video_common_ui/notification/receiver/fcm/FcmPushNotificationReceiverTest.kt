package com.kaleyra.video_common_ui.notification.receiver.fcm

import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.kaleyra.video_common_ui.KaleyraVideoSharedPrefs
import com.kaleyra.video_common_ui.PushNotificationHandlingStrategy
import com.kaleyra.video_common_ui.utils.isFcmIntent
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FcmPushNotificationReceiverTest {


    @Before
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
        mockkObject(ContextRetainer)
        every { ContextRetainer.context } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testFcmNotificationReceived_workManagerEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Automatic
        val fcmReceiver = FcmPushNotificationReceiver()
        val fcmIntent = Intent()
        fcmIntent.putExtra("on_call_incoming", "test")
        every { fcmIntent.isFcmIntent() } returns true

        fcmReceiver.onReceive(mockk(relaxed = true), fcmIntent)

        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun testFcmNotificationReceived_pushReceiverDisabled_workManagerNotEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Manual
        val fcmReceiver = FcmPushNotificationReceiver()
        val fcmIntent = Intent()
        fcmIntent.putExtra("on_call_incoming", "test")
        every { fcmIntent.isFcmIntent() } returns true

        fcmReceiver.onReceive(mockk(relaxed = true), fcmIntent)

        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }


    @Test
    fun testFcmNotificationReceived_notFcmIntent_workManagerNotEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Automatic
        val fcmReceiver = FcmPushNotificationReceiver()
        val fcmIntent = Intent()
        fcmIntent.putExtra("on_call_incoming", "test")
        every { fcmIntent.isFcmIntent() } returns false

        fcmReceiver.onReceive(mockk(relaxed = true), fcmIntent)

        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun testFcmNotificationReceived_notKaleyraPushPayload_workManagerNotEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Automatic
        val fcmReceiver = FcmPushNotificationReceiver()
        val fcmIntent = Intent()
        every { fcmIntent.isFcmIntent() } returns true
        fcmIntent.putExtra("other_push_payload", "test")

        fcmReceiver.onReceive(mockk(relaxed = true), fcmIntent)

        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }
}