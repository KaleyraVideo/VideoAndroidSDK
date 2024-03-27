package com.kaleyra.video_sdk.call.participantspanel

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.stream.model.AudioUi
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

private val KickParticipantColor = Color(0xFFAE1300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsPanel(
    companyLogo: Logo,
    streams: ImmutableList<StreamUi>,
    invited: ImmutableList<String>,
    adminsStreamsIds: ImmutableList<String>,
    pinnedStreamsIds: ImmutableList<String>,
    amIAdmin: Boolean,
    onLayoutClick: (isGridLayout: Boolean) -> Unit,
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
            var isSheetOpen by rememberSaveable { mutableStateOf(false) }

            if (isSheetOpen) {
                ModalBottomSheet(
                    shape = RoundedCornerShape(16.dp),
                    onDismissRequest = { isSheetOpen = false }
                ) {
                    AdminBottomSheetContent(
                        avatar = ImmutableUri(),
                        username = "username hardcoded"
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
                    StreamsLayoutSelector(onLayoutClick)
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

                        if (amIAdmin) {
                            ShowAdminModalSheetButton(onClick = { isSheetOpen = true })
                        } else {
                            PinButton(
                                streamId = stream.id,
                                pinnedStreamsIds = pinnedStreamsIds,
                                onClick = onPinStreamClick
                            )
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
                onClick = {}
            )
        }

        item {
            AdminSheetItem(
                text = stringResource(id = R.string.kaleyra_participants_panel_mute_for_you),
                painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_speaker_off),
                onClick = {}
            )
        }

        item {
            AdminSheetItem(
                text = stringResource(id = R.string.kaleyra_participants_panel_remove_from_call),
                painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_kick),
                color = KickParticipantColor,
                onClick = {}
            )
        }
    }
}

@Composable
private fun AdminSheetItem(
    text: String,
    painter: Painter,
    color: Color = Color.Unspecified,
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

@Composable
private fun DisableMicButton(streamId: String, audio: AudioUi?, onClick: (String, Boolean) -> Unit) {
    IconButton(
        onClick = { if (audio != null) onClick(streamId, !audio.isEnabled) else Unit }
    ) {
        Icon(
            painter = painterResource(id = if (audio?.isEnabled == true) R.drawable.ic_kaleyra_participant_panel_mic_on else R.drawable.ic_kaleyra_participant_panel_mic_off),
            contentDescription = stringResource(id = if (audio?.isEnabled == true) R.string.kaleyra_participants_panel_disable_microphone else R.string.kaleyra_participants_panel_enable_microphone)
        )
    }
}

@Composable
private fun MuteForYouButton(streamId: String, audio: AudioUi?, onClick: (String, Boolean) -> Unit) {
    IconButton(
        onClick = { if (audio != null) onClick(streamId, !audio.isMutedForYou) else Unit }
    ) {
        Icon(
            painter = painterResource(id = if (audio == null || audio.isMutedForYou) R.drawable.ic_kaleyra_participant_panel_speaker_off else R.drawable.ic_kaleyra_participant_panel_speaker_on),
            contentDescription = stringResource(id = if (audio == null || audio.isMutedForYou) R.string.kaleyra_participants_panel_unmute_for_you else R.string.kaleyra_participants_panel_mute_for_you)
        )
    }
}

@Composable
private fun ShowAdminModalSheetButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_more),
            contentDescription = stringResource(id = R.string.kaleyra_participants_panel_show_more_actions)
        )
    }
}

@Composable
private fun PinButton(
    streamId: String,
    pinnedStreamsIds: ImmutableList<String>,
    onClick: (String, Boolean) -> Unit
) {
    val isStreamPinned by remember(pinnedStreamsIds) {
        derivedStateOf { pinnedStreamsIds.value.contains(streamId) }
    }
    IconButton(
        onClick = { onClick(streamId, !isStreamPinned) }
    ) {
        Icon(
            painter = painterResource(id = if (isStreamPinned) R.drawable.ic_kaleyra_participant_panel_unpin else R.drawable.ic_kaleyra_participant_panel_pin),
            contentDescription = stringResource(id = if (isStreamPinned) R.string.kaleyra_participants_panel_unpin else R.string.kaleyra_participants_panel_pin)
        )
    }
}

@Composable
internal fun ParticipantItem(
    avatar: ImmutableUri?,
    title: String,
    subtitle: String,
    actions: @Composable RowScope.() -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            uri = avatar,
            contentDescription = stringResource(id = R.string.kaleyra_avatar),
            backgroundColor = MaterialTheme.colorScheme.primary,
            contentColor = contentColorFor(MaterialTheme.colorScheme.primary),
            size = 28.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = subtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
        }

        actions(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsTopAppBar(
    companyLogo: Logo,
    participantsCount: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    onCloseClick: () -> Unit
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            Avatar(
                uri = (if (isSystemInDarkTheme) companyLogo.dark else companyLogo.light)?.let {
                    ImmutableUri(it)
                },
                contentDescription = stringResource(id = R.string.kaleyra_company_logo),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = contentColorFor(MaterialTheme.colorScheme.primary),
                size = 24.dp,
                modifier = Modifier.padding(12.dp)
            )
        },
        title = {
            Text(
                pluralStringResource(id = R.plurals.kaleyra_participants_panel_participants, count = participantsCount, participantsCount),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        actions = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_close),
                    contentDescription = stringResource(id = R.string.kaleyra_participants_panel_close)
                )
            }
        }
    )
}

@Composable
internal fun StreamsLayoutSelector(onLayoutClick: (isGridLayout: Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.weight(1f),
            onClick = remember { { onLayoutClick(true) } }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_grid),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.kaleyra_participants_panel_grid), fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.width(14.dp))

        Button(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.weight(1f),
            onClick = remember { { onLayoutClick(false) } }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participant_panel_pin),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.kaleyra_participants_panel_pin), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantPanelPreview() {
    KaleyraM3Theme {
        ParticipantsPanel(
            companyLogo = Logo(),
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
            onLayoutClick = {},
            onDisableMicClick = {_,_ ->},
            onKickParticipantClick = {},
            onPinStreamClick = {_,_ ->},
            onMuteStreamClick = {_,_ ->},
            onCloseClick = {}
        )
    }
}