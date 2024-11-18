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

package com.kaleyra.video_sdk.chat.conversation

import android.content.res.Configuration
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.model.ConversationState
import com.kaleyra.video_sdk.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.video_sdk.chat.conversation.view.ConversationContent
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val TOP_THRESHOLD = 15

private val ScrollToBottomThreshold = 128.dp

private val LazyListState.isApproachingTop: Boolean
    get() = derivedStateOf {
        val totalItemsCount = layoutInfo.totalItemsCount
        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        totalItemsCount != 0 && totalItemsCount <= lastVisibleItemIndex + TOP_THRESHOLD
    }.value

@Composable
internal fun scrollToBottomFabEnabled(listState: LazyListState): State<Boolean> {
    val resetScrollThreshold = with(LocalDensity.current) { ScrollToBottomThreshold.toPx() }
    return remember {
        derivedStateOf {
            val firstCompletelyVisibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            val firstCompletelyVisibleItemIndex = firstCompletelyVisibleItem?.index ?: 0
            val firstCompletelyVisibleItemOffset = -(firstCompletelyVisibleItem?.offset ?: 0)
            firstCompletelyVisibleItemIndex != 0 || firstCompletelyVisibleItemOffset > resetScrollThreshold
        }
    }
}

@Composable
internal fun ConversationComponent(
    modifier: Modifier = Modifier,
    conversationState: ConversationState,
    participantsDetails: ImmutableMap<String, ChatParticipantDetails>? = null,
    onMessageScrolled: (ConversationItem.Message) -> Unit = { },
    onApproachingTop: () -> Unit = { },
    scrollState: LazyListState = rememberLazyListState(),
) {
    val screenHeight = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }

    LaunchedEffect(scrollState) {
        val index = conversationState.conversationItems?.value?.indexOfFirst { it is ConversationItem.UnreadMessages } ?: -1
        if (index != -1) {
            scrollState.scrollToItem(index)
            scrollState.scrollBy(-screenHeight * 2 / 3f)
        }
    }

    LaunchedEffect(scrollState, conversationState.conversationItems) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .onEach {
                val item = conversationState.conversationItems?.value?.getOrNull(it) as? ConversationItem.Message ?: return@onEach
                onMessageScrolled(item)
            }.launchIn(this)
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isApproachingTop }
            .filter { it }
            .onEach { onApproachingTop() }
            .launchIn(this)
    }

    LaunchedEffect(conversationState.conversationItems) {
        if (scrollState.firstVisibleItemIndex < 3) scrollState.animateScrollToItem(0)
    }

    Box(
        modifier = Modifier
            .then(modifier)
    ) {
        if (conversationState.conversationItems == null) LoadingMessagesLabel(Modifier.align(Alignment.Center))
        else if (conversationState.conversationItems.value.isEmpty()) NoMessagesLabel(Modifier.align(Alignment.Center))
        else {
            ConversationContent(
                items = conversationState.conversationItems,
                participantsDetails = participantsDetails,
                isFetching = conversationState.isFetching,
                scrollState = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
internal fun LoadingMessagesLabel(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_channel_loading),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
internal fun NoMessagesLabel(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_no_messages_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(id = R.string.kaleyra_chat_no_messages_subtitle),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun LoadingConversationComponentPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colorScheme.surface) {
        ConversationComponent(
            conversationState = ConversationState(),
            onMessageScrolled = { },
            onApproachingTop = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun EmptyConversationComponentPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colorScheme.surface) {
        ConversationComponent(
            conversationState = ConversationState(conversationItems = ImmutableList(listOf())),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ConversationComponentPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colorScheme.surface) {
        ConversationComponent(
            conversationState = ConversationState(conversationItems = mockConversationElements),
            modifier = Modifier.fillMaxSize()
        )
    }
}
