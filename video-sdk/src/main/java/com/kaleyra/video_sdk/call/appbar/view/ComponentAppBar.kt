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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.kaleyra.video_sdk.call.appbar.view

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.topappbar.TopAppBar

@Composable
internal fun ComponentAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onBackPressed: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = { Spacer(Modifier.width(56.dp)) },
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.pinnedScrollBehavior(),
    scrollableState: ScrollableState? = null,
    isLargeScreen: Boolean = false,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        windowInsets = if (isLargeScreen) WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) else TopAppBarDefaults.windowInsets,
        navigationIcon = {
            androidx.compose.material3.IconButton(
                modifier = Modifier.padding(4.dp),
                onClick = onBackPressed,
            ) {
                Icon(
                    painter = if (isLargeScreen) painterResource(id = R.drawable.ic_kaleyra_back_right) else painterResource(id = R.drawable.ic_kaleyra_back_down),
                    contentDescription = stringResource(id = R.string.kaleyra_close)
                )
            }
        },
        content = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        },
        actions = actions,
        containerColor = if (scrollableState?.canScrollBackward == true) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier
    )
}
