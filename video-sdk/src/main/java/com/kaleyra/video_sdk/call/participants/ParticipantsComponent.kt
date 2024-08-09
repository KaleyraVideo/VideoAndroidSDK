package com.kaleyra.video_sdk.call.participants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.call.CameraStreamConstants
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.participants.view.AdminBottomSheetContent
import com.kaleyra.video_sdk.call.participants.view.ParticipantItem
import com.kaleyra.video_sdk.call.participants.view.ParticipantsTopAppBar
import com.kaleyra.video_sdk.call.participants.view.StreamsLayoutSelector
import com.kaleyra.video_sdk.call.participants.viewmodel.ParticipantsViewModel
import com.kaleyra.video_sdk.call.stream.model.core.StreamUi
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.toImmutableList
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.launch

internal const val AdminBottomSheetTag = "AdminBottomSheetTag"

@Composable
internal fun ParticipantsComponent(
    modifier: Modifier = Modifier,
    participantsViewModel: ParticipantsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ParticipantsViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    streamViewModel: StreamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    ),
    onDismiss: () -> Unit
) {
    val activity = LocalContext.current.findActivity()
    val participantsUiState by participantsViewModel.uiState.collectAsStateWithLifecycle()
    val streamsUiState by streamViewModel.uiState.collectAsStateWithLifecycle()

    val streamsLayout by remember {
        derivedStateOf {
            if (streamsUiState.pinnedStreams.count() > 0) StreamsLayout.Pin else StreamsLayout.Grid
        }
    }
    val pinnedStreamsIds by remember {
        derivedStateOf {
            streamsUiState.pinnedStreams.value.map { it.id }.toImmutableList()
        }
    }
    val firstStreamNotMine by remember {
        derivedStateOf {
            streamsUiState.streams.value.firstOrNull { !it.isMine }
        }
    }
    val isLocalScreenShareEnabled by remember {
        derivedStateOf {
            streamsUiState.streams.value.find { it.video?.isScreenShare == true && it.isMine } != null
        }
    }
    val isPinLimitReached by remember(streamViewModel) {
        derivedStateOf {
            streamsUiState.pinnedStreams.count() >= streamViewModel.maxPinnedStreams
        }
    }

    ParticipantsComponent(
        streamsLayout = streamsLayout,
        streams = streamsUiState.streams,
        invited = participantsUiState.invitedParticipants,
        adminsStreamsIds = participantsUiState.adminsStreamsIds,
        pinnedStreamsIds = pinnedStreamsIds,
        amIAdmin = participantsUiState.isLocalParticipantAdmin,
        enableGridLayout = !isLocalScreenShareEnabled,
        isPinLimitReached = isPinLimitReached,
        onLayoutClick = remember(streamViewModel, firstStreamNotMine) {
            { layout ->
                if (layout == StreamsLayout.Grid) streamViewModel.unpinAll()
                else firstStreamNotMine?.let { streamViewModel.pin(it.id) }
            }
        },
        onMuteStreamClick = remember(participantsViewModel) {
            { streamId, _ ->
                participantsViewModel.muteStreamAudio(streamId)
            }
        },
        onDisableMicClick = remember(participantsViewModel) {
            { streamId, _ ->
                val isMyStream = streamId == CameraStreamConstants.CAMERA_STREAM_ID
                if (isMyStream) participantsViewModel.toggleMic(activity)
            }
        },
        onPinStreamClick = remember(streamViewModel) {
            { streamId, pin ->
                if (pin) streamViewModel.pin(streamId)
                else streamViewModel.unpin(streamId)
            }
        } ,
        onKickParticipantClick = {},
        onCloseClick = onDismiss,
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
    enableGridLayout: Boolean,
    isPinLimitReached: Boolean,
    onLayoutClick: (layout: StreamsLayout) -> Unit,
    onMuteStreamClick: (streamId: String, mute: Boolean) -> Unit,
    onDisableMicClick: (streamId: String, disable: Boolean) -> Unit,
    onPinStreamClick: (streamId: String, pin: Boolean) -> Unit,
    onKickParticipantClick: (streamId: String) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ParticipantsTopAppBar(
                participantsCount = streams.count(),
                scrollBehavior = scrollBehavior,
                onBackPressed = onCloseClick
            )
        }
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
                modifier = Modifier.testTag(AdminBottomSheetTag)
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
            Box(Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),
                    contentPadding = PaddingValues(start = 38.dp, end = 38.dp, bottom = 38.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(id = R.string.kaleyra_participants_component_change_layout),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        StreamsLayoutSelector(
                            streamsLayout = streamsLayout,
                            enableGridLayout = enableGridLayout,
                            onLayoutClick = onLayoutClick
                        )
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                    }

                    item {
                        Text(
                            text = stringResource(id = R.string.kaleyra_participants_component_users_in_call),
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(items = streams.value, key = { it.id }) { stream ->
                        ParticipantItem(
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
                            Spacer(Modifier.height(16.dp))
                        }

                        item {
                            Text(
                                fontWeight = FontWeight.SemiBold,
                                text = stringResource(R.string.kaleyra_participants_component_users_invited),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        items(items = invited.value) { username ->
                            Text(
                                text = username,
                                modifier = Modifier.fillMaxWidth()
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
            streamsLayout = StreamsLayout.Grid,
            adminsStreamsIds = ImmutableList(listOf("id1")),
            amIAdmin = true,
            streams = ImmutableList(
                listOf(
                    StreamUi("id1", "Marina", true, null, null),
                    StreamUi("id2", "Viola Allen", false, null, null),
                )
            ),
            pinnedStreamsIds = ImmutableList(),
            enableGridLayout = true,
            isPinLimitReached = false,
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