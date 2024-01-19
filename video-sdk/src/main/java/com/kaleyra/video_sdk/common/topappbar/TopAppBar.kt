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

@file:OptIn(ExperimentalFoundationApi::class)

package com.kaleyra.video_sdk.common.topappbar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal const val ActionsTag = "ActionsTag"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopAppBar(
    navigationIcon: @Composable RowScope.() -> Unit,
    content: @Composable (RowScope.() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.pinnedScrollBehavior(),
    scrollState: LazyListState,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.TopAppBar(
        modifier = modifier
            .focusGroup(),
        title = { },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (scrollState.canScrollForward) androidx.compose.material3.MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) else androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        windowInsets = windowInsets,
        scrollBehavior = scrollBehavior,
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, content = navigationIcon)

            if (content != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }

            if (actions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag(ActionsTag),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        },
    )
}

@Composable
internal fun TopAppBar(
    navigationIcon: @Composable RowScope.() -> Unit,
    content: @Composable (RowScope.() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    contentPadding: PaddingValues = AppBarDefaults.ContentPadding,
    modifier: Modifier = Modifier
) {
    androidx.compose.material.TopAppBar(
        modifier = modifier.focusGroup(),
        elevation = elevation,
        contentPadding = contentPadding,
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Row(verticalAlignment = Alignment.CenterVertically, content = navigationIcon)

            if (content != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }

            if (actions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .testTag(ActionsTag),
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}
