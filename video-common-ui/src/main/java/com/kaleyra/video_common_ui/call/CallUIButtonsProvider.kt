package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.CallUI.Button
import com.kaleyra.video_common_ui.utils.extensions.CallExtensions.toCallUIButton
import com.kaleyra.video_common_ui.utils.extensions.CallTypeExtensions.toCallButtons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

interface CallUIButtonsProvider {
    /**
     * Current Call UI buttons
     */
    val buttons: StateFlow<Set<Button>>

    /**
     * Buttons Provider represents an optional callback that is invoked when call buttons hierarchy is about to being displayed on call UI.
     * This callback allows to return an updated set of Call Buttons if some call button must be added or removed in the integration use case.
     */
    var buttonsProvider: ((MutableSet<CallUI.Button>) -> Set<CallUI.Button>)?
}

internal class DefaultCallUIButtonsProvider(
    val callType: StateFlow<Call.PreferredType>,
    val callState: StateFlow<Call.State>,
    val legacyActions: MutableStateFlow<Set<CallUI.Action>>? = null,
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) : CallUIButtonsProvider {

    private var legacyCallActions: Set<CallUI.Action>? = null

    private val _buttons: MutableStateFlow<Set<Button>> = MutableStateFlow(setOf())

    override val buttons: StateFlow<Set<Button>> = _buttons

    private val baseButtonProvider: ((MutableSet<CallUI.Button>) -> Set<CallUI.Button>) = { callButtons ->
        val updatedButtons = legacyCallActions?.map { it.toCallUIButton() }?.toSet() ?: callButtons
        emitCallButtons(updatedButtons)
        updatedButtons
    }

    override var buttonsProvider: ((MutableSet<CallUI.Button>) -> Set<CallUI.Button>)? = baseButtonProvider
        set(value) {
            if (value == null && callState.value is Call.State.Disconnected.Ended) field = null
            else {
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
        }

    fun bind() {
        legacyActions?.value?.let { legacyCallActions = it }

        legacyActions?.combine(callState) { actions, callState -> actions to callState }
            ?.takeWhile { (_, callState) ->
                callState !is Call.State.Disconnected.Ended
            }?.onEach { (actions, _) ->
                this@DefaultCallUIButtonsProvider.legacyCallActions = actions
                _buttons.emit(actions.map { it.toCallUIButton() }.toSet())
            }?.launchIn(scope)
            ?.invokeOnCompletion {
                println("removed")
                buttonsProvider = null
            }

        callType.combine(callState) { callType, callState -> callType to callState }
            .takeWhile { (_, callState) ->
                callState !is Call.State.Disconnected.Ended
            }
            .onEach { (callType, _) ->
                val updatedCallButtons = buttonsProvider?.invoke(callType.toCallButtons(legacyCallActions))
                emitCallButtons(updatedCallButtons)
            }.launchIn(scope)
    }

    private fun emitCallButtons(buttons: Set<Button>? = null) {
        _buttons.value.filterIsInstance<CallUI.Button.Custom>().forEach { it.onButtonUpdated = null }
        buttons?.let { _buttons.tryEmit(it) }
        buttons?.filterIsInstance<CallUI.Button.Custom>()?.forEach { customButton ->
            customButton.onButtonUpdated = {
                val actualButtons = _buttons.value.toMutableSet()
                _buttons.tryEmit(actualButtons.filterNot { it == customButton }.toSet())
                _buttons.tryEmit(actualButtons)
            }
        }
    }

    init {
        bind()
    }
}
