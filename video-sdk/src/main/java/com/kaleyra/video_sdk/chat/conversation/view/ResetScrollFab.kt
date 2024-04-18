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

@file:OptIn(ExperimentalAnimationApi::class)

package com.kaleyra.video_sdk.chat.conversation.view

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

private val ResetScrollFabIconSize = 16.dp
private val ResetScrollFabHorizontalPadding = 8.dp

@Composable
internal fun ResetScrollFab(
    modifier: Modifier = Modifier,
    counter: Int,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    AnimatedVisibility(
        visible = enabled,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = modifier
    ) {
        SmallFloatingActionButton(
            onClick = onClick,
            modifier = Modifier.highlightOnFocus(interactionSource),
            shape = CircleShape,
            interactionSource = interactionSource
        ) {
            Row(
                modifier = Modifier.padding(horizontal = ResetScrollFabHorizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (counter > 0) {
                    Text(
                        text = "$counter",
                        fontSize = MaterialTheme.typography.labelMedium.fontSize,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Icon(
                    painter = painterResource(id = R.drawable.kaleyra_z_arrow_down_3),
                    contentDescription = stringResource(id = R.string.kaleyra_chat_scroll_to_last_message),
                    modifier = Modifier.size(ResetScrollFabIconSize)
                )
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Light Mode")
@Composable
internal fun ResetScrollFabDarkPreview() {
    KaleyraM3Theme {
        ResetScrollFab(counter = 5, onClick = { }, enabled = true)
    }
}
