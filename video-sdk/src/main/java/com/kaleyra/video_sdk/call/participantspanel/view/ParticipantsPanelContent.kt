@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.video_sdk.call.participantspanel.view

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kaleyra.video.conversation.internal.chat_client.l
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.call.participantspanel.model.StreamArrangement
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.call.utils.custommodifiers.fadingEdges
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

private val MaxWatermarkHeight = 48.dp
private val MaxWatermarkWidth = 48.dp

@ExperimentalCoroutinesApi
@Composable
internal fun ParticipantsPanelContent(
    watermarkInfo: WatermarkInfo? = null,
    isLoggedUserAdmin: Boolean,
    adminUserId: String,
    inCallStreamUi: ImmutableList<StreamUi>,
    invitedParticipants: ImmutableList<String>,
    streamArrangement: StreamArrangement,
    onClose: () -> Unit,
    onGridClicked: () -> Unit,
    onPinClicked: () -> Unit,
    onPin: (StreamUi, Boolean) -> Unit,
    onMute: (StreamUi, Boolean) -> Unit,
) {
    val isDarkTheme = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = if (isDarkTheme) Color(0xFF242424) else Color.White),
    ) {
        Column {
            ParticipantPanelTopBarComponent(watermarkInfo, inCallStreamUi, onClose, isDarkTheme)
            if (inCallStreamUi.value.isNotEmpty()) ParticipantsPanelChangeLayoutComponent(streamArrangement, onGridClicked, onPinClicked)
            ParticipantsPanelCallParticipantsListComponent(inCallStreamUi, invitedParticipants, isLoggedUserAdmin, adminUserId, isDarkTheme, onPin, onMute)
        }
    }
}

@Composable
internal fun ParticipantPanelTopBarComponent(
    watermarkInfo: WatermarkInfo?,
    inCallStreamUi: ImmutableList<StreamUi>,
    onClose: () -> Unit,
    isDarkTheme: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (watermarkInfo?.logo != null) AsyncImage(
            model = watermarkInfo.logo.let { if (!isDarkTheme) it.light else it.dark },
            contentDescription = stringResource(id = R.string.kaleyra_company_logo),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .heightIn(max = MaxWatermarkHeight)
                .widthIn(max = MaxWatermarkWidth),
        )
        val participantsCount = inCallStreamUi.count()
        if (participantsCount > 0) {
            val headerText = "$participantsCount ${pluralStringResource(id = R.plurals.kaleyra_participants, count = participantsCount)}"
            Text(modifier = Modifier.padding(16.dp), text = headerText, style = TextStyle(
                fontSize = 14.55.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(500),
                color = if (isDarkTheme) Color(0xFFFFFFFF) else Color.Black,
                textAlign = TextAlign.Center,
            ))
        }
        IconButton(
            onClick = onClose,
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_kaleyra_close
                ),
                contentDescription = stringResource(
                    id = R.string.kaleyra_close
                ),
                tint = if (isDarkTheme) Color.White else Color.Black,
                modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
internal fun ParticipantsPanelChangeLayoutComponent(streamArrangement: StreamArrangement, onGridClicked: () -> Unit, onPinClicked: () -> Unit) {
    Text(modifier = Modifier.padding(16.dp), text = stringResource(id = R.string.kaleyra_change_layout), style = TextStyle(
        fontSize = 13.22.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight(500),
        color = Color(0xFF92979D),
    ))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        ExtendedFloatingActionButton(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.5f)
                .padding(end = 8.dp)
                .height(38.dp),
            contentColor = Color.White,
            containerColor = if (streamArrangement is StreamArrangement.Grid) Color(0xFF1589C9) else Color(0xFF1B1B1B),
            shape = RoundedCornerShape(4.dp),
            onClick = { if (streamArrangement != StreamArrangement.Grid) onGridClicked() },
            content = {
                Icon(painter = painterResource(id = R.drawable.ic_kaleyra_grid_arrangement), contentDescription = stringResource(id = R.string.kaleyra_grid_arrangement))
                Text(modifier = Modifier.padding(start = 12.dp), text = stringResource(id = R.string.kaleyra_grid_arrangement), color = Color.White)
            }
        )
        ExtendedFloatingActionButton(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.5f)
                .padding(start = 8.dp)
                .height(38.dp)
                .weight(1f, true),
            contentColor = Color.White,
            containerColor = if (streamArrangement is StreamArrangement.Pin) Color(0xFF1589C9) else Color(0xFF1B1B1B),
            shape = RoundedCornerShape(4.dp),
            onClick = { if (streamArrangement != StreamArrangement.Pin) onPinClicked() },
            content = {
                Icon(painter = painterResource(id = R.drawable.ic_kaleyra_pin), contentDescription = stringResource(id = R.string.kaleyra_pin_arrangement))
                Text(modifier = Modifier.padding(start = 12.dp), text = stringResource(id = R.string.kaleyra_pin_arrangement), color = Color.White)
            }
        )
    }
}

@Composable
internal fun ParticipantsPanelCallParticipantsListComponent(
    inCallStreamUi: ImmutableList<StreamUi>,
    invitedParticipants: ImmutableList<String>,
    isLoggedUserAdmin: Boolean,
    adminUserId: String,
    isDarkTheme: Boolean,
    onPin: (StreamUi, Boolean) -> Unit,
    onMute: (StreamUi, Boolean) -> Unit,
) {
    LazyColumn {
        item(key = R.string.kaleyra_users_in_call) {
            if (inCallStreamUi.value.isNotEmpty())
                Text(modifier = Modifier.padding(16.dp), text = stringResource(id = R.string.kaleyra_users_in_call), style = TextStyle(
                    fontSize = 13.22.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF92979D),
                ))
        }

        items(items = inCallStreamUi.value, key = { it.hashCode() }) {
            ParticipantsPanelDetailComponent(
                streamUi = it,
                isLoggedUserAdmin = isLoggedUserAdmin,
                adminUserId = adminUserId,
                onMuteClicked = { muted ->
                    onMute(it, muted)
                },
                onPinClicked = { pinned ->
                    onPin(it, pinned)
                },
                onMoreClicked = {},
            )
        }

        if (invitedParticipants.value.isEmpty()) return@LazyColumn

        item(key = R.string.kaleyra_invited_users) {
            Text(modifier = Modifier.padding(16.dp), text = stringResource(id = R.string.kaleyra_invited_users),
                style = TextStyle(
                    fontSize = 13.22.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF92979D),
                )
            )
        }

        items(items = invitedParticipants.value, key = { it.hashCode() }) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = it,
                style = TextStyle(
                    fontSize = 14.55.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(400),
                    color = if (isDarkTheme) Color(0xFFECEDEE) else Color.Black,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantsPanelContentPreview() = KaleyraTheme {
    ParticipantsPanelContent(
        watermarkInfo = WatermarkInfo("brand", Logo(light = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"), dark = Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video.png"))),
        inCallStreamUi = ImmutableList((0..5).map {
            streamUiMock.copy(username = "${streamUiMock.username} $it")
        }),
        invitedParticipants = ImmutableList(listOf("John Parse")),
        streamArrangement = StreamArrangement.Pin,
        adminUserId = "Hugo",
        isLoggedUserAdmin = false,
        onClose = {},
        onGridClicked = {},
        onPinClicked = {},
        onPin = { streamUi, pinned -> },
        onMute = { streamUi, muted -> }
    )
}