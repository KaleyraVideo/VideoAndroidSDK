package com.kaleyra.video_sdk.call.participantspanel

import android.content.res.Configuration
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
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
            },
            content = { contentPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(contentPadding),
                    contentPadding = PaddingValues(start = 38.dp, end = 38.dp, bottom = 38.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Change Layout",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    item {
                        LayoutSelector(onLayoutClick)
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Users in call",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(items = streams.value, key = { it.id }) { stream ->
                        ParticipantItem(
                            avatar = stream.avatar,
                            title = stream.username + if (stream.mine) " (You)" else "",
                            subtitle = remember(stream, adminsStreamsIds) {
                                when {
                                    stream.video?.isScreenShare == true -> "Screenshare"
                                    adminsStreamsIds.value.contains(stream.id) -> "Admin"
                                    else -> "Participant"
                                }
                            }
                        ) {
                            if (amIAdmin || stream.mine) {
                                IconButton(
                                    onClick = remember(stream.audio) { { if (stream.audio != null) onDisableMicClick(stream.id, !stream.audio.isEnabled) else Unit } }
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (stream.audio?.isEnabled == true) R.drawable.ic_kaleyra_mic_on_new else R.drawable.ic_kaleyra_mic_off_new),
                                        contentDescription = if (stream.audio?.isEnabled == true) "disable participant microphone" else "participant microphone already disabled"
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = remember(stream.audio) {
                                        {
                                            if (stream.audio != null) onMuteStreamClick(stream.id, !stream.audio.isMuted) else Unit
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (stream.audio == null || stream.audio.isMuted) R.drawable.ic_kaleyra_speaker_off_new else R.drawable.ic_kaleyra_speaker_on_new),
                                        contentDescription = if (stream.audio == null || stream.audio.isMuted) "unmute participant for you" else "mute participant for you"
                                    )
                                }
                            }

                            if (amIAdmin) {
                                IconButton(onClick = { /*TODO show the bottom sheet*/ }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_kaleyra_more_new),
                                        contentDescription = "show admin actions"
                                    )
                                }
                            } else {
                                val isStreamPinned by remember(pinnedStreamsIds) {
                                    derivedStateOf { pinnedStreamsIds.value.contains(stream.id) }
                                }
                                IconButton(
                                    onClick = remember(isStreamPinned) { { onPinStreamClick(stream.id, !isStreamPinned) } }
                                ) {
                                    Icon(
                                        painter = painterResource(id = if (isStreamPinned) R.drawable.ic_kaleyra_unpin_new else R.drawable.ic_kaleyra_pin_new),
                                        contentDescription = if (isStreamPinned) "unpin stream" else "pin stream"
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Invited",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    items(items = invited.value) { username ->
                        Text(username, Modifier.fillMaxWidth())
                    }
                }
            }
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
            contentDescription = "user avatar",
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
                contentDescription = "company logo",
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = contentColorFor(MaterialTheme.colorScheme.primary),
                size = 24.dp,
                modifier = Modifier.padding(12.dp)
            )
        },
        title = {
            Text(
                "$participantsCount Participants",
                style = MaterialTheme.typography.titleMedium
            )
        },
        actions = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaleyra_close_new),
                    contentDescription = "close panel"
                )
            }
        }
    )
}

@Composable
internal fun LayoutSelector(onLayoutClick: (isGridLayout: Boolean) -> Unit) {
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
                painter = painterResource(id = R.drawable.ic_kaleyra_grid_new),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("Grid")
        }

        Spacer(Modifier.width(14.dp))

        Button(
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.weight(1f),
            onClick = remember { { onLayoutClick(false) } }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_pin_new),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text("Pin")
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
            amIAdmin = false,
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