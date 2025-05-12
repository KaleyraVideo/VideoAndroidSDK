package com.kaleyra.video_sdk.call.participants

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.bottomsheet.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.participants.view.AdminBottomSheetContent
import com.kaleyra.video_sdk.call.participants.view.ParticipantItem
import com.kaleyra.video_sdk.call.participants.view.ParticipantsTopAppBar
import com.kaleyra.video_sdk.call.participants.view.StreamsLayoutSelector
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.common.user.UserInfo
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.launch

internal const val AdminBottomSheetTag = "AdminBottomSheetTag"

@Composable
internal fun ParticipantsComponent(
    modifier: Modifier = Modifier,
    viewModel: ParticipantsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ParticipantsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    onDismiss: () -> Unit,
    isLargeScreen: Boolean = false
) {
    val activity = LocalContext.current.findActivity()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ParticipantsComponent(
        streamsLayout = uiState.streamsLayout,
        streams = uiState.streams,
        invited = uiState.invitedParticipants,
        adminsStreamsIds = uiState.adminsStreamIds,
        pinnedStreamsIds = uiState.pinnedStreamIds,
        amIAdmin = uiState.isLocalParticipantAdmin,
        isPinLimitReached = uiState.hasReachedMaxPinnedStreams,
        participantsCount = uiState.joinedParticipantCount,
        onLayoutClick = remember(viewModel) {
            { layout ->
                when (layout) {
                    StreamsLayout.Auto -> viewModel.switchToAutoLayout()
                    else -> viewModel.switchToManualLayout()
                }
            }
        },
        onMuteStreamClick = remember(viewModel) {
            { streamId, _ -> viewModel.muteStreamAudio(streamId) }
        },
        onDisableMicClick = remember(viewModel) {
            { streamId, _ ->
                val isMyStream = streamId == CameraStreamConstants.CAMERA_STREAM_ID
                if (isMyStream) viewModel.toggleMic(activity)
            }
        },
        onPinStreamClick = remember(viewModel) {
            { streamId, pin ->
                if (pin) viewModel.pinStream(streamId)
                else viewModel.unpinStream(streamId)
            }
        },
        onKickParticipantClick = {},
        onCloseClick = onDismiss,
        isLargeScreen = isLargeScreen,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsComponent(
    streamsLayout: StreamsLayout,
    streams: ImmutableList<StreamUi>,
    invited: ImmutableList<String>,
    adminsStreamsIds: ImmutableList<String>,
    pinnedStreamsIds: ImmutableList<String>,
    amIAdmin: Boolean,
    isPinLimitReached: Boolean,
    participantsCount: Int,
    onLayoutClick: (layout: StreamsLayout) -> Unit,
    onMuteStreamClick: (streamId: String, mute: Boolean) -> Unit,
    onDisableMicClick: (streamId: String, disable: Boolean) -> Unit,
    onPinStreamClick: (streamId: String, pin: Boolean) -> Unit,
    onKickParticipantClick: (streamId: String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ParticipantsTopAppBar(
                participantsCount = participantsCount,
                onBackPressed = onCloseClick,
                isLargeScreen = isLargeScreen
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) { contentPadding ->
        var bottomSheetStream by remember { mutableStateOf<StreamUi?>(null) }

        bottomSheetStream?.apply {
            val sheetState = rememberModalBottomSheetState(true)
            val scope = rememberCoroutineScope()
            val animateToDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        bottomSheetStream = null
                    }
                }
            }

            ModalBottomSheet(
                sheetState = sheetState,
                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                onDismissRequest = { bottomSheetStream = null },
                dragHandle = { CallBottomSheetDefaults.HDragHandle() },
                modifier = Modifier.testTag(AdminBottomSheetTag),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                AdminBottomSheetContent(
                    stream = this@apply,
                    isStreamPinned = pinnedStreamsIds.value.contains(id),
                    isPinLimitReached = isPinLimitReached,
                    onMuteStreamClick = { streamId, value ->
                        onMuteStreamClick(streamId, value)
                        animateToDismiss()
                    },
                    onPinStreamClick = { streamId, value ->
                        onPinStreamClick(streamId, value)
                        animateToDismiss()
                    },
                    onKickParticipantClick = { streamId ->
                        onKickParticipantClick(streamId)
                        animateToDismiss()
                    }
                )
            }
        }

        Column(Modifier.fillMaxSize()) {
            val layoutDirection = LocalLayoutDirection.current
            val start = contentPadding.calculateStartPadding(layoutDirection)
            val top = contentPadding.calculateTopPadding()
            val end = contentPadding.calculateEndPadding(layoutDirection)
            val bottom = contentPadding.calculateBottomPadding()

            Column(
                modifier = Modifier.padding(start = start, top = top, end = end)
            ) {
                Text(
                    text = stringResource(id = R.string.kaleyra_participants_component_change_layout),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, top = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                StreamsLayoutSelector(
                    streamsLayout = streamsLayout,
                    onLayoutClick = onLayoutClick,
                    modifier = Modifier
                        .padding(horizontal = 40.dp)
                )
                Spacer(Modifier.height(32.dp))
            }
            Box(Modifier
                .weight(1f)
                .padding(horizontal = 32.dp)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = start, end = end, bottom = bottom),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp),
                ) {
                    item {
                        Text(
                            text = stringResource(id = R.string.kaleyra_participants_component_users_in_call),
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )
                    }
                    items(items = streams.value, key = { it.id }) { stream ->
                        ParticipantItem(
                            modifier = Modifier.padding(start = 8.dp),
                            stream = stream,
                            isPinned = pinnedStreamsIds.value.contains(stream.id),
                            isAdminStream = adminsStreamsIds.value.contains(stream.id),
                            amIAdmin = amIAdmin,
                            isPinLimitReached = isPinLimitReached,
                            onMuteStreamClick = onMuteStreamClick,
                            onDisableMicClick = onDisableMicClick,
                            onPinStreamClick = onPinStreamClick,
                            onMoreClick = { bottomSheetStream = stream }
                        )
                    }

                    if (invited.count() > 0) {
                        item {
                            Spacer(Modifier.height(32.dp))
                        }

                        item {
                            Text(
                                text = stringResource(R.string.kaleyra_strings_info_participant_invited),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                        }

                        items(items = invited.value) { username ->
                            Text(
                                text = username,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, bottom = 16.dp)
                            )
                        }
                    }
                }
            }

            NavigationBarsSpacer()
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun ParticipantsComponentPreview() {
    KaleyraTheme {
        ParticipantsComponent(
            streamsLayout = StreamsLayout.Mosaic,
            adminsStreamsIds = ImmutableList(listOf("id1")),
            amIAdmin = true,
            streams = ImmutableList(
                listOf(
                    StreamUi("id1", UserInfo("userId1", "Marina", ImmutableUri()), true, null, null),
                    StreamUi("id2", UserInfo("userId2", "Viola Allen", ImmutableUri()), false, null, null),
                )
            ),
            pinnedStreamsIds = ImmutableList(),
            isPinLimitReached = false,
            participantsCount = 2,
            invited = ImmutableList(
                listOf(
                    "Mario Rossi",
                    "Alessandra Scaramelli",
                )
            ),
            onLayoutClick = { },
            onDisableMicClick = { _, _ -> },
            onKickParticipantClick = {},
            onPinStreamClick = { _, _ -> },
            onMuteStreamClick = { _, _ -> },
            onCloseClick = { }
        )
    }
}