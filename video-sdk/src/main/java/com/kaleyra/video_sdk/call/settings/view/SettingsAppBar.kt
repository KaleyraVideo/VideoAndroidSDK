package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.call.signature.view.SignDocumentsAppBar
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsAppBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    lazyGridState: LazyGridState,
    isLargeScreen: Boolean = false,
) {
    ComponentAppBar(
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_strings_action_settings),
        scrollableState = lazyGridState,
        isLargeScreen = isLargeScreen,
        modifier = modifier,
        enableSearch = false,
    )
}

@DayModePreview
@NightModePreview
@Composable
internal fun SettingsAppBarTest() {
    KaleyraTheme {
        SettingsAppBar(
            onBackPressed = { },
            lazyGridState = rememberLazyGridState(),
        )
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun SetingsLargeScreenTest() {
    KaleyraTheme {
        SettingsAppBar(
            onBackPressed = { },
            lazyGridState = rememberLazyGridState(),
            isLargeScreen = true,
        )
    }
}