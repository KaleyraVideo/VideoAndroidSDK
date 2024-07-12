package com.kaleyra.video_common_ui.notification.call

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.kaleyra.video_common_ui.KaleyraUIProvider
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.notification.CallNotification
import com.kaleyra.video_common_ui.notification.CallNotificationActionReceiver
import com.kaleyra.video_common_ui.notification.CallNotificationExtra
import com.kaleyra.video_common_ui.notification.CallNotificationManager
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_common_ui.utils.PendingIntentExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.isScreenLocked
import com.kaleyra.video_common_ui.utils.extensions.ContextExtensions.turnOnScreen
import com.kaleyra.video_utils.ContextRetainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class CallNotificationManagerTest {

    private val callNotificationManager = object : CallNotificationManager {}

    private val context = spyk(ApplicationProvider.getApplicationContext())

    @Before
    fun setUp() {
        mockkObject(ContextRetainer)
        mockkObject(ContextExtensions)
        mockkObject(DeviceUtils)
        every { ContextRetainer.context } returns context
        every { context.turnOnScreen() } returns Unit
        every { context.isScreenLocked() } returns false
        every { DeviceUtils.isSmartGlass } returns false
        mockkConstructor(CallNotification.Builder::class)
        every { anyConstructed<CallNotification.Builder>().user(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().importance(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().enableCallStyle(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().color(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().timer(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().contentText(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().contentIntent(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().fullscreenIntent(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().answerIntent(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().declineIntent(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().screenShareIntent(any()) } answers { self as CallNotification.Builder }
        every { anyConstructed<CallNotification.Builder>().build() } returns mockk(relaxed = true)
    }

    @Test
    fun testBuildIncomingCallNotification() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildIncomingCallNotification(username = "username")
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().user("username")
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().importance(false)
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(
                resources.getString(R.string.kaleyra_notification_tap_to_return_to_call)
            )
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().enableCallStyle(false)
        }
        verifyContentIntent(exactly = 1)
        verifyAnswerPendingIntent(exactly = 1)
        verifyDeclineIntent(exactly = 1, action = CallNotificationActionReceiver.ACTION_DECLINE)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().answerIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                Assert.assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                Assert.assertEquals(false, intent.getBooleanExtra(KaleyraUIProvider.ENABLE_TILT_EXTRA, false))
                Assert.assertEquals(CallNotificationActionReceiver.ACTION_ANSWER, intent.getStringExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA))
            })
        }
    }

    @Test
    @Config(sdk = [ Build.VERSION_CODES.R])
    fun deviceLocked_buildIncomingCallNotification_fullScreenIntentIsSet() {
        every { context.isScreenLocked() } returns true
        buildIncomingCallNotification()
        verifyFullscreenIntent(exactly = 1)
    }

    @Test
    @Config(sdk = [ Build.VERSION_CODES.R])
    fun deviceUnlocked_buildIncomingCallNotification_fullScreenIntentIsNotSet() {
        every { context.isScreenLocked() } returns false
        buildIncomingCallNotification()
        verifyFullscreenIntent(exactly = 0)
    }

    @Test
    @Config(sdk = [ Build.VERSION_CODES.S])
    fun api31_buildIncomingCallNotification_fullScreenIntentIsSet() {
        every { context.isScreenLocked() } returns false
        buildIncomingCallNotification()
        verifyFullscreenIntent(exactly = 1)
    }

    @Test
    fun isGroupCallTrue_buildIncomingCallNotification_groupUserAndContentText() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildIncomingCallNotification(isGroupCall = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().user(
                resources.getString(R.string.kaleyra_notification_incoming_group_call)
            )
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(
                resources.getString(R.string.kaleyra_notification_tap_to_return_to_group_call)
            )
        }
    }

    @Test
    fun isCallServiceTrue_buildIncomingCallNotification_isCallServiceRunningExtraTrue() {
        buildIncomingCallNotification(isCallServiceRunning = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().fullscreenIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(true, intent.getBooleanExtra(CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA, false))
            })
        }
    }

    @Test
    fun isCallServiceFalse_buildIncomingCallNotification_isCallServiceRunningExtraFalse() {
        buildIncomingCallNotification(isCallServiceRunning = false)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().fullscreenIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(false, intent.getBooleanExtra(CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA, true))
            })
        }
    }

    @Test
    fun isCallServiceTrue_buildOutgoingCallNotification_isCallServiceRunningExtraTrue() {
        buildOutgoingCallNotification(isCallServiceRunning = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().fullscreenIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(true, intent.getBooleanExtra(CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA, false))
            })
        }
    }

    @Test
    fun isCallServiceFalse_buildOutgoingCallNotification_isCallServiceRunningExtraFalse() {
        buildOutgoingCallNotification(isCallServiceRunning = false)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().fullscreenIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(false, intent.getBooleanExtra(CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA, true))
            })
        }
    }

    @Test
    fun enableCallStyleTrue_buildIncomingCallNotification_callStyleEnabled() {
        buildIncomingCallNotification(enableCallStyle = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().enableCallStyle(true)
        }
    }

    @Test
    fun highPriority_buildIncomingCallNotification_importanceSetTrueAndScreenNotTurnedOn() {
        buildIncomingCallNotification(isHighPriority = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().importance(true)
        }
        verify(exactly = 0) { context.turnOnScreen() }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.P])
    fun highPriorityAndApi28_buildIncomingCallNotification_screenTurnedOn() {
        buildIncomingCallNotification(isHighPriority = true)
        verify(exactly = 1) { context.turnOnScreen() }
    }

    @Test
    fun testBuildOutgoingCallNotification() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildOutgoingCallNotification(username = "username")
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().user("username")
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(resources.getString(R.string.kaleyra_notification_tap_to_return_to_call))
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().enableCallStyle(false)
        }
        verifyContentIntent(exactly = 1)
        verifyDeclineIntent(exactly = 1, action = CallNotificationActionReceiver.ACTION_HANGUP)
    }

    @Test
    fun isGroupCallTrue_buildOutgoingCallNotification_groupUserAndContentText() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildOutgoingCallNotification(isGroupCall = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().user(resources.getString(R.string.kaleyra_notification_outgoing_group_call))
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(resources.getString(R.string.kaleyra_notification_tap_to_return_to_group_call))
        }
    }

    @Test
    fun enableCallStyleTrue_buildOutgoingCallNotification_callStyleEnabled() {
        buildOutgoingCallNotification(enableCallStyle = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().enableCallStyle(true)
        }
    }

    @Test
    fun callNotificationManager_buildOngoingCallNotification_callStyleEnabled() {
        callNotificationManager.buildOngoingCallNotification(
            "",
            isLink = false,
            isGroupCall = false,
            isCallRecorded = false,
            isSharingScreen = false,
            isConnecting = false,
            activityClazz = this::class.java,
            enableCallStyle = true
        )
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().enableCallStyle(true)
        }
    }

    @Test
    @Config(sdk = [ Build.VERSION_CODES.N_MR1])
    fun api25_buildOutgoingCallNotification_fullScreenIntentNotSet() {
        buildOutgoingCallNotification()
        verifyFullscreenIntent(exactly = 0)
    }

    @Test
    @Config(sdk = [ Build.VERSION_CODES.O])
    fun api26_buildOutgoingCallNotification_fullScreenIntentSet() {
        buildOutgoingCallNotification()
        verifyFullscreenIntent(exactly = 1)
    }

    @Test
    fun testBuildOngoingCallNotification() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildOngoingCallNotification(username = "username")
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().user("username")
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(resources.getString(R.string.kaleyra_notification_tap_to_return_to_call))
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().enableCallStyle(false)
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().timer(true)
        }
        verifyContentIntent(exactly = 1)
        verifyDeclineIntent(exactly = 1, action = CallNotificationActionReceiver.ACTION_HANGUP)
        verifyScreenShareIntent(exactly = 0)
    }

    @Test
    fun isGroupCallTrue_buildOngoingCallNotification_groupUserAndContentText() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildOngoingCallNotification(isGroupCall = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().user(resources.getString(R.string.kaleyra_notification_ongoing_group_call))
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(resources.getString(R.string.kaleyra_notification_tap_to_return_to_group_call))
        }
    }

    @Test
    fun isLinkTrue_buildOngoingCallNotification_linkUserText() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildOngoingCallNotification(isLink = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().user(resources.getString(R.string.kaleyra_notification_ongoing_call))
        }
    }

    @Test
    fun isCallRecordedTrue_buildOngoingCallNotification_callRecordedContentText() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildOngoingCallNotification(isCallRecorded = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(resources.getString(R.string.kaleyra_notification_call_recorded))
        }
    }

    @Test
    fun isConnectingTrue_buildOngoingCallNotification_callConnectingContentTextAndTimer() {
        val resources = ApplicationProvider.getApplicationContext<Context>().resources
        buildOngoingCallNotification(isConnecting = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().contentText(resources.getString(R.string.kaleyra_notification_connecting_call))
        }
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().timer(false)
        }
    }

    @Test
    fun isSharingScreenTrue_buildOngoingCallNotification_screenShareIntentSet() {
        buildOngoingCallNotification(isSharingScreen = true)
        verifyScreenShareIntent(exactly = 1)
    }

    @Test
    fun enableCallStyleTrue_buildOngoingCallNotification_callStyleEnabled() {
        buildOngoingCallNotification(enableCallStyle = true)
        verify(exactly = 1) {
            anyConstructed<CallNotification.Builder>().enableCallStyle(true)
        }
    }

    @Test
    @Config(sdk = [ Build.VERSION_CODES.N_MR1])
    fun api25_buildOngoingCallNotification_fullScreenIntentNotSet() {
        buildOngoingCallNotification()
        verifyFullscreenIntent(exactly = 0)
    }

    @Test
    @Config(sdk = [ Build.VERSION_CODES.O])
    fun api26_buildOngoingCallNotification_fullScreenIntentSet() {
        buildOngoingCallNotification()
        verifyFullscreenIntent(exactly = 1)
    }

    private fun buildIncomingCallNotification(
        username: String = "",
        isGroupCall: Boolean = false,
        activityClazz: Class<*> = this::class.java,
        isHighPriority: Boolean = false,
        enableCallStyle: Boolean = false,
        isCallServiceRunning: Boolean = true
    ) {
        callNotificationManager.buildIncomingCallNotification(
            username = username,
            isGroupCall = isGroupCall,
            activityClazz = activityClazz,
            isHighPriority = isHighPriority,
            enableCallStyle = enableCallStyle,
            isCallServiceRunning = isCallServiceRunning
        )
    }

    private fun buildOutgoingCallNotification(
        username: String = "",
        isGroupCall: Boolean = false,
        activityClazz: Class<*> = this::class.java,
        enableCallStyle: Boolean = false,
        isCallServiceRunning: Boolean = true
    ) {
        callNotificationManager.buildOutgoingCallNotification(
            username = username,
            isGroupCall = isGroupCall,
            activityClazz = activityClazz,
            enableCallStyle = enableCallStyle,
            isCallServiceRunning = isCallServiceRunning
        )
    }

    private fun buildOngoingCallNotification(
        username: String = "",
        isLink: Boolean = false,
        isGroupCall: Boolean = false,
        isCallRecorded: Boolean = false,
        isSharingScreen: Boolean = false,
        isConnecting: Boolean = false,
        activityClazz: Class<*> = this::class.java,
        enableCallStyle: Boolean = false
    ) {
        callNotificationManager.buildOngoingCallNotification(
            username = username,
            isLink = isLink,
            isGroupCall = isGroupCall,
            isCallRecorded = isCallRecorded,
            isSharingScreen = isSharingScreen,
            isConnecting = isConnecting,
            activityClazz = activityClazz,
            enableCallStyle = enableCallStyle
        )
    }

    private fun verifyContentIntent(exactly: Int) {
        verify(exactly = exactly) {
            anyConstructed<CallNotification.Builder>().contentIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                Assert.assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                Assert.assertEquals(false, intent.getBooleanExtra(KaleyraUIProvider.ENABLE_TILT_EXTRA, false))
                Assert.assertEquals(true, intent.getBooleanExtra(CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA, false))
            })
        }
    }

    private fun verifyAnswerPendingIntent(exactly: Int) {
        verify(exactly = exactly) {
            anyConstructed<CallNotification.Builder>().answerIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                Assert.assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                Assert.assertEquals(false, intent.getBooleanExtra(KaleyraUIProvider.ENABLE_TILT_EXTRA, false))
                Assert.assertEquals(true, intent.getBooleanExtra(CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA, false))
                Assert.assertEquals(CallNotificationActionReceiver.ACTION_ANSWER, intent.getStringExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA))
            })
        }
    }

    private fun verifyDeclineIntent(exactly: Int, action: String) {
        verify(exactly = exactly) {
            anyConstructed<CallNotification.Builder>().declineIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                Assert.assertEquals(action, intent.getStringExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA))
            })
        }
    }

    private fun verifyFullscreenIntent(exactly: Int) {
        verify(exactly = exactly) {
            anyConstructed<CallNotification.Builder>().fullscreenIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                Assert.assertEquals(Intent.ACTION_MAIN, intent.action)
                assert(intent.hasCategory(Intent.CATEGORY_LAUNCHER))
                Assert.assertEquals(false, intent.getBooleanExtra(KaleyraUIProvider.ENABLE_TILT_EXTRA, false))
                Assert.assertEquals(true, intent.getBooleanExtra(CallNotificationExtra.IS_CALL_SERVICE_RUNNING_EXTRA, false))
            })
        }
    }

    private fun verifyScreenShareIntent(exactly: Int) {
        verify(exactly = exactly) {
            anyConstructed<CallNotification.Builder>().screenShareIntent(withArg {
                val intent = Shadows.shadowOf(it).savedIntent
                Assert.assertEquals(PendingIntentExtensions.updateFlags, Shadows.shadowOf(it).flags)
                Assert.assertEquals(CallNotificationActionReceiver.ACTION_STOP_SCREEN_SHARE, intent.getStringExtra(CallNotificationExtra.NOTIFICATION_ACTION_EXTRA))
            })
        }
    }
}