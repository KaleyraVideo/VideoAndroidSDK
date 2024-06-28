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

package com.kaleyra.video_sdk.call.callinfowidget.view

import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kaleyra.video_sdk.call.callinfowidget.model.WatermarkInfo
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.callinfowidget.kaleyraCollaborationSuiteUIPrefs
import java.io.File

private val MaxWatermarkHeight = 80.dp
private val MaxWatermarkWidth = 300.dp

/**
 * Watermark tag
 */
internal const val WatermarkTag = "WatermarkTag"
internal const val DESIGN_PREFS = "KaleyraCollaborationSuiteUIPrefs"

@Composable
internal fun Watermark(watermarkInfo: WatermarkInfo, modifier: Modifier = Modifier) {
    val isDarkTheme = isSystemInDarkTheme()

    val customWatermarkUri: Uri? = LocalContext.current.kaleyraCollaborationSuiteUIPrefs().getString("call_watermark_image_uri", null)?.takeIf { it.isNotBlank() }?.let {
        if (it.startsWith("content") || it.startsWith("android.resource") || it.startsWith("file") || it.startsWith("http")) Uri.parse(it) else Uri.fromFile(File(it))
    }

    Row(
        modifier = modifier.testTag(WatermarkTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            customWatermarkUri != null || (watermarkInfo.logo != null && (isDarkTheme && watermarkInfo.logo.dark != Uri.EMPTY || !isDarkTheme && watermarkInfo.logo.light != Uri.EMPTY)) -> {
                AsyncImage(
                    model = customWatermarkUri ?: watermarkInfo.logo.let { if (!isDarkTheme) it?.light else it?.dark },
                    contentDescription = stringResource(id = R.string.kaleyra_company_logo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .heightIn(max = MaxWatermarkHeight)
                        .widthIn(max = MaxWatermarkWidth),
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            watermarkInfo.text != null -> {
                Text(
                    text = watermarkInfo.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Call Info Widget Composable Preview
 */
@Preview
@Composable
fun CallInfoWidgetPreview() {
    KaleyraTheme {
        Watermark(
            watermarkInfo = WatermarkInfo(text = "text", logo = null)
        )
    }
}