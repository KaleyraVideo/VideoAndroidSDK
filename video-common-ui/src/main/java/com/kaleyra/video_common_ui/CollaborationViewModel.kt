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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.Company
import com.kaleyra.video.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * CollaborationViewModel representing the conference and conversation wrapper
 * @constructor
 */
abstract class CollaborationViewModel(configure: suspend () -> Configuration) : ViewModel() {

    /**
     * Collaboration View Model Configuration
     */
    sealed class Configuration {
        /**
         * Success Configuration
         * @property conference ConferenceUI the conference module
         * @property conversation ConversationUI the conversation module
         * @property company Company the requesting company to be configured
         * @property connectedUser StateFlow<User?> a flow representing the logged user whenever it will be connected
         * @constructor
         */
        data class Success(val conference: ConferenceUI, val conversation: ConversationUI, val company: Company, val connectedUser: StateFlow<User?>) : Configuration()

        /**
         * Failure Configuration representing an error during the configuration retrieval process
         */
        data object Failure : Configuration()
    }

    private val _configuration = MutableSharedFlow<Configuration>(replay = 1, extraBufferCapacity = 1)

    /**
     * Flag flow representing if the configuration has been successful
     */
    val isCollaborationConfigured = _configuration.map { it is Configuration.Success }.shareInEagerly(viewModelScope)

    /**
     * Conference module flow
     */
    val conference = _configuration.mapSuccess { it.conference }.shareInEagerly(viewModelScope)

    /**
     * Conversation module flow
     */
    val conversation = _configuration.mapSuccess { it.conversation }.shareInEagerly(viewModelScope)

    /**
     * Configured company flow
     */
    val company = _configuration.mapSuccess { it.company }.shareInEagerly(viewModelScope)

    /**
     * Logged user flow whenever it will be connected
     */
    val connectedUser = _configuration.filterIsInstance<Configuration.Success>().flatMapLatest { it.connectedUser }.shareInEagerly(viewModelScope)

    init {
        viewModelScope.launch {
            _configuration.emit(configure())
        }
    }

    private inline fun <T> Flow<Configuration>.mapSuccess(crossinline block: (Configuration.Success) -> T): Flow<T> =
        filterIsInstance<Configuration.Success>().map { block(it) }

    /**
     * Share-in-eagerly flow operator based on input coroutine scope
     * @receiver Flow<T> the generic type T flow to be used in the share-in-eagerly operation
     * @param scope CoroutineScope the coroutine scope to be used for the share-in operation
     * @return SharedFlow<T> returns the obtained share-in-eagerly shared flow of generic type T
     */
    protected fun <T> Flow<T>.shareInEagerly(scope: CoroutineScope): SharedFlow<T> =
        this@shareInEagerly.shareIn(scope, SharingStarted.Eagerly, 1)

    /**
     * Utility function to retrieve the associated current value of the shared flow
     * @receiver SharedFlow<T> the generic type T shared flow from which to retrieve the current value
     * @return T? the current value if any
     */
    protected fun <T> SharedFlow<T>.getValue(): T? =
        replayCache.firstOrNull()
}

/**
 * Request a new Configuration via KaleyraVideoService implementation
 * @return CollaborationViewModel.Configuration returns the required configuration if the procedure succeed, a failure error otherwise.
 */
fun requestCollaborationViewModelConfiguration(): CollaborationViewModel.Configuration {
    requestConfiguration()
    return if (KaleyraVideo.isConfigured) {
        CollaborationViewModel.Configuration.Success(
            conference = KaleyraVideo.conference,
            conversation = KaleyraVideo.conversation,
            company = KaleyraVideo.collaboration?.company ?: NoOpCompany(),
            connectedUser = KaleyraVideo.connectedUser
        )
    } else CollaborationViewModel.Configuration.Failure
}
