package com.kaleyra.video_sdk.ui

import com.kaleyra.video.Company
import com.kaleyra.video.User
import com.kaleyra.video_common_ui.CollaborationViewModel
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.ConversationUI
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal fun mockkSuccessfulConfiguration(
    conference: ConferenceUI = mockk(),
    conversation: ConversationUI = mockk(),
    company: Company = mockk(),
    connectedUser: StateFlow<User?> = MutableStateFlow(mockk()),
): CollaborationViewModel.Configuration.Success {
    return CollaborationViewModel.Configuration.Success(
        conference = conference,
        conversation = conversation,
        company = company,
        connectedUser = connectedUser
    )
}