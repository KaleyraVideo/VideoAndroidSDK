/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui.termsandconditions

import android.content.Context
import com.kaleyra.video_common_ui.termsandconditions.activity.TermsAndConditionsUIActivityDelegate
import com.kaleyra.video_common_ui.termsandconditions.broadcastreceiver.TermsAndConditionBroadcastReceiver
import com.kaleyra.video_common_ui.termsandconditions.notification.TermsAndConditionsUINotificationDelegate
import com.kaleyra.video_common_ui.utils.AppLifecycle
import com.kaleyra.video_utils.ContextRetainer

/**
 * Terms And Conditions Ui representation of the Terms And Conditions on the Ui
 * @property notificationConfig Notification notification configuration
 * @property activityConfig Activity activity configuration
 * @property activityDelegate TermsAndConditionsUIActivityDelegate terms and conditions activity delegate
 * @property notificationDelegate TermsAndConditionsUINotificationDelegate notification delegate
 * @constructor
 */
open class TermsAndConditionsUI(
    context: Context = ContextRetainer.context,
    activityClazz: Class<*>,
    private val notificationConfig: Config.Notification,
    private val activityConfig: Config.Activity
) : TermsAndConditionBroadcastReceiver() {

    /**
     * Config
     * @property title String title
     * @property message String message
     */
    sealed interface Config {

        val title: String

        val message: String

        /**
         * Notification
         * @property title String notification title
         * @property message String notification message
         * @property dismissCallback Function0<Unit> dismiss callback
         * @property enableFullscreen Boolean flag indicating if the notification can be fullscreen, true if capable of being displayed fullscreen, false otherwise
         * @property timeout Long? optional display timeout in milliseconds
         * @constructor
         */
        data class Notification(
            override val title: String,
            override val message: String,
            val dismissCallback: () -> Unit,
            val enableFullscreen: Boolean = false,
            val timeout: Long? = null
        ) : Config

        /**
         * Activity notification
         * @property title String notification title
         * @property message String notification message
         * @property acceptText String accept text
         * @property declineText String decline test
         * @property acceptCallback Function0<Unit> accept callback
         * @property declineCallback Function0<Unit> decline callback
         * @constructor
         */
        data class Activity(
            override val title: String,
            override val message: String,
            val acceptText: String,
            val declineText: String,
            val acceptCallback: () -> Unit,
            val declineCallback: () -> Unit
        ) : Config
    }

    private val activityDelegate = TermsAndConditionsUIActivityDelegate(context, activityConfig, activityClazz)

    private val notificationDelegate = TermsAndConditionsUINotificationDelegate(context, notificationConfig)

    /**
     * Show the notification
     * @param context Context required context to show the notifcation
     */
    open fun show(context: Context = ContextRetainer.context) {
        registerForTermAndConditionAction(context)
        if (AppLifecycle.isInForeground.value) activityDelegate.showActivity()
        else notificationDelegate.showNotification(activityDelegate.getActivityIntent())
    }

    /**
     * Dismiss the notifcation
     */
    fun dismiss() = notificationDelegate.dismissNotification()

    /**
     * Called when the notification has been accepted
     */
    override fun onActionAccept() = activityConfig.acceptCallback()

    /**
     * Called when the notification has been declined
     */
    override fun onActionDecline() = activityConfig.declineCallback()

    /**
     * Called when the notification has been canceled
     */
    override fun onActionCancel() = notificationConfig.dismissCallback()
}
