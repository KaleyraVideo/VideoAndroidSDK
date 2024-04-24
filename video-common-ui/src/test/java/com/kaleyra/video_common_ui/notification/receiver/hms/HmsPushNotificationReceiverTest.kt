package com.kaleyra.video_common_ui.notification.receiver.hms

import android.content.Intent
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.kaleyra.video_common_ui.KaleyraVideoSharedPrefs
import com.kaleyra.video_common_ui.PushNotificationHandlingStrategy
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
class HmsPushNotificationReceiverTest {

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
    fun testHmsNotificationReceived_kaleyraPayload_workManagerEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Automatic
        val hmsReceiver = HmsPushNotificationReceiver()
        val hmsIntent = Intent()
        hmsIntent.putExtra(HmsPushNotificationReceiver.PUSH_PAYLOAD_BYTE_ARRAY_KEY, ">>>>on_call_incoming>>>>".toByteArray())

        hmsReceiver.onReceive(mockk(relaxed = true), hmsIntent)

        verify { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun testHmsNotificationReceived_otherPayload_workManagerEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Automatic
        val hmsReceiver = HmsPushNotificationReceiver()
        val hmsIntent = Intent()
        hmsIntent.putExtra(HmsPushNotificationReceiver.PUSH_PAYLOAD_BYTE_ARRAY_KEY, ">>>>qwerty>>>>".toByteArray())

        hmsReceiver.onReceive(mockk(relaxed = true), hmsIntent)

        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun testHmsNotificationReceived_noPayload_workManagerEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Automatic
        val hmsReceiver = HmsPushNotificationReceiver()
        val hmsIntent = Intent()

        hmsReceiver.onReceive(mockk(relaxed = true), hmsIntent)

        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

    @Test
    fun testHmsNotificationReceived_pushReceiverDisabled_workManagerEnqueued() {
        val workManager = spyk<WorkManager>()
        mockkStatic(WorkManager::class)
        every { WorkManager.getInstance(any()) } returns workManager
        mockkStatic("com.kaleyra.video_common_ui.utils.PushNotificationPayloadUtilsKt")
        mockkStatic("com.kaleyra.video_common_ui.utils.BundleUtilsKt")
        mockkObject(KaleyraVideoSharedPrefs)
        every { KaleyraVideoSharedPrefs.getPushNotificationHandlingStrategy() } returns PushNotificationHandlingStrategy.Manual
        val hmsReceiver = HmsPushNotificationReceiver()
        val hmsIntent = Intent()

        hmsReceiver.onReceive(mockk(relaxed = true), hmsIntent)

        verify(exactly = 0) { workManager.enqueue(any<OneTimeWorkRequest>()) }
    }

}