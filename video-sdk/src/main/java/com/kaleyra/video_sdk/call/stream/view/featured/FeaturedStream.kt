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

package com.kaleyra.video_sdk.call.stream.view.featured

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video.conference.StreamView
import com.kaleyra.video_sdk.common.button.IconButton
import com.kaleyra.video_sdk.call.stream.model.ImmutableView
import com.kaleyra.video_sdk.call.stream.model.StreamUi
import com.kaleyra.video_sdk.call.utils.StreamViewSettings.featuredSettings
import com.kaleyra.video_sdk.call.pointer.view.PointerStreamWrapper
import com.kaleyra.video_sdk.call.stream.view.core.Stream
import com.kaleyra.video_sdk.call.stream.view.core.StreamContainer
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.extensions.TextStyleExtensions.shadow
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.extensions.ModifierExtensions.horizontalCutoutPadding
import com.kaleyra.video_sdk.R

/**
 * Featured Stream Tag
 */
const val FeaturedStreamTag = "FeaturedStreamTag"

/**
 * Featured Stream Header auto hide timing in milliseconds
 */
const val HeaderAutoHideMs = 5000L

@Composable
internal fun FeaturedStream(
    stream: StreamUi,
    isFullscreen: Boolean = false,
    fullscreenVisible: Boolean = false,
    showOverlay: Boolean = false,
    onFullscreenClick: () -> Unit,
    onBackPressed: (() -> Unit)? = null,
    headerModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    isTesting: Boolean = false
) {
    Box(modifier = modifier.testTag(FeaturedStreamTag)) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            StreamContainer {
                PointerStreamWrapper(
                    streamView = stream.video?.view,
                    pointerList = stream.video?.pointers,
                    isTesting = isTesting
                ) {
                    val shouldFit = stream.video?.isScreenShare == true
                    Stream(
                        streamView = stream.video?.view?.featuredSettings(
                            if (shouldFit) StreamView.ScaleType.Fit else StreamView.ScaleType.Fill()
                        ) ?: ImmutableView(View(LocalContext.current)),
                        avatar = stream.avatar,
                        avatarVisible = stream.video?.view == null || !stream.video.isEnabled,
                        showOverlay = showOverlay
                    )
                }
            }

            Header(
                username = stream.username,
                fullscreen = isFullscreen,
                fullscreenVisible = fullscreenVisible,
                onBackPressed = onBackPressed,
                onFullscreenClick = onFullscreenClick,
                modifier = headerModifier.horizontalCutoutPadding()
            )
        }
    }
}

@Composable
private fun Header(
    username: String,
    fullscreen: Boolean,
    fullscreenVisible: Boolean,
    onBackPressed: (() -> Unit)? = null,
    onFullscreenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.padding(horizontal = 4.dp, vertical = 4.dp)) {
        if (onBackPressed != null) {
            IconButton(
                icon = rememberVectorPainter(image = Icons.Filled.ArrowBack),
                iconDescription = stringResource(id = R.string.kaleyra_back),
                onClick = onBackPressed
            )
        }

        Text(
            text = username,
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .weight(1f),
            fontWeight = FontWeight.SemiBold,
            style = LocalTextStyle.current.shadow()
        )

        if (fullscreenVisible) {
            IconButton(
                icon = painterResource(
                    id = if (fullscreen) R.drawable.ic_kaleyra_exit_fullscreen else R.drawable.ic_kaleyra_enter_fullscreen
                ),
                iconDescription = stringResource(
                    id = if (fullscreen) R.string.kaleyra_exit_fullscreen else R.string.kaleyra_enter_fullscreen
                ),
                onClick = onFullscreenClick
            )
        }
    }
}

@Preview
@Composable
internal fun FeaturedStreamPreview() {
    KaleyraTheme {
        FeaturedStream(
            stream = streamUiMock,
            onBackPressed = { },
            onFullscreenClick = { }
        )
    }
}
