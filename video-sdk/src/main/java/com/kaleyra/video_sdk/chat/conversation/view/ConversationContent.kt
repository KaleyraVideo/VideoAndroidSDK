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

package com.kaleyra.video_sdk.chat.conversation.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.model.Message
import com.kaleyra.video_sdk.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.video_sdk.chat.conversation.view.item.DayHeaderItem
import com.kaleyra.video_sdk.chat.conversation.view.item.MyMessageItem
import com.kaleyra.video_sdk.chat.conversation.view.item.NewMessagesHeaderItem
import com.kaleyra.video_sdk.chat.conversation.view.item.OtherMessageItem
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal const val MessageStateTag = "MessageStateTag"
internal const val ConversationContentTag = "ConversationContentTag"
internal const val ProgressIndicatorTag = "ProgressIndicatorTag"

internal val ConversationContentPadding = 16.dp

@Composable
internal fun ConversationContent(
    items: ImmutableList<ConversationItem>,
    participantsDetails: ImmutableMap<String, ChatParticipantDetails>?,
    isFetching: Boolean,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(vertical = ConversationContentPadding),
        modifier = Modifier
            .testTag(ConversationContentTag)
            .then(modifier)
    ) {
        items(items.value, key = { it.id }, contentType = { it::class.java }) { item ->
            when (item) {
                is ConversationItem.Message -> {
                    when (val message = item.message) {
                        is Message.OtherMessage -> OtherMessageItem(
                            message = message,
                            isFirstChainMessage = item.isFirstChainMessage,
                            isLastChainMessage = item.isLastChainMessage,
                            participantDetails = participantsDetails?.get(message.userId),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PaddingValues(horizontal = ConversationContentPadding))
                        )

                        is Message.MyMessage -> MyMessageItem(
                            message = message,
                            isFirstChainMessage = item.isFirstChainMessage,
                            isLastChainMessage = item.isLastChainMessage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(PaddingValues(horizontal = ConversationContentPadding))
                        )
                    }
                }

                is ConversationItem.Day ->
                    Box(modifier = Modifier.padding(vertical = 6.dp)) {
                        DayHeaderItem(
                            timestamp = item.timestamp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                is ConversationItem.UnreadMessages ->
                    Box(modifier = Modifier.padding(vertical = 12.dp)) {
                        NewMessagesHeaderItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
            }
        }
        if (isFetching) {
            item {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(28.dp)
                        .testTag(ProgressIndicatorTag)
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ConversationContentPreview() = KaleyraTheme {
    Surface {
        ConversationContent(
            items = mockConversationElements,
            participantsDetails = ImmutableMap(),
            isFetching = false,
            scrollState = rememberLazyListState()
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ConversationContentGroupPreview() = KaleyraTheme {
    Surface {
        ConversationContent(
            items = mockConversationElements,
            participantsDetails = ImmutableMap(
                hashMapOf(
                    "userId1" to ChatParticipantDetails("Enea"),
                    "userId4" to ChatParticipantDetails("Luca"),
                    "userId5" to ChatParticipantDetails("Franco"),
                    "userId7" to ChatParticipantDetails("Francesco"),
                    "userId8" to ChatParticipantDetails("Marco"),
                )
            ),
            isFetching = false,
            scrollState = rememberLazyListState()
        )
    }
}
