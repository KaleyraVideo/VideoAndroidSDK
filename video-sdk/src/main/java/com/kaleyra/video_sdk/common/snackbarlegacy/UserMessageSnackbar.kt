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

package com.kaleyra.video_sdk.common.snackbarlegacy

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserMessageSnackbar(
    iconPainter: Painter,
    title: String,
    subtitle: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = .8f)
        .compositeOver(MaterialTheme.colorScheme.surface)
) {
    SwipeToDismissBox(state = rememberSwipeToDismissBoxState(), backgroundContent = {}) {
        Snackbar(
            containerColor = backgroundColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Row {
                Image(
                    painter = iconPainter,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Medium)
                    if (subtitle != null) Text(text = subtitle)
                }
            }
        }
    }
}

@Composable
internal fun UserMessageInfoSnackbar(title: String, subtitle: String? = null) {
    UserMessageSnackbar(
        iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_info),
        title = title,
        subtitle = subtitle
    )
}

@Composable
internal fun UserMessageErrorSnackbar(title: String, subtitle: String? = null) {
    UserMessageSnackbar(
        iconPainter = painterResource(id = R.drawable.ic_kaleyra_snackbar_error),
        title = title,
        subtitle = subtitle,
        backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = .8f)
            .compositeOver(MaterialTheme.colorScheme.onError)
    )
}
