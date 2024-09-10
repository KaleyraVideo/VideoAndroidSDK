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

package com.kaleyra.video_sdk.chat.input

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.userinput.ChatUserInputText
import com.kaleyra.video_sdk.extensions.ModifierExtensions.clearFocusOnKeyboardDismiss
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.extensions.ModifierExtensions.supportRtl
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal const val TextFieldTag = "TextFieldTag"

@Composable
internal fun ChatUserInput(
    onTextChanged: () -> Unit,
    onMessageSent: (String) -> Unit,
    onDirectionLeft: (() -> Unit) = { },
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    var textState by remember { mutableStateOf(TextFieldValue()) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .focusGroup()
                .highlightOnFocus(interactionSource)
                .padding(start = 16.dp, bottom = 14.dp, top = 16.dp, end = 12.dp)
        ) {

            ChatUserInputText(
                textFieldValue = textState,
                onTextChanged = {
                    textState = it
                    onTextChanged()
                },
                maxLines = 4,
                onDirectionLeft = onDirectionLeft,
                interactionSource = interactionSource,
                modifier = Modifier
                    .clearFocusOnKeyboardDismiss()
                    .weight(1.0f)
            )

            Spacer(modifier = Modifier.size(12.dp))

            val buttonInteractionSource = remember { MutableInteractionSource() }
            FilledIconButton(
                onClick = {
                    onMessageSent(textState.text)
                    textState = TextFieldValue()
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_kaleyra_send),
                        contentDescription = stringResource(id = R.string.kaleyra_chat_send),
                        modifier = Modifier.fillMaxSize(.66f)
                    )
                },
                enabled = textState.text.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    disabledContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                interactionSource = buttonInteractionSource,
                modifier = Modifier
                    .supportRtl()
                    .size(44.dp)
                    .highlightOnFocus(buttonInteractionSource)
            )
        }
    }

}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ChatUserInputPreview() = KaleyraTheme {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        ChatUserInput(onTextChanged = { }, onMessageSent = { })
    }
}
