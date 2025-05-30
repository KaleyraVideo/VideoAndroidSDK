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

package com.kaleyra.video_sdk.call.fileshare.view

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.fileshare.model.mockUploadSharedFile
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme
import java.util.UUID

/**
 * File Share Item Tag
 */
const val FileShareItemTag = "FileShareItemTag"

/**
 * File Share Item Divider Tag
 */
const val FileShareItemDividerTag = "FileShareItemDividerTag"
private val ContentBottomPadding = 72.dp

@Composable
internal fun FileShareContent(
    modifier: Modifier = Modifier,
    items: ImmutableList<SharedFileUi>,
    onItemClick: (SharedFileUi) -> Unit,
    onItemActionClick: (SharedFileUi) -> Unit,
    lazyGridState: LazyGridState
) {

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minSize = 600.dp),
        contentPadding = PaddingValues(bottom = ContentBottomPadding),
        state = lazyGridState
    ) {
        itemsIndexed(items = items.value, key = { _, item -> item.id }) { index, item ->
            FileShareItem(
                sharedFile = item,
                modifier = Modifier
                    .clickable(
                        enabled = item.state is SharedFileUi.State.Success,
                        onClickLabel = stringResource(R.string.kaleyra_fileshare_open_file),
                        role = Role.Button,
                        onClick = { onItemClick(item) }
                    )
                    .testTag(FileShareItemTag),
                onActionClick = { onItemActionClick(item) }
            )
            if (index != 0) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.testTag(FileShareItemDividerTag)
                )
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareContentPreview() {
    KaleyraTheme {
        Surface {
            FileShareContent(
                items = ImmutableList((0..200).map { mockUploadSharedFile.copy(id = UUID.randomUUID().toString()) }),
                onItemClick = {},
                onItemActionClick = {},
                lazyGridState = rememberLazyGridState()
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun FileShareNoItemsContentPreview() {
    KaleyraTheme {
        Surface {
            FileShareContent(
                items = ImmutableList(listOf()),
                onItemClick = {},
                onItemActionClick = {},
                lazyGridState = rememberLazyGridState()
            )
        }
    }
}