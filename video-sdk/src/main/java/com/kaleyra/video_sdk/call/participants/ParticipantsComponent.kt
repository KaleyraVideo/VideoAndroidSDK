package com.kaleyra.video_sdk.call.participants

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.call.participants.view.AdminBottomSheetContent
import com.kaleyra.video_sdk.call.participants.view.ParticipantItem
import com.kaleyra.video_sdk.call.participants.view.ParticipantsTopAppBar
import com.kaleyra.video_sdk.call.participants.view.StreamsLayoutSelector
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsComponent(
    companyLogo: Logo,
    streamsLayout: StreamsLayout,
    streams: ImmutableList<StreamUi>,
    invited: ImmutableList<String>,
    adminsStreamsIds: ImmutableList<String>,
    pinnedStreamsIds: ImmutableList<String>,
    amIAdmin: Boolean,
    onLayoutClick: (layout: StreamsLayout) -> Unit,
    onMuteStreamClick: (streamId: String, mute: Boolean) -> Unit,
    onDisableMicClick: (streamId: String, disable: Boolean) -> Unit,
    onPinStreamClick: (streamId: String, pin: Boolean) -> Unit,
    onKickParticipantClick: (streamId: String) -> Unit,
    onCloseClick: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ParticipantsTopAppBar(
                companyLogo = companyLogo,
                participantsCount = streams.count(),
                scrollBehavior = scrollBehavior,
                onCloseClick = onCloseClick
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
                onDismissRequest = { bottomSheetStream = null }
            ) {
                AdminBottomSheetContent(
                    avatar = avatar,
                    username = username,
                    streamId = id,
                    streamAudio = audio,
                    streamPinned = pinnedStreamsIds.value.contains(id),
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
                StreamsLayoutSelector(streamsLayout, onLayoutClick)
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
                    pinned = pinnedStreamsIds.value.contains(stream.id),
                    admin = adminsStreamsIds.value.contains(stream.id),
                    enableAdminSheet = amIAdmin,
                    onMuteStreamClick = onMuteStreamClick,
                    onDisableMicClick = onDisableMicClick,
                    onPinStreamClick = onPinStreamClick,
                    onMoreClick = { bottomSheetStream = stream }
                )
            }

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
                Text(username, Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantPanelPreview() {
    KaleyraM3Theme {
        var streamsLayout by remember {
            mutableStateOf(StreamsLayout.Grid)
        }
        ParticipantsComponent(
            companyLogo = Logo(),
            streamsLayout = streamsLayout,
            adminsStreamsIds = ImmutableList(listOf("id1")),
            amIAdmin = true,
            streams = ImmutableList(
                listOf(
                    StreamUi("id1", "username1", true, null, null),
                    StreamUi("id2", "username2", false, null, null),
                    StreamUi("id3", "username3", false, null, null),
                    StreamUi("id4", "username4", false, null, null)
                )
            ),
            pinnedStreamsIds = ImmutableList(),
            invited = ImmutableList(
                listOf(
                    "ciao",
                    "ciao1",
                    "ciao2",
                    "ciao3",
                    "ciao4",
                    "ciao5",
                    "ciao6",
                    "ciao7",
                    "ciao8",
                    "ciao9",
                    "ciao10",
                    "ciao11",
                    "ciao12",
                    "ciao13",
                    "ciao14",
                    "ciao15",
                    "ciao16",
                    "ciao17",
                    "ciao18",
                    "ciao19",
                    "ciao20",
                    "ciao21"
                )
            ),
            onLayoutClick = { streamsLayout = it },
            onDisableMicClick = { _, _ -> },
            onKickParticipantClick = {},
            onPinStreamClick = { _, _ -> },
            onMuteStreamClick = { _, _ -> },
            onCloseClick = {}
        )
    }
}