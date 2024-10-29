package com.kaleyra.video_sdk.call.pip.view

import android.util.Rational
import android.util.Size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video.conference.VideoStreamView
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_common_ui.utils.MathUtils
import com.kaleyra.video_sdk.call.stream.model.StreamUiState
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.view.AdaptiveStreamLayout
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.items.MoreParticipantsItem
import com.kaleyra.video_sdk.call.stream.view.items.NonDisplayedParticipantData
import com.kaleyra.video_sdk.call.stream.view.items.StreamItem
import com.kaleyra.video_sdk.call.stream.view.items.StreamStatusIcons
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.preCallStreamViewSettings
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList

internal val DefaultPipAspectRatio = Rational(9, 16)
internal const val PipStreamComponentTag = "PipStreamComponentTag"

@Composable
internal fun PipStreamComponent(
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = StreamViewModel.provideFactory(configure = ::requestCollaborationViewModelConfiguration)
    ),
    onPipAspectRatio: (Rational) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PipStreamComponent(
        uiState = uiState,
        onPipAspectRatio = onPipAspectRatio,
        modifier = modifier
    )
}

@Composable
internal fun PipStreamComponent(
    uiState: StreamUiState,
    onPipAspectRatio: (Rational) -> Unit,
    modifier: Modifier = Modifier
) {
    val streamsToDisplay = remember(uiState) { streamsToDisplayFor(uiState) }
    val aspectRatioView = remember(streamsToDisplay) {
        if (streamsToDisplay.size == 1) {
            streamsToDisplay[0].video?.view?.value as? VideoStreamView
        } else null
    }

    LaunchedEffect(aspectRatioView) {
        if (aspectRatioView == null) onPipAspectRatio.invoke(DefaultPipAspectRatio)
        else {
            aspectRatioView.videoSize.collect { size ->
                onPipAspectRatio.invoke(computePipAspectRatio(size))
            }
        }
    }

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

            StreamStatusIcons(
                uiState.preview.audio,
                uiState.preview.video,
                fullscreen = false,
                mine = true,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
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
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
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
    val pinnedStreams = uiState.pinnedStreams.value.filterNot { it.isMine && it.video?.isScreenShare == true }
    val fullscreenStream = uiState.fullscreenStream

    return when {
        fullscreenStream != null -> listOf(fullscreenStream)
        pinnedStreams.isNotEmpty() -> pinnedStreams.take(2)
        else -> streams.filter { !it.isMine }.take(2)
    }
}

private fun computePipAspectRatio(size: Size): Rational {
    val gcd = MathUtils.findGreatestCommonDivisor(size.width, size.height)
    return if (gcd != 0) Rational(size.width / gcd, size.height / gcd)
    else Rational.NaN
}