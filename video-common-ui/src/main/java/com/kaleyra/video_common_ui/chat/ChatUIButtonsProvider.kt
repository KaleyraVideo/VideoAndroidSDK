package com.kaleyra.video_common_ui.chat

import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ChatUI
import com.kaleyra.video_common_ui.common.UIButtonsProvider
import com.kaleyra.video_common_ui.utils.extensions.ChatExtensions.toChatUIButtons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

interface ChatUIButtonsProvider : UIButtonsProvider<ChatUI.Button> {
    /**
     * Current Chat UI buttons
     */
    override val buttons: StateFlow<Set<ChatUI.Button>>

    /**
     * Buttons Provider represents an optional callback that is invoked when chat buttons hierarchy is about to being displayed on chat UI.
     * This callback allows to return an updated set of Chat Buttons if some chat button must be added or removed in the integration use case.
     */
    override var buttonsProvider: ((MutableSet<ChatUI.Button>) -> Set<ChatUI.Button>)?
}

internal class DefaultChatUIButtonsProvider(
    val legacyActions: MutableStateFlow<Set<ChatUI.Action>>? = null,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) : ChatUIButtonsProvider {

    private var legacyCallActions: Set<ChatUI.Action>? = null

    private val _buttons: MutableStateFlow<Set<ChatUI.Button>> = MutableStateFlow(setOf())

    override val buttons: StateFlow<Set<ChatUI.Button>> = _buttons

    private val baseButtonProvider: ((MutableSet<ChatUI.Button>) -> Set<ChatUI.Button>) = { callButtons ->
        val updatedButtons = legacyCallActions?.map { it.toChatUIButtons() }?.toSet() ?: callButtons
        emitCallButtons(updatedButtons)
        updatedButtons
    }

    override var buttonsProvider: ((MutableSet<ChatUI.Button>) -> Set<ChatUI.Button>)? = baseButtonProvider
        set(value) {
            field = { callButtons ->
                val updatedButtons = (value ?: baseButtonProvider).invoke(callButtons)
                emitCallButtons(updatedButtons)
                updatedButtons
            }
            buttons.replayCache.firstOrNull()?.let { callButtons ->
                val updatedCallButtons = field?.invoke(callButtons.toMutableSet())
                emitCallButtons(updatedCallButtons)
            }
        }

    fun bind() {
        legacyActions?.value?.let { legacyCallActions = it }

        legacyActions?.onEach { legacyActions ->
                this@DefaultChatUIButtonsProvider.legacyCallActions = legacyActions
                _buttons.emit(legacyActions.map { it.toChatUIButtons() }.toSet())
            }?.launchIn(scope)
            ?.invokeOnCompletion {
                buttonsProvider = null
            }

        if (legacyActions == null || legacyActions.value.isEmpty()) emitCallButtons(ChatUI.Button.Collections.default)
    }

    private fun emitCallButtons(buttons: Set<ChatUI.Button>? = null) {
        buttons?.let { _buttons.tryEmit(it) }
    }

    init {
        bind()
    }
}
