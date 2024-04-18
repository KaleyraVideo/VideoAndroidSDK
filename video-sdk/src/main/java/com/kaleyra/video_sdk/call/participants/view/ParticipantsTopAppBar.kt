package com.kaleyra.video_sdk.call.participants.view

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsTopAppBar(
    companyLogo: Logo,
    participantsCount: Int,
    scrollBehavior: TopAppBarScrollBehavior? = null,
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
                modifier = Modifier.padding(12.dp)
            )
        },
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
@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ParticipantsTopAppBarPreview() {
    KaleyraM3Theme {
        ParticipantsTopAppBar(companyLogo = Logo(), participantsCount = 3, onCloseClick = {})
    }
}