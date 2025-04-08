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

package com.kaleyra.video_sdk.call.signature.view

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView

internal const val SignViewTag = "WhiteboardViewTag"

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun SignDocumentView(
    signView: View,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        val runningInPreview = LocalInspectionMode.current
        if (!runningInPreview) {
            AndroidView(
                modifier = Modifier.testTag(SignViewTag),
                factory = {
                    val parentView = signView.parent as? ViewGroup
                    parentView?.removeView(signView)
                    signView.apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        isFocusable = true
                        isFocusableInTouchMode = true
                    }
                },
                update = { signView ->
                    signView.setOnTouchListener { v, _ ->
                        v.parent.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                }
            )
        }
    }
}
