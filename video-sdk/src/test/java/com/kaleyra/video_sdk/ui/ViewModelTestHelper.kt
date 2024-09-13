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
    conference: ConferenceUI = mockk(relaxed = true),
    conversation: ConversationUI = mockk(relaxed = true),
    company: Company = mockk(relaxed = true),
    connectedUser: StateFlow<User?> = MutableStateFlow(mockk(relaxed = true)),
): CollaborationViewModel.Configuration.Success {
    return CollaborationViewModel.Configuration.Success(
        conference = conference,
        conversation = conversation,
        company = company,
        connectedUser = connectedUser
    )
}