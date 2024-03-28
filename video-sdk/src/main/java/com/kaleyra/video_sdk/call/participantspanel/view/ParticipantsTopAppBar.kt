package com.kaleyra.video_sdk.call.participantspanel.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.contentColorFor
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar

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