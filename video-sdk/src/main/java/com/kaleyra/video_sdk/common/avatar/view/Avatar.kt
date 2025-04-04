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

package com.kaleyra.video_sdk.common.avatar.view

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.extensions.ModifierExtensions.drawCircleBorder
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal object AvatarDefaults {

    val defaultSize = 24.dp
}

@Composable
internal fun Avatar(
    uri: ImmutableUri?,
    username: String,
    modifier: Modifier = Modifier,
    size: Dp = AvatarDefaults.defaultSize,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = contentColorFor(backgroundColor),
    borderColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    borderWidth: Dp = 0.dp,
    @DrawableRes placeholder: Int = R.drawable.ic_kaleyra_avatar_bold,
    onSuccess: (() -> Unit)? = null
) {
    var isImageLoaded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        AsyncImage(
            model = uri?.value,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onSuccess = {
                onSuccess?.invoke()
                isImageLoaded = true
            },
            modifier = Modifier
                .drawCircleBorder(borderColor, borderWidth)
                .clip(CircleShape)
                .size(size)
                .background(backgroundColor)
        )
        if (!isImageLoaded) {
            when {
                username.isBlank() -> {
                    val iconSize = remember(size) { size / 1.75f }
                    Icon(
                        painter = painterResource(placeholder),
                        tint = contentColor,
                        contentDescription = stringResource(R.string.kaleyra_avatar),
                        modifier = Modifier.size(iconSize)
                    )
                }
                else -> {
                    val fontSize = with(LocalDensity.current) { size.toSp() / 2.5 }
                    Text(
                        text = username.firstOrNull()?.uppercase() ?: "",
                        color = contentColor,
                        fontSize = fontSize
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun AvatarPreview() {
    KaleyraTheme {
        Avatar(
            username = "J",
            uri = null
        )
    }
}

@Preview
@Composable
internal fun AvatarPlaceholderPreview() {
    KaleyraTheme {
        Avatar(
            username = "",
            uri = null
        )
    }
}


