package com.kaleyra.video_sdk.call.callinfo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfo.model.CallInfoUiState
import com.kaleyra.video_sdk.call.callinfo.model.TextRef
import com.kaleyra.video_sdk.call.mapper.CallStateMapper.toCallStateUi
import com.kaleyra.video_sdk.call.mapper.ParticipantMapper.toOtherDisplayNames
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.viewmodel.BaseViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallInfoViewModel(configure: suspend () -> Configuration) : BaseViewModel<CallInfoUiState>(configure) {
    override fun initialState() = CallInfoUiState()

    init {
        viewModelScope.launch {
            call.first()
            observeCallStates()
        }
    }

    private fun observeCallStates() {
        val call = call.getValue()!!
        var hasBeenConnectingOnce = false
        combine(call.toCallStateUi(), call.toOtherDisplayNames()) { callUiState, otherDisplayNames ->
            callUiState to otherDisplayNames
        }.onEach { combinedFlows ->
            if (combinedFlows.first == CallStateUi.Connecting) {
                if (hasBeenConnectingOnce) return@onEach
                hasBeenConnectingOnce = true
            }
            val callStateUi = combinedFlows.first
            val displayNames = combinedFlows.second
            val displayState = callStateUi.toTextRef(call)
            _uiState.update { it.copy(callStateUi = callStateUi, displayNames = ImmutableList(displayNames), displayState = displayState) }
        }.launchIn(viewModelScope)
    }

    companion object {
        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CallInfoViewModel(configure) as T
                }
            }
    }
}

internal fun CallStateUi.toTextRef(call: CallUI?): TextRef? {
    call ?: return null
    return when (this@toTextRef) {
        CallStateUi.Connecting -> TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting)
        CallStateUi.Dialing -> TextRef.StringResource(R.string.kaleyra_call_status_dialing)
        CallStateUi.Ringing -> TextRef.PluralResource(R.plurals.kaleyra_call_incoming_status_ringing, call.participants.value.others.size)
        CallStateUi.RingingRemotely -> TextRef.StringResource(R.string.kaleyra_call_status_ringing)
        CallStateUi.Reconnecting -> TextRef.StringResource(R.string.kaleyra_strings_info_status_connecting)
        CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice -> TextRef.StringResource(R.string.kaleyra_call_status_answered_on_other_device)
        CallStateUi.Disconnected.Ended.Declined -> TextRef.StringResource(R.string.kaleyra_strings_info_call_declined)
        CallStateUi.Disconnected.Ended.LineBusy -> TextRef.StringResource(R.string.kaleyra_call_status_ended_line_busy)
        CallStateUi.Disconnected.Ended.CurrentUserInAnotherCall -> TextRef.StringResource(R.string.kaleyra_current_user_busy)
        CallStateUi.Disconnected.Ended.Timeout -> TextRef.StringResource(R.string.kaleyra_strings_info_call_no_answer)
        CallStateUi.Disconnected.Ended.Error,
        CallStateUi.Disconnected.Ended.Error.Server,
        CallStateUi.Disconnected.Ended.Error.Unknown -> TextRef.StringResource(R.string.kaleyra_call_failed)
        else -> null
    }
}
