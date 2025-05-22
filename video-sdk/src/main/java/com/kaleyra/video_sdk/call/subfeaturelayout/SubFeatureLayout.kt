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

package com.kaleyra.video_sdk.call.subfeaturelayout

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SubFeatureLayout(
    title: String,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(start = 24.dp, end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            val buttonInteractionSource = remember { MutableInteractionSource() }
            IconButton(
                onClick = onCloseClick,
                interactionSource = buttonInteractionSource,
                modifier = Modifier.highlightOnFocus(buttonInteractionSource)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaleyra_close),
                    contentDescription = stringResource(id = R.string.kaleyra_close))
            }
        }
        content()
        NavigationBarsSpacer()
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SubFeatureLayoutPreview() {
    KaleyraTheme {
        Surface {
            SubFeatureLayout(title = "Select an audio output", onCloseClick = {}) {

            }
        }
    }
}
