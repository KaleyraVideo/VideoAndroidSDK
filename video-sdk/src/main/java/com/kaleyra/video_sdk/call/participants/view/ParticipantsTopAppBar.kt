package com.kaleyra.video_sdk.call.participants.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsTopAppBar(
    participantsCount: Int,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    ComponentAppBar(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = pluralStringResource(id = R.plurals.kaleyra_participants_component_participants, count = participantsCount, participantsCount),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@DayModePreview
@NightModePreview
@Composable
internal fun ParticipantsTopAppBarPreview() {
    KaleyraTheme {
        ParticipantsTopAppBar(participantsCount = 3, onBackPressed = {})
    }
}