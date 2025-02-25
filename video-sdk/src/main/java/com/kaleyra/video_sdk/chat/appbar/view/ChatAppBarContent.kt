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

package com.kaleyra.video_sdk.chat.appbar.view

import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.avatar.view.Avatar
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal val TypingDotsPadding = 4.dp

@Composable
internal fun ChatAppBarContent(
    image: ImmutableUri,
    title: String,
    subtitle: String,
    typingDots: Boolean,
    isGroup: Boolean = false,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(
            username = if (!isGroup) title else "",
            uri = image,
            placeholder = if (isGroup) R.drawable.ic_kaleyra_avatars_bold else R.drawable.ic_kaleyra_avatar,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            backgroundColor = MaterialTheme.colorScheme.secondary,
            size = 40.dp
        )
        Column(Modifier.padding(start = 12.dp)) {
            Text(
                modifier = Modifier.basicMarquee(),
                text = title,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight!!,
            )
            Row {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = MaterialTheme.typography.bodySmall.fontWeight!!,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(SubtitleTag).basicMarquee(),
                )
                if (typingDots) {
                    TypingDots(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(start = TypingDotsPadding, bottom = TypingDotsPadding)
                            .testTag(BouncingDotsTag)
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ChatScreenPreview() = KaleyraTheme {
    Column(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
        ChatAppBarContent(
            image = ImmutableUri(Uri.parse("https://www.kaleyra.com/wp-content/uploads/Video-Call.png")),
            title = "John Smith",
            subtitle = "typing",
            typingDots = true,
            isGroup = true,
        )
    }
}
