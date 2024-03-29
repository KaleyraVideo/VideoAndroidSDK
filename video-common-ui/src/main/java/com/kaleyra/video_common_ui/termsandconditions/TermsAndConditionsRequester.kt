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

import com.kaleyra.video.State
import com.kaleyra.video_common_ui.R
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_utils.ContextRetainer
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.plus

internal class TermsAndConditionsRequester(
    private val activityClazz: Class<*>,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO) + CoroutineName("TermsRequester")
) {

    fun setUp(
        state: StateFlow<State>,
        onDecline: () -> Unit
    ) {
        dispose()
        state
            .onEach { connectorState ->
                if (connectorState !is State.Connecting.TermsAgreementRequired) return@onEach
                showTermsAndConditions(connectorState.requiredTerms.first(), onDecline)
            }
            .takeWhile { it !is State.Connected }
            .launchIn(scope)
    }

    fun dispose() {
        scope.coroutineContext.cancelChildren()
    }

    private fun showTermsAndConditions(
        requiredTerms: State.Connecting.TermsAgreementRequired.ConnectionTerms,
        onDecline: () -> Unit
    ) {
        val context = ContextRetainer.context
        val notificationConfig = TermsAndConditionsUI.Config.Notification(
            title = context.getString(R.string.kaleyra_user_data_consent_agreement_notification_title),
            message = context.getString(R.string.kaleyra_user_data_consent_agreement_notification_message),
            dismissCallback = onDecline,
            enableFullscreen = DeviceUtils.isSmartGlass
        )
        val activityConfig =
            TermsAndConditionsUI.Config.Activity(
                title = requiredTerms.titleFieldText,
                message = requiredTerms.bodyFieldText,
                acceptText = requiredTerms.agreeButtonText,
                declineText = requiredTerms.disagreeButtonText,
                acceptCallback = {
                    requiredTerms.agree()
                },
                declineCallback = onDecline
            )

        TermsAndConditionsUI(activityClazz = activityClazz, notificationConfig = notificationConfig, activityConfig = activityConfig).show()
    }

}