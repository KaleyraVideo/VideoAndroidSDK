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

import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Conference
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallActivityShow
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCannotJoinUrlToast
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallServiceStart
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * Conference UI
 *
 * @property conference The Conference delegate
 * @property callActivityClazz The call activity Class<*>
 * @property logger The PriorityLogger
 * @constructor
 */
class ConferenceUI(
    private val conference: Conference,
    private val callActivityClazz: Class<*>,
    private val logger: PriorityLogger? = null,
) : Conference by conference {

    private val callScope = CoroutineScope(Dispatchers.IO)

    private var callUIMap: HashMap<String, CallUI> = HashMap()

    override val call: SharedFlow<CallUI> = conference.call.map { getOrCreateCallUI(it) }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)

    override val callHistory: SharedFlow<List<CallUI>> = conference.callHistory.map { calls -> calls.map { getOrCreateCallUI(it) } }.shareIn(callScope, SharingStarted.Eagerly, replay = 1)

    /**
     * The call actions that will be set on every call
     */
    var callActions: Set<CallUI.Action> = CallUI.Action.default

    /**
     * The connection service option
     */
    var connectionServiceOption: ConnectionServiceOption = ConnectionServiceOption.Enabled

    init {
        configureCallServiceStart(callActivityClazz, logger, callScope)
        configureCannotJoinUrlToast(callScope)
        configureCallActivityShow(callScope)
    }

    internal fun dispose() {
        callScope.cancel()
    }
    /**
     * Call
     *
     * @param userIDs to be called
     * @param options creation options
     */
    fun call(userIDs: List<String>, options: (Conference.CreationOptions.() -> Unit)? = null): Result<CallUI> = create(userIDs, options).onSuccess { it.connect() }

    /**
     * Join an url
     *
     * @param url to join
     */
    fun joinUrl(url: String): Result<CallUI> = create(url).onSuccess { it.connect() }

    /**
     * @suppress
     */
    override fun create(url: String): Result<CallUI> =
        conference.create(url).map { getOrCreateCallUI(it) }

    /**
     * @suppress
     */
    override fun create(userIDs: List<String>, conf: (Conference.CreationOptions.() -> Unit)?): Result<CallUI> =
        conference.create(userIDs, conf).map { getOrCreateCallUI(it) }

    private fun getOrCreateCallUI(call: Call): CallUI = synchronized(this) {
        callUIMap[call.id] ?: CallUI(
            call = call,
            activityClazz = callActivityClazz,
            actions = MutableStateFlow(callActions)
        ).apply { callUIMap[id] = this }
    }

}