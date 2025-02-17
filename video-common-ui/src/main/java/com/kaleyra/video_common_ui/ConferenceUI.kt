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
import com.kaleyra.video_common_ui.ConferenceUIExtensions.bindCallButtons
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallActivityShow
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallServiceStart
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureCallSounds
import com.kaleyra.video_common_ui.ConferenceUIExtensions.configureScreenShareOverlayProducer
import com.kaleyra.video_common_ui.notification.DisplayedChatActivity.Companion.chatId
import com.kaleyra.video_utils.logging.PriorityLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.newFixedThreadPoolContext

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
    private val isSmartGlass: Boolean = false,
) : Conference by conference {

    private val backgroundDispatcher = newFixedThreadPoolContext(100, "Conference dispatcher")

    private val conferenceDispatcher = CoroutineScope(backgroundDispatcher)

    private var callUIMap: HashMap<String, CallUI> = HashMap()

    override val call: SharedFlow<CallUI> = conference.call.map { getOrCreateCallUI(it) }.shareIn(conferenceDispatcher, SharingStarted.Eagerly, replay = 1)

    override val callHistory: SharedFlow<List<CallUI>> = conference.callHistory.map { calls -> calls.map { getOrCreateCallUI(it) } }.shareIn(conferenceDispatcher, SharingStarted.Eagerly, replay = 1)

    /**
     * The call actions that will be set on every call
     */
    @Deprecated("CallActions have been deprecated and will be removed in a further release. Please add Call Buttons to the CallUI object in order to add/remove call actions to a call.")
    var callActions: Set<CallUI.Action>? = null

    /**
     * The connection service option enabled by default
     */
    var connectionServiceOption: ConnectionServiceOption = if (!isSmartGlass) ConnectionServiceOption.Enabled else ConnectionServiceOption.Disabled
        set(value) {
            if (isSmartGlass) return
            else field = value
        }

    init {
        configureCallServiceStart(callActivityClazz, logger, conferenceDispatcher)
        configureCallSounds(logger, conferenceDispatcher)
        configureScreenShareOverlayProducer(conferenceDispatcher)
        configureCallActivityShow(conferenceDispatcher)
        bindCallButtons(conferenceDispatcher)
    }

    internal fun dispose() {
        conferenceDispatcher.cancel()
    }

    /**
     * Call
     *
     * @param userIDs to be called
     * @param options creation options
     */
    fun call(
        userIDs: List<String>,
        options: (Conference.CreationOptions.() -> Unit)? = null
    ): Result<CallUI> = create(userIDs, options, null).onSuccess { it.connect() }

    /**
     * Call
     *
     * @param userIDs to be called
     * @param options creation options
     */
    fun call(
        userIDs: List<String>,
        options: (Conference.CreationOptions.() -> Unit)? = null,
        chatId: String? = null): Result<CallUI> = create(userIDs, options, chatId).onSuccess { it.connect() }

    /**
     * Join an url
     *
     * @param url to join
     */
    @Deprecated(
        message = ("joinUrl function has been deprecated and it will be removed in a further release."),
        replaceWith = ReplaceWith("KaleyraVideo.conference.join(url)")
    )
    fun joinUrl(url: String): Result<CallUI> = join(url)

    fun join(url: String): Result<CallUI> = create(url).onSuccess { it.connect() }

    /**
     * @suppress
     */
    override fun create(url: String): Result<CallUI> =
        conference.create(url).map { getOrCreateCallUI(it) }

    /**
     * @suppress
     */
    override fun create(
        userIDs: List<String>,
        conf: (Conference.CreationOptions.() -> Unit)?,
        chatId: String?): Result<CallUI> = conference.create(userIDs, conf, chatId).map { getOrCreateCallUI(it) }

    private fun getOrCreateCallUI(call: Call): CallUI = synchronized(this) {
        callUIMap[call.id] ?: CallUI(
            call = call,
            activityClazz = callActivityClazz,
            actions = callActions?.let { MutableStateFlow(it) }
            ).apply { callUIMap[id] = this }
    }
}
