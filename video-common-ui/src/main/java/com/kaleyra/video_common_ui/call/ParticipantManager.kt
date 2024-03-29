package com.kaleyra.video_common_ui.call

import com.kaleyra.video.conference.Call
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ParticipantManager(private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {

    private var job: Job? = null

    fun bind(call: Call) {
        stop()
        keepContactDetailsUpdated(call)
    }

    fun keepContactDetailsUpdated(call: Call) {
        job = call.participants
            .onEach { participants ->
                val userIds = participants.list.map { it.userId }.toTypedArray()
                ContactDetailsManager.refreshContactDetails(*userIds)
            }
            .launchIn(coroutineScope)
    }

    fun stop() {
        job?.cancel()
    }

}