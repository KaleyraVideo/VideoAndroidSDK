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

package com.kaleyra.video_sdk.call.whiteboard.view

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.video_sdk.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus

internal const val LinearProgressIndicatorTag = "LinearProgressIndicatorTag"
internal const val WhiteboardViewTag = "WhiteboardViewTag"

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun WhiteboardContent(
    whiteboardView: View,
    loading: Boolean,
    upload: WhiteboardUploadUi?,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        val runningInPreview = LocalInspectionMode.current
        if (!runningInPreview) {
            val webViewInteractionSource = remember { MutableInteractionSource() }
            val focusInteraction = remember { FocusInteraction.Focus() }
            AndroidView(
                modifier = Modifier
                    .testTag(WhiteboardViewTag)
                    .highlightOnFocus(webViewInteractionSource),
                factory = {
                    val parentView = whiteboardView.parent as? ViewGroup
                    parentView?.removeView(whiteboardView)
                    whiteboardView.apply {
                        visibility = if (loading) View.GONE else View.VISIBLE
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        isFocusable = true
                        isFocusableInTouchMode = true
                        setOnFocusChangeListener { _, hasFocus ->
                            val interaction = if (hasFocus) focusInteraction else FocusInteraction.Unfocus(focusInteraction)
                            webViewInteractionSource.tryEmit(interaction)
                        }
                    }
                },
                update = { whiteboardView ->
                    whiteboardView.visibility = if (loading) View.GONE else View.VISIBLE
                    whiteboardView.setOnTouchListener { v, _ ->
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        v.performClick()
                        false
                    }
                }
            )
        }

        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .testTag(LinearProgressIndicatorTag),
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (upload != null) {
            WhiteboardUploadCard(upload = upload)
        }
    }
}
