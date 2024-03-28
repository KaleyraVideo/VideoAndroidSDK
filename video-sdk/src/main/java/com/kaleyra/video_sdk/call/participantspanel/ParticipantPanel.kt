package com.kaleyra.video_sdk.call.participantspanel

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.contentColorFor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantItem
import com.kaleyra.video_sdk.call.participantspanel.view.ParticipantsTopAppBar
import com.kaleyra.video_sdk.call.stream.model.AudioUi
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.launch

private val KickParticipantColor = Color(0xFFAE1300)

@Immutable
internal enum class StreamsLayout {
    Grid,
    Pin
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsPanel(
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
    Surface {
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
                val scope = rememberCoroutineScope()
                val sheetState = rememberModalBottomSheetState(true)
                ModalBottomSheet(
                    sheetState = sheetState,
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                    onDismissRequest = { bottomSheetStream = null }
                ) {
                    AdminBottomSheetContent(
                        avatar = avatar,
                        username = username,
                        streamId = id,
                        audio = audio,
                        onMuteStreamClick = { streamId, value ->
                            onMuteStreamClick(streamId, value)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    bottomSheetStream = null
                                }
                            }
                        },
                        onPinStreamClick = { streamId, value ->
                            onPinStreamClick(streamId, value)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    bottomSheetStream = null
                                }
                            }
                        },
                        onKickParticipantClick = { streamId ->
                            onKickParticipantClick(streamId)
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    bottomSheetStream = null
                                }
                            }
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
                        text = stringResource(id = R.string.kaleyra_participants_panel_change_layout),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    StreamsLayoutSelector(
                        streamsLayout,
                        onLayoutClick
                    )
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(id = R.string.kaleyra_participants_panel_users_in_call),
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(items = streams.value, key = { it.id }) { stream ->
                    ParticipantItem(
                        avatar = stream.avatar,
                        title = if (stream.mine) stringResource(
                            id = R.string.kaleyra_participants_panel_you,
                            stream.username
                        ) else stream.username,
                        subtitle = stringResource(
                            when {
                                stream.video?.isScreenShare == true -> R.string.kaleyra_participants_panel_screenshare
                                adminsStreamsIds.value.contains(stream.id) -> R.string.kaleyra_participants_panel_admin
                                else -> R.string.kaleyra_participants_panel_participant
                            }
                        )
                    ) {
                        if (amIAdmin || stream.mine) {
                            DisableMicButton(
                                streamId = stream.id,
                                audio = stream.audio,
                                onClick = onDisableMicClick
                            )
                        } else {
                            MuteForYouButton(
                                streamId = stream.id,
                                audio = stream.audio,
                                onClick = onMuteStreamClick
                            )
                        }

                        if (!amIAdmin || stream.mine) {
                            PinButton(
                                streamId = stream.id,
                                pinnedStreamsIds = pinnedStreamsIds,
                                onClick = onPinStreamClick
                            )
                        } else {
                            ShowAdminModalSheetButton(onClick = { bottomSheetStream = stream })
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        fontWeight = FontWeight.SemiBold,
                        text = stringResource(R.string.kaleyra_participants_panel_users_invited),
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
}

@Composable
internal fun AdminBottomSheetContent(
    username: String,
    avatar: ImmutableUri?,
    streamId: String,
    audio: AudioUi?,
    onMuteStreamClick: (streamId: String, mute: Boolean) -> Unit,
    onPinStreamClick: (streamId: String, pin: Boolean) -> Unit,
    onKickParticipantClick: (streamId: String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 52.dp)) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 27.dp, vertical = 12.dp)
            ) {
                Avatar(
                    uri = avatar,
                    contentDescription = stringResource(id = R.string.kaleyra_avatar),
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = contentColorFor(MaterialTheme.colorScheme.primary),
                    size = 34.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = username,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        item {
            AdminSheetItem(
                text = stringResource(id = R.string.kaleyra_participants_panel_pin),
                painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_pin),
                onClick = { /* TODO */ }
            )
        }

        item {
            AdminSheetItem(
                text = stringResource(id = if (audio == null || audio.isMutedForYou) R.string.kaleyra_participants_panel_unmute_for_you else R.string.kaleyra_participants_panel_mute_for_you),
                painter = painterResource(id = if (audio == null || audio.isMutedForYou) R.drawable.ic_kaleyra_participant_panel_speaker_off else R.drawable.ic_kaleyra_participant_panel_speaker_on),
                onClick = { if (audio != null) onMuteStreamClick(streamId, !audio.isMutedForYou) else Unit }
            )
        }

        item {
            AdminSheetItem(
                text = stringResource(id = R.string.kaleyra_participants_panel_remove_from_call),
                painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_kick),
                color = KickParticipantColor,
                onClick = { onKickParticipantClick(streamId) }
            )
        }
    }
}

@Composable
private fun AdminSheetItem(
    text: String,
    painter: Painter,
    color: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp)
    ) {
        Icon(
            tint = color,
            painter = painter,
            modifier = Modifier.size(24.dp),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(26.dp))
        Text(text, color = color)
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
        ParticipantsPanel(
            companyLogo = Logo(),
            streamsLayout = streamsLayout,
            adminsStreamsIds = ImmutableList(listOf("id1")),
            amIAdmin = true,
            streams = ImmutableList(
                listOf(
                    StreamUi("id1", "username1", true, null, null),
                    StreamUi("id2", "username2", false,null, null),
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
            onDisableMicClick = {_,_ ->},
            onKickParticipantClick = {},
            onPinStreamClick = {_,_ ->},
            onMuteStreamClick = {_,_ ->},
            onCloseClick = {}
        )
    }
}