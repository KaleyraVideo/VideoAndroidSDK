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

package com.kaleyra.demo_video_sdk

import android.content.Context
import android.net.Uri
import com.kaleyra.app_configuration.model.UserDetailsProviderMode.CUSTOM
import com.kaleyra.app_configuration.utils.MediaStorageUtils.getUriFromString
import com.kaleyra.app_utilities.storage.ConfigurationPrefsManager
import com.kaleyra.app_utilities.storage.LoginManager
import com.kaleyra.demo_video_sdk.storage.DefaultConfigurationManager
import com.kaleyra.demo_video_sdk.ui.custom_views.CallConfiguration
import com.kaleyra.demo_video_sdk.ui.custom_views.mapToCallUIButtons
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Conference
import com.kaleyra.video_common_ui.ConnectionServiceOption
import com.kaleyra.video_common_ui.KaleyraFontFamily
import com.kaleyra.video_common_ui.KaleyraVideo
import com.kaleyra.video_common_ui.KaleyraVideoInitializer
import com.kaleyra.video_common_ui.PushNotificationHandlingStrategy
import com.kaleyra.video_common_ui.model.UserDetails
import com.kaleyra.video_common_ui.model.UserDetailsProvider
import com.kaleyra.video_common_ui.theme.Theme
import com.kaleyra.video_common_ui.theme.resource.URIResource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

class DemoAppKaleyraVideoInitializer : KaleyraVideoInitializer() {

    companion object {

        fun configure(applicationContext: Context) {
            val configuration = applicationContext.configuration()
            if (!KaleyraVideo.isConfigured) {
                KaleyraVideo.configure(configuration)
                KaleyraVideo.userDetailsProvider = customUserDetailsProvider(applicationContext)
                KaleyraVideo.conference.connectionServiceOption = ConnectionServiceOption.Enabled
                KaleyraVideo.pushNotificationHandlingStrategy = PushNotificationHandlingStrategy.Automatic
                val appConfiguration = ConfigurationPrefsManager.getConfiguration(applicationContext)
                val logoUri = appConfiguration.logoUrl?.takeIf { it.isNotEmpty() }
                    ?.let { getUriFromString(appConfiguration.logoUrl) }
                KaleyraVideo.theme = Theme(
                    logo = logoUri?.let { Theme.Logo(URIResource(it, it)) },
                    typography = Theme.Typography(fontFamily = KaleyraFontFamily.default),
                    config = Theme.Config(style = Theme.Config.Style.System)
                )
            }

            if (getCallConfiguration().options.backCameraAsDefault)
                KaleyraVideo.conference.settings.camera = Conference.Settings.Camera.Back

            KaleyraVideo.conference.call.onEach { ongoingCall ->
                val callConfiguration = getCallConfiguration()

                ongoingCall.buttonsProvider = { callButtons ->
                    callButtons + getCallConfiguration().actions.mapToCallUIButtons()
                }

                ongoingCall.state
                    .takeWhile { it !is Call.State.Disconnected.Ended }
                    .onCompletion { ongoingCall.buttonsProvider = null }
                    .launchIn(MainScope())

                ongoingCall.withFeedback = callConfiguration.options.feedbackEnabled
            }.launchIn(MainScope())
        }

        fun getCallConfiguration(): CallConfiguration = DefaultConfigurationManager.getDefaultCallConfiguration()

        fun connect(applicationContext: Context) {
            val loggedUserId = LoginManager.getLoggedUser(applicationContext)
            if (!LoginManager.isUserLogged(applicationContext)) return
            KaleyraVideo.connect(loggedUserId) { requestToken(loggedUserId) }
        }

        internal fun customUserDetailsProvider(context: Context): UserDetailsProvider? {
            val appConfiguration = ConfigurationPrefsManager.getConfiguration(context)
            if (appConfiguration.userDetailsProviderMode != CUSTOM) return null
            return { userIds: List<String> ->
                Result.success(userIds.map {
                    UserDetails(
                        userId = it,
                        name = appConfiguration.customUserDetailsName ?: it,
                        image = getUriFromString(appConfiguration.customUserDetailsImageUrl) ?: Uri.EMPTY
                    )
                })
            }
        }
    }

    override fun onRequestKaleyraVideoConfigure() = configure(applicationContext)

    override fun onRequestKaleyraVideoConnect() = connect(applicationContext)
}