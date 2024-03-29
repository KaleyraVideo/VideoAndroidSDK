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

package com.kaleyra.video_sdk.common.userinput

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.input.TextFieldTag

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun UserInputText(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    maxLines: Int = Int.MAX_VALUE,
    onDirectionLeft: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .height(48.dp)
            .then(modifier),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = textFieldValue,
            textStyle = MaterialTheme.typography.subtitle1.copy(color = LocalContentColor.current),
            onValueChange = { onTextChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TextFieldTag)
                .onPreviewKeyEvent {
                    if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (it.key) {
                        Key.Tab -> {
                            focusManager.moveFocus(FocusDirection.Next); true
                        }
                        Key.DirectionUp -> {
                            focusManager.moveFocus(FocusDirection.Up); true
                        }
                        Key.DirectionDown -> {
                            focusManager.moveFocus(FocusDirection.Down); true
                        }
                        Key.DirectionRight -> {
                            focusManager.moveFocus(FocusDirection.Right); true
                        }
                        Key.DirectionLeft -> {
                            onDirectionLeft.invoke(); true
                        }
                        else -> false
                    }
                },
            interactionSource = interactionSource,
            maxLines = maxLines,
            cursorBrush = SolidColor(MaterialTheme.colors.secondary)
        )

        val hintColor = LocalContentColor.current.copy(alpha = 0.5f)
        if (textFieldValue.text.isEmpty()) {
            Text(
                text = stringResource(id = R.string.kaleyra_edit_text_input_placeholder),
                style = MaterialTheme.typography.subtitle1.copy(color = hintColor)
            )
        }
    }
}
