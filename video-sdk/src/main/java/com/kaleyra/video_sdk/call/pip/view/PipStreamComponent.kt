package com.kaleyra.video_sdk.call.pip.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.screen.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.streamnew.model.StreamUiState
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.view.core.Stream
import com.kaleyra.video_sdk.call.streamnew.view.items.MoreParticipantsItem
import com.kaleyra.video_sdk.call.streamnew.view.items.NonDisplayedParticipantData
import com.kaleyra.video_sdk.call.streamnew.view.items.StreamItem
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.preCallStreamViewSettings
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList

internal const val PipStreamComponentTag = "PipStreamComponentTag"

@Composable
internal fun PipStreamComponent(
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PipStreamComponent(
        uiState = uiState,
        modifier = modifier
    )
}

@Composable
internal fun PipStreamComponent(
    uiState: StreamUiState,
    modifier: Modifier = Modifier
) {
    val streamsToDisplay = remember(uiState) { streamsToDisplayFor(uiState) }

    val nonDisplayedParticipantsData = remember(uiState, streamsToDisplay) {
        val nonDisplayedStreams = uiState.streams.value.filter { !it.isMine } - streamsToDisplay.toSet()
        nonDisplayedStreams.map { NonDisplayedParticipantData(it.id, it.username, it.avatar) }.toImmutableList()
    }

    val shouldDisplayMoreParticipantItem = nonDisplayedParticipantsData.isNotEmpty() && streamsToDisplay.size > 1 && uiState.pinnedStreams.count() == 0

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.testTag(PipStreamComponentTag)
    ) {
        if (uiState.preview != null) {
            val video = uiState.preview.video
            Stream(
                streamView = video?.view?.preCallStreamViewSettings(),
                avatar = uiState.preview.avatar,
                username = uiState.preview.username ?: "",
                showStreamView = video?.view != null && video.isEnabled
            )
        } else {
            Box(modifier = modifier) {
                AdaptiveStreamLayout(
                    thumbnailSize = 0.dp,
                    thumbnailsCount = 0
                ) {
                    streamsToDisplay.fastForEachIndexed { index, stream ->
                        key(stream.id) {
                            val displayAsMoreParticipantsItem = shouldDisplayMoreParticipantItem && index == streamsToDisplay.size - 1

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                tonalElevation = 1.dp,
                                modifier = Modifier.testTag(stream.id)
                            ) {
                                Box {
                                    when {
                                        displayAsMoreParticipantsItem -> {
                                            // Add this participant to the list of non displayed participants
                                            val participants = listOf(NonDisplayedParticipantData(stream.id, stream.username, stream.avatar)) + nonDisplayedParticipantsData.value
                                            MoreParticipantsItem(participants.toImmutableList())
                                        }

                                        else -> {
                                            StreamItem(
                                                stream = stream,
                                                fullscreen = false,
                                                pin = false,
                                                statusIconsAlignment = Alignment.TopEnd
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun streamsToDisplayFor(uiState: StreamUiState): List<StreamUi> {
    val streams = uiState.streams.value
    val pinnedStreams = uiState.pinnedStreams.value
    val fullscreenStream = uiState.fullscreenStream

    return when {
        fullscreenStream != null -> listOf(fullscreenStream)
        pinnedStreams.isNotEmpty() -> pinnedStreams.take(2)
        else -> streams.filter { !it.isMine }.take(2)
    }
}