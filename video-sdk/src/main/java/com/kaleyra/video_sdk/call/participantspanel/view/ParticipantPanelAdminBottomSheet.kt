@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.call.participantspanel.view

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.screenshare.view.ScreenShareItem
import com.kaleyra.video_sdk.call.screenshare.view.clickLabelFor
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ModifierExtensions.horizontalCutoutPadding
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUi: StreamUi) =
    ImmutableList(
        listOf(
            BottomSheetItemUi(
                text = stringResource(id = if (streamUi.pinned) R.string.kaleyra_call_action_unpin_description else R.string.kaleyra_call_action_pin_description),
                iconPainter = painterResource(id = if (streamUi.pinned) R.drawable.ic_kaleyra_unpin else R.drawable.ic_kaleyra_pin)
            ),
            BottomSheetItemUi(
                text = stringResource(id = if (streamUi.audio?.isEnabledForYou == true) R.string.kaleyra_call_action_mic_mute_for_you else R.string.kaleyra_call_action_mic_unmute_for_you),
                iconPainter = painterResource(id = if (streamUi.audio?.isEnabledForYou == true) R.drawable.ic_kaleyra_muted else R.drawable.ic_kaleyra_loud_speaker)
            ),
            BottomSheetItemUi(
                text = stringResource(id = R.string.kaleyra_call_action_remove_participant_from_call),
                iconPainter = painterResource(id = R.drawable.kaleyra_z_remove_from_call),
                tint = MaterialTheme.colorScheme.error
            ),
        )
    )

@Composable
internal fun ParticipantPanelAdminBottomSheet(
    skipPartiallyExpanded: Boolean = false,
    edgeToEdgeEnabled: Boolean = false,
    isSystemInDarkTheme: Boolean,
    streamUi: StreamUi,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar_bold,
    @DrawableRes avatarError: Int = R.drawable.ic_kaleyra_avatar_bold,
    avatarSize: Dp = 56.dp,

    ) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded)

    ModalBottomSheet(
        modifier = Modifier
            .padding(WindowInsets.displayCutout.asPaddingValues()),
        containerColor = if (isSystemInDarkTheme) Color(0xFF242424) else Color.White,
        onDismissRequest = { },
        sheetState = modalBottomSheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        windowInsets = if (edgeToEdgeEnabled) WindowInsets(0) else BottomSheetDefaults.windowInsets,
    ) {
        ParticipantPanelAdminContent(
            isSystemInDarkTheme = isSystemInDarkTheme,
            streamUi = streamUi,
            items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUi),
            avatarError = avatarError,
            avatarPlaceholder = avatarPlaceholder,
            avatarSize = avatarSize,
            onMuteForYouClicked = { a, b -> },
            onPinClicked = { a, b -> },
            onRemoveFromCallClicked = {},
        )
    }
}

@Composable
internal fun ParticipantPanelAdminContent(
    isSystemInDarkTheme: Boolean,
    streamUi: StreamUi,
    items: ImmutableList<BottomSheetItemUi>,
    @DrawableRes avatarPlaceholder: Int = R.drawable.ic_kaleyra_avatar_bold,
    @DrawableRes avatarError: Int = R.drawable.ic_kaleyra_avatar_bold,
    avatarSize: Dp = 56.dp,
    onPinClicked: (StreamUi, Boolean) -> Unit,
    onMuteForYouClicked: (StreamUi, Boolean) -> Unit,
    onRemoveFromCallClicked: () -> Unit,
) {
    val tintColor = if (isSystemInDarkTheme) Color(0xFF242424) else Color.White
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .background(color = tintColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalCutoutPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                uri = streamUi.avatar,
                contentDescription = stringResource(id = R.string.kaleyra_avatar),
                placeholder = avatarPlaceholder,
                error = avatarError,
                contentColor = LocalContentColor.current,
                backgroundColor = Color.LightGray,
                size = avatarSize
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    modifier = Modifier,
                    lineHeight = 22.sp,
                    text = streamUi.username,
                    color = if (isSystemInDarkTheme) Color.White else Color.Black)
            }
        }
        Spacer(modifier = Modifier.size(16.dp))

        LazyColumn {
            items(items = items.value, key = { it.hashCode() }) {
                BottomSheetRowItem(
                    bottomSheetItemUi = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    onClick = {
                        when (items.value.indexOf(it)) {
                            0 -> onPinClicked(streamUi, !streamUi.pinned)
                            1 -> onMuteForYouClicked(streamUi, !streamUi.audio?.isEnabledForYou!!)
                            2 -> onRemoveFromCallClicked()
                        }
                    }
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantPanelAdminBottomSheetPreview() = KaleyraTheme {
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val skipPartiallyExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { openBottomSheet = !openBottomSheet }) {
            Text(text = "Show ParticipantPanelAdminBottomSheet")
        }
    }
    ParticipantPanelAdminContent(
        isSystemInDarkTheme = isSystemInDarkTheme(),
        streamUi = streamUiMock,
        items = ParticipantPanelAdminBottomSheetItemsForStreamUi(streamUiMock),
        onPinClicked = { a, b -> },
        onMuteForYouClicked = { a, b -> },
        onRemoveFromCallClicked = {},
    )

    if (openBottomSheet) {
        ParticipantPanelAdminBottomSheet(
            isSystemInDarkTheme = isSystemInDarkTheme(),
            streamUi = streamUiMock,
            skipPartiallyExpanded = skipPartiallyExpanded
        )
    }
}
