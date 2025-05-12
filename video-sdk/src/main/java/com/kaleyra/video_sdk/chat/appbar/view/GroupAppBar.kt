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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.chat.appbar.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantsState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.appbar.model.mockActions
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun GroupAppBar(
    image: ImmutableUri,
    name: String,
    connectionState: ConnectionState,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    scrollState: LazyListState,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    participantsDetails: ImmutableMap<String, ChatParticipantDetails>,
    participantsState: ChatParticipantsState,
    isInCall: Boolean,
    actions: ImmutableSet<ChatAction>,
    onBackPressed: () -> Unit = { }
) {
    ChatAppBar(
        actions = actions,
        isInCall = isInCall,
        onBackPressed = onBackPressed,
        scrollBehavior = scrollBehavior,
        windowInsets = windowInsets,
        scrollState = scrollState,
        content = {
            ChatAppBarContent(
                image = image,
                title = name,
                subtitle = textFor(connectionState, participantsState, participantsDetails),
                typingDots = participantsState.typing.count() > 0,
                isGroup = true
            )
        }
    )
}

@Composable
private fun textFor(
    connectionState: ConnectionState,
    participantsState: ChatParticipantsState,
    participantsDetails: ImmutableMap<String, ChatParticipantDetails>
): String {
    val typingCount = participantsState.typing.count()
    val onlineCount = participantsState.online.count()
    return when {
        connectionState is ConnectionState.Offline -> stringResource(R.string.kaleyra_strings_info_offline)
        connectionState is ConnectionState.Connecting -> stringResource(R.string.kaleyra_chat_state_connecting)
        typingCount == 1 -> pluralStringResource(
            id = R.plurals.kaleyra_call_participants_typing,
            count = 1,
            participantsState.typing.value.first()
        )

        typingCount > 1 -> pluralStringResource(
            id = R.plurals.kaleyra_call_participants_typing,
            count = typingCount,
            typingCount
        )

        onlineCount == 1 -> participantsState.online.value.first() + " " + stringResource(
            R.string.kaleyra_chat_participants_is_online
        )

        onlineCount > 0 -> stringResource(
            R.string.kaleyra_chat_participants_online,
            onlineCount
        )

        else -> participantsDetails.value.values.joinToString(", ") { it.username }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun GroupAppBarPreview() = KaleyraTheme {
    GroupAppBar(
        image = ImmutableUri(),
        name = "Trip Crashers",
        connectionState = ConnectionState.Connected,
        participantsDetails = ImmutableMap(
            mapOf(
                "userId1" to ChatParticipantDetails("John Smith"),
                "userId2" to ChatParticipantDetails("Jack Daniels")
            )
        ),
        participantsState = ChatParticipantsState(typing = ImmutableList(listOf("Gianni", "Muzio"))),
        isInCall = false,
        actions = mockActions,
        onBackPressed = { },
        scrollState = rememberLazyListState()
    )
}
