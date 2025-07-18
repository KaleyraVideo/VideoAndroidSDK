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

package com.kaleyra.video_sdk.call.virtualbackground.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun VirtualBackgroundItem(
    background: VirtualBackgroundUi,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = when (background) {
                    VirtualBackgroundUi.None -> R.drawable.ic_kaleyra_virtual_background_none
                    is VirtualBackgroundUi.Blur -> R.drawable.ic_kaleyra_virtual_background_blur
                    is VirtualBackgroundUi.Image -> R.drawable.ic_kaleyra_virtual_background_image
                }
            ),
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
        Spacer(modifier = Modifier.width(16.dp))
        val bodyLargeStyle = MaterialTheme.typography.bodyLarge
        Text(
            text = textFor(background),
            maxLines = 1,
            color = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            style = if (selected) MaterialTheme.typography.titleMedium.copy(letterSpacing = bodyLargeStyle.letterSpacing) else bodyLargeStyle,
        )
    }
}

@Composable
internal fun clickLabelFor(background: VirtualBackgroundUi) = textFor(background = background)

@Composable
private fun textFor(background: VirtualBackgroundUi) =
    stringResource(
        id = when (background) {
            VirtualBackgroundUi.None -> R.string.kaleyra_virtual_background_none
            is VirtualBackgroundUi.Blur -> R.string.kaleyra_virtual_background_blur
            is VirtualBackgroundUi.Image -> R.string.kaleyra_virtual_background_image
        }
    )

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun NoneBackgroundItemPreview() {
    BackgroundItemPreview(background = VirtualBackgroundUi.None)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun BlurBackgroundItemPreview() {
    BackgroundItemPreview(background = VirtualBackgroundUi.Blur("id"))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ImageBackgroundItemPreview() {
    BackgroundItemPreview(background = VirtualBackgroundUi.Image("id"))
}

@Composable
private fun BackgroundItemPreview(background: VirtualBackgroundUi) {
    KaleyraTheme {
        Surface {
            VirtualBackgroundItem(background = background, false)
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SelectedBackgroundItemPreview() {
    KaleyraTheme {
        Surface {
            VirtualBackgroundItem(background = VirtualBackgroundUi.Blur("id"), true)
        }
    }
}
