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

package com.kaleyra.video_common_ui.notification.signature

import android.app.Notification
import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video.sharedfolder.SignDocument
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_common_ui.notification.NotificationManager
import com.kaleyra.video_common_ui.notification.NotificationProducer
import com.kaleyra.video_common_ui.notification.model.PRIORITY_HIDDEN
import com.kaleyra.video_common_ui.notification.model.toNotificationCompatPriority
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform

const val EXTRA_SIGN_ID = "com.kaleyra.video_common_ui.EXTRA_SIGN_ID"

internal class SignatureNotificationProducer(private val coroutineScope: CoroutineScope) : NotificationProducer() {

    private var job: Job? = null

    override fun bind(call: CallUI) {
        super.bind(call)
        var lastNotifiedSignDocument: SignDocument? = null
        val me = call.participants.value.me
        val context = ContextRetainer.context
        job = call.sharedFolder.signDocuments
            .transform { files ->
                val lastSignDocument = files.lastOrNull { file -> file.sender.userId != me?.userId } ?: return@transform
                if (lastNotifiedSignDocument != lastSignDocument) emit(lastSignDocument)
                lastNotifiedSignDocument = lastSignDocument
            }.onEach {
                if (!SignDocumentsVisibilityObserver.isDisplayed.value) {
                    val notificationPriority = (notificationPresentationHandler
                        ?.notificationPresentationHandler
                        ?.invoke(com.kaleyra.video_common_ui.notification.model.Notification.SignDocument)
                        ?: com.kaleyra.video_common_ui.notification.model.Notification.PresentationMode.HighPriority
                        ).toNotificationCompatPriority()
                    if (notificationPriority == PRIORITY_HIDDEN) return@onEach
                    val notification = buildNotification(context, call, it, notificationPriority, call.activityClazz)
                    NotificationManager.notify(it.id.hashCode(), notification)
                }
            }.onCompletion {
                call.sharedFolder.signDocuments.value.forEach {
                    NotificationManager.cancel(it.id.hashCode())
                }
            }
            .launchIn(coroutineScope)
    }

    override fun stop() {
        super.stop()
        job?.cancel()
    }

    private suspend fun buildNotification(context: Context, call: Call, signDocument: SignDocument, notificationPriority: Int, activityClazz: Class<*>): Notification {
        val participants = call.participants.first()
        val participant = participants.others.firstOrNull { it.userId == signDocument.sender.userId }
        val participantsList = participant?.userId?.let { listOf(it) } ?: listOf()
        ContactDetailsManager.refreshContactDetails(*participantsList.toTypedArray())
        val username = participant?.combinedDisplayName?.first() ?: participant?.userId ?: ""
        return NotificationManager.buildIncomingSignatureNotification(context, username, signDocument.id, notificationPriority, activityClazz)
    }
}