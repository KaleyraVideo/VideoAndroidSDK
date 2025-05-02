package com.kaleyra.video_sdk.call.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal enum class ModularComponent {
    Audio,
    ScreenShare,
    FileShare,
    Whiteboard,
    SignDocuments,
    SignDocumentView,
    VirtualBackground,
    Participants,
    Chat
}