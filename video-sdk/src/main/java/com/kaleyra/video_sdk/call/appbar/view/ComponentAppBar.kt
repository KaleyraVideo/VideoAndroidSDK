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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.topappbar.TopAppBar
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

const val SearchInputTag = "SearchInputTag"

@Composable
internal fun ComponentAppBar(
    modifier: Modifier = Modifier,
    title: String,
    onBackPressed: () -> Unit,
    enableSearch: Boolean = false,
    actions: @Composable (RowScope.() -> Unit) = { if (!enableSearch) Spacer(Modifier.width(56.dp)) },
    scrollBehavior: TopAppBarScrollBehavior? = TopAppBarDefaults.pinnedScrollBehavior(),
    scrollableState: ScrollableState? = null,
    isLargeScreen: Boolean = false,
    onSearch: (String) -> Unit = {}
) {
    var displaySearchBar by remember { mutableStateOf(false) }
    var queryText by remember { mutableStateOf("") }
    val searchFocusRequester = remember { FocusRequester() }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        windowInsets = if (isLargeScreen) WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) else TopAppBarDefaults.windowInsets,
        navigationIcon = {
            val interactionSource = remember { MutableInteractionSource() }
            IconButton(
                modifier = Modifier
                    .padding(4.dp)
                    .highlightOnFocus(interactionSource),
                onClick = {
                    if (displaySearchBar) {
                        queryText = ""
                        onSearch("")
                        displaySearchBar = false
                    } else onBackPressed()
                },
                interactionSource = interactionSource,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                if (!displaySearchBar) {
                    Icon(
                        painter = if (isLargeScreen) painterResource(id = R.drawable.ic_kaleyra_back_right) else painterResource(id = R.drawable.ic_kaleyra_back_down),
                        contentDescription = stringResource(id = R.string.kaleyra_close)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_back),
                        contentDescription = stringResource(id = R.string.kaleyra_close)
                    )
                }

            }
        },
        content = {
                if (!displaySearchBar) {
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {

                    SearchInput(
                        modifier = Modifier
                            .padding(end = 24.dp)
                            .focusRequester(searchFocusRequester),
                        query = queryText,
                        onQueryChange = {
                            queryText = it
                            onSearch(it)
                        }
                    )

                    LaunchedEffect(Unit) {
                        searchFocusRequester.requestFocus()
                    }
                }
        },
        actions = {
            if (!displaySearchBar) actions()
            if (enableSearch && !displaySearchBar) {
                val interactionSource = remember { MutableInteractionSource() }
                IconButton(
                    modifier = Modifier.highlightOnFocus(interactionSource),
                    interactionSource = interactionSource,
                    onClick = { displaySearchBar = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(R.string.kaleyra_strings_action_search)
                    )
                }
            }
        },
        containerColor = if (scrollableState?.canScrollBackward == true) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = modifier
    )
}

@Composable
fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = stringResource(R.string.kaleyra_strings_action_search),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(text = hint) },
        singleLine = true,
        interactionSource = interactionSource,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = hint
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                val trailingButtonInteractionSource = remember { MutableInteractionSource() }
                IconButton(
                    onClick = { onQueryChange("") },
                    interactionSource = trailingButtonInteractionSource,
                    modifier = Modifier.highlightOnFocus(trailingButtonInteractionSource)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.kaleyra_strings_action_clear)
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        modifier = modifier
            .fillMaxWidth()
            .highlightOnFocus(interactionSource)
            .testTag(SearchInputTag),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

@MultiConfigPreview
@Composable
fun SearchInputPreview() {
    var searchQuery by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.padding(16.dp)
    ) {
        SearchInput(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
        )
    }
}

@MultiConfigPreview
@Composable
fun ComponentAppBarPreview() {
    KaleyraTheme {
        ComponentAppBar(
            title = "testing",
            onBackPressed = {},
            enableSearch = true
        )
    }
}
