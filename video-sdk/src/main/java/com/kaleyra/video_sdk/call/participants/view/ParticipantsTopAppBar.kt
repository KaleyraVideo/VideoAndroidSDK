package com.kaleyra.video_sdk.call.participants.view

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsTopAppBar(
    participantsCount: Int,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onCloseClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                pluralStringResource(id = R.plurals.kaleyra_participants_component_participants, count = participantsCount, participantsCount),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        actions = {
            val interactionSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = onCloseClick,
                interactionSource = interactionSource,
                modifier = Modifier.highlightOnFocus(interactionSource)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_close),
                    contentDescription = stringResource(id = R.string.kaleyra_participants_component_close)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@DayModePreview
@NightModePreview
@Composable
internal fun ParticipantsTopAppBarPreview() {
    KaleyraM3Theme {
        ParticipantsTopAppBar(participantsCount = 3, onCloseClick = {})
    }
}