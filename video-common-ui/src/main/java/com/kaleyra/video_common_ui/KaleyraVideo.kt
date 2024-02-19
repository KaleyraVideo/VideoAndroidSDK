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

package com.kaleyra.video_common_ui

import com.kaleyra.video.AccessTokenProvider
import com.kaleyra.video.Collaboration
import com.kaleyra.video.Company
import com.kaleyra.video.State
import com.kaleyra.video.Synchronization
import com.kaleyra.video.User
import com.kaleyra.video.configuration.Configuration
import com.kaleyra.video.utils.extensions.awaitResult
import com.kaleyra.video_common_ui.activityclazzprovider.GlassActivityClazzProvider
import com.kaleyra.video_common_ui.activityclazzprovider.PhoneActivityClazzProvider
import com.kaleyra.video_common_ui.model.UserDetailsProvider
import com.kaleyra.video_common_ui.termsandconditions.TermsAndConditionsRequester
import com.kaleyra.video_common_ui.utils.CORE_UI
import com.kaleyra.video_common_ui.utils.extensions.CoroutineExtensions.launchBlocking
import com.kaleyra.video_utils.ContextRetainer
import com.kaleyra.video_utils.cached
import com.kaleyra.video_utils.getValue
import com.kaleyra.video_utils.logging.PriorityLogger
import com.kaleyra.video_utils.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import java.util.concurrent.Executors

/**
 * Kaleyra Video Android SDK facade
 */
object KaleyraVideo {

    @get:Synchronized
        /**
         * Is configured flag, true if KaleyraVideo is configured, false otherwise
         * */
    val isConfigured
        get() = collaboration != null


    @get:Synchronized
    @set:Synchronized
    /**
     * Collaboration
     */
    internal var collaboration: Collaboration? = null
        set(value) {
            logger = value?.configuration?.logger
            field = value
        }

    private val serialScope by lazy { CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) }

    private lateinit var callActivityClazz: Class<*>
    private lateinit var chatActivityClazz: Class<*>
    private var termsAndConditionsActivityClazz: Class<*>? = null
    private var chatNotificationActivityClazz: Class<*>? = null

    private var termsAndConditionsRequester: TermsAndConditionsRequester? = null

    private var logger: PriorityLogger? = null
    private var _conference: ConferenceUI? by cached { ConferenceUI(collaboration!!.conference, callActivityClazz, logger) }
    private var _conversation: ConversationUI? by cached { ConversationUI(collaboration!!.conversation, chatActivityClazz, chatNotificationActivityClazz) }


    @get:Synchronized
        /**
         * Conference module
         */
    val conference: ConferenceUI
        get() {
            require(collaboration != null) { "configure the CollaborationUI to use the conference" }
            return _conference!!
        }

    @get:Synchronized
        /**
         * Conversation module
         */
    val conversation: ConversationUI
        get() {
            require(collaboration != null) { "configure the KaleyraVideo to use the conversation" }
            return _conversation!!
        }

    /**
     * Company name flow that will emit company's name whenever available
     */
    val companyName: SharedFlow<String> by lazy { collaboration?.company?.name ?: MutableSharedFlow() }

    /**
     * Company theme flow that will emit company's theme configuration whenever available
     */
    val companyTheme: SharedFlow<Company.Theme> by lazy { collaboration?.company?.theme ?: MutableSharedFlow() }

    @get:Synchronized
    @set:Synchronized
        /**
         * Users description to be used for the UI users presentation
         */
    var userDetailsProvider: UserDetailsProvider? = null

    @get:Synchronized
    @set:Synchronized
        /**
         * Optional theme setup that will be used on the UI layer
         */
    var theme: CompanyUI.Theme? = null

    /**
     * Configure
     *
     * @param configuration representing a set of info necessary to instantiate the communication
     * @return Boolean true if KaleyraVideo has been configured, false if already configured
     */
    fun configure(configuration: Configuration): Boolean = synchronized(this) {
        kotlin.runCatching { ContextRetainer.context }.onFailure {
            configuration.logger?.error(logTarget = CORE_UI, message = "You are trying to configure KaleyraVideo SDK in a multi-process application.\nPlease call enableMultiProcess method.")
            return false
        }

        if (isConfigured) return false

        val activityConfiguration = PhoneActivityClazzProvider.getActivityClazzConfiguration() ?: GlassActivityClazzProvider.getActivityClazzConfiguration()
        if (activityConfiguration != null) {
            callActivityClazz = activityConfiguration.callClazz
            chatActivityClazz = activityConfiguration.chatClazz
            termsAndConditionsActivityClazz = activityConfiguration.termsAndConditionsClazz
            chatNotificationActivityClazz = activityConfiguration.customChatNotificationClazz
        } else {
            configuration.logger?.error(logTarget = CORE_UI, message = "No video sdk module included")
            return false
        }

        Collaboration.create(configuration).apply {
            collaboration = this
        }

        termsAndConditionsActivityClazz?.also {
            termsAndConditionsRequester = TermsAndConditionsRequester(it)
        }
        return true
    }

    /**
     * State flow representing Kaleyra Video SDK current state
     */
    val state: StateFlow<State>
        get() {
            require(collaboration != null) { "You need to configure the KaleyraVideo to get the state" }
            return collaboration!!.state
        }

    /**
     * Synchronization flow representing Kaleyra Video SDK current synchronization state
     */
    val synchronization: StateFlow<Synchronization>
        get() {
            require(collaboration != null) { "You need to configure the KaleyraVideo to get the synchronization" }
            return collaboration!!.synchronization
        }

    /**
     * Connected user flow that will emit currently logged user
     */
    val connectedUser: StateFlow<User?>
        get() {
            require(collaboration != null) { "You need to configure the KaleyraVideo to get the connectedUser" }
            return collaboration!!.connectedUser
        }

    /**
     * Connect Kaleyra Video SDK
     * @param userId String the userId of the user that is requested to be connected
     * @param accessTokenProvider SuspendFunction1<[@kotlin.ParameterName] Date, Result<String>> lambda function that will be called whenever a token for the sdk connection is requested
     * @return Deferred<User> the Deferred<User> async result that will contain the connected user or the connection error after the connection attempt
     */
    fun connect(userId: String, accessTokenProvider: AccessTokenProvider): Deferred<User> = CompletableDeferred<User>().apply {
        serialScope.launchBlocking {
            logger?.verbose(logTarget = CORE_UI, message = "Connecting KaleyraVideo...")
            val connect = collaboration?.connect(userId, accessTokenProvider)
            if (connect == null) {
                logger?.error(logTarget = CORE_UI, message = "Connecting KaleyraVideo but KaleyraCollaboration is null")
                return@launchBlocking
            }

            runCatching {
                logger?.verbose(logTarget = CORE_UI, message = "Connecting KaleyraVideo awaiting connect...")
                connect.awaitResult { result ->
                    if (result.isFailure) {
                        logger?.verbose(logTarget = CORE_UI, message = "Connecting KaleyraVideo connect failed with error ${result.exceptionOrNull()?.message}")
                        completeExceptionally(CancellationException(result.exceptionOrNull()?.message))
                    } else {
                        logger?.verbose(logTarget = CORE_UI, message = "Connecting KaleyraVideo connect completed")
                        complete(result.getOrNull()!!)
                    }
                }
            }.onFailure {
                logger?.error(logTarget = CORE_UI, message = "Connecting KaleyraVideo failed with error: ${it.message}")
            }
            termsAndConditionsRequester?.setUp(state, ::disconnect)
        }.invokeOnCompletion { completionException ->
            logger?.verbose(logTarget = CORE_UI, message = "Connecting KaleyraVideo connect job completed ${completionException?.let { "with error: ${it.message}" }}")
        }
    }

    /**
     * Connect Kaleyra Video SDK via access-link
     * The access-link represents a call that the SDK connection will be scoped to. After the connection and the call establishment, when the call will end, the SDK
     * will be automatically disconnected.
     * @param accessLink String the access-link to be used for the SDK connection
     * @return Deferred<User> the Deferred<User> async result that will contain the connected user or the connection error after the connection attempt
     */
    fun connect(accessLink: String): Deferred<User> = CompletableDeferred<User>().apply {
        serialScope.launchBlocking {
            val connect = collaboration?.connect(accessLink) ?: return@launchBlocking
            connect.awaitResult { result ->
                if (result.isFailure) completeExceptionally(CancellationException(result.exceptionOrNull()?.message))
                else complete(result.getOrNull()!!)
            }
            termsAndConditionsRequester?.setUp(state, ::disconnect)
        }
    }

    /**
     * Disconnects the Kaleyra Video SDK
     * @param clearSavedData Boolean flag representing the request to clear all SDK saved data, true to clear all the saved data, false otherwise
     */
    fun disconnect(clearSavedData: Boolean = false) {
        serialScope.launchBlocking {
            collaboration?.disconnect(clearSavedData)
            termsAndConditionsRequester?.dispose()
        }
    }

    /**
     * Resets Kaleyra Video SDK by disconnecting it, clearing configuration and all saved data
     */
    fun reset() {
        serialScope.launchBlocking {
            collaboration ?: return@launchBlocking
            collaboration?.disconnect(true)
            _conference?.dispose()
            _conversation?.dispose()
            termsAndConditionsRequester?.dispose()
            collaboration = null
            _conference = null
            _conversation = null
        }
    }
}

/**
 * Utility function to be called when the call is ready to be displayed
 * @receiver KaleyraVideo Kaleyra Video SDK
 * @param scope CoroutineScope the scope on which to notify the callback
 * @param block Function1<[@kotlin.ParameterName] CallUI, Unit> the callback called when the call is ready to be displayed
 */
internal fun KaleyraVideo.onCallReady(scope: CoroutineScope, block: suspend (call: CallUI) -> Unit) {
    conference.call
        .take(1)
        .onEach { block.invoke(it) }
        .launchIn(scope)
}
