/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.signature.view

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SignDocumentsAppBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    lazyGridState: LazyGridState,
    isLargeScreen: Boolean = false,
    enableSearch: Boolean = true,
    onSearch: (String) -> Unit
) {
    ComponentAppBar(
        onBackPressed = onBackPressed,
        title = stringResource(id = R.string.kaleyra_sign_documents),
        scrollableState = lazyGridState,
        isLargeScreen = isLargeScreen,
        modifier = modifier,
        enableSearch = enableSearch,
        onSearch = onSearch
    )
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareAppBarTest() {
    KaleyraTheme {
        SignDocumentsAppBar(
            onBackPressed = { },
            lazyGridState = rememberLazyGridState(),
            onSearch = {}
        )
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareAppBarLargeScreenTest() {
    KaleyraTheme {
        SignDocumentsAppBar(
            onBackPressed = { },
            lazyGridState = rememberLazyGridState(),
            isLargeScreen = true,
            onSearch = {}
        )
    }
}