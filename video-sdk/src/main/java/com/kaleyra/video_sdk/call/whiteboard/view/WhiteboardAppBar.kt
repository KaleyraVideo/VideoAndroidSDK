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

package com.kaleyra.video_sdk.call.whiteboard.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.appbar.view.ComponentAppBar
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WhiteboardAppBar(
    isFileSharingSupported: Boolean,
    onBackPressed: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false
) {
    ComponentAppBar(
        modifier = modifier,
        onBackPressed = onBackPressed,
        isLargeScreen = isLargeScreen,
        title = stringResource(id = R.string.kaleyra_whiteboard),
        actions = {
            if (isFileSharingSupported) {
                androidx.compose.material3.IconButton(
                    modifier = Modifier.padding(4.dp),
                    onClick = onUploadClick,
                    ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_add),
                        contentDescription = stringResource(id = R.string.kaleyra_upload_file))
                }
            } else {
                Spacer(Modifier.width(56.dp))
            }
        },
    )
}

@DayModePreview
@NightModePreview
@Composable
internal fun WhiteboardAppBarTest() {
    KaleyraTheme {
        WhiteboardAppBar(
            isFileSharingSupported = true,
            onBackPressed = {},
            onUploadClick = {}
        )
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun WhiteboardAppBarLargeScreenTest() {
    KaleyraTheme {
        WhiteboardAppBar(
            isFileSharingSupported = true,
            onBackPressed = {},
            onUploadClick = {},
            isLargeScreen = true
        )
    }
}
