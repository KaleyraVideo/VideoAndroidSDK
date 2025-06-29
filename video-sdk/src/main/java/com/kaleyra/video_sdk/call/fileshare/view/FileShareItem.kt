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

package com.kaleyra.video_sdk.call.fileshare.view

import android.content.Context
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kaleyra.video_common_ui.utils.TimestampUtils
import com.kaleyra.video_common_ui.utils.extensions.UriExtensions.getMimeType
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.ProgressIndicatorTag
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.fileshare.model.mockDownloadSharedFile
import com.kaleyra.video_sdk.call.fileshare.model.mockUploadSharedFile
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.common.text.Ellipsize
import com.kaleyra.video_sdk.common.text.EllipsizeText
import com.kaleyra.video_sdk.extensions.MimeTypeExtensions.isArchiveMimeType
import com.kaleyra.video_sdk.extensions.MimeTypeExtensions.isImageMimeType
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlin.math.roundToInt

private const val FileMediaType = "MediaType"
private const val FileArchiveType = "ArchiveType"
private val LinearProgressIndicatorWidth = 3000.dp
private val LinearProgressIndicatorHeight = 4.dp

@Composable
internal fun FileShareItem(
    sharedFile: SharedFileUi,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileTypeAndSize(
            fileUri = sharedFile.uri,
            fileSize = sharedFile.size,
            modifier = Modifier.padding(start = 6.dp)
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SharedFileInfoAndProgress(
                    sharedFile = sharedFile,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                ActionButton(
                    sharedFileState = sharedFile.state,
                    onActionClick = onActionClick
                )
            }

            if (sharedFile.state is SharedFileUi.State.Error) {
                ErrorMessage(sharedFile.isMine)
            }
        }
    }
}

@Composable
private fun FileTypeAndSize(
    fileUri: ImmutableUri,
    fileSize: Long?,
    modifier: Modifier = Modifier
) {
    val contentColorAlpha70 = LocalContentColor.current.copy(alpha = .7f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val fileType = getFileType(LocalContext.current, fileUri.value)
        Icon(
            painter = painterResource(
                id = when (fileType) {
                    FileMediaType -> R.drawable.ic_kaleyra_image
                    FileArchiveType -> R.drawable.ic_kaleyra_zip
                    else -> R.drawable.ic_kaleyra_file
                }
            ),
            contentDescription = stringResource(
                id = when (fileType) {
                    FileMediaType -> R.string.kaleyra_fileshare_media
                    FileArchiveType -> R.string.kaleyra_fileshare_archive
                    else -> R.string.kaleyra_fileshare_miscellaneous
                }
            ),
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (fileSize != null && fileSize >= 0L) {
                Formatter.formatShortFileSize(LocalContext.current, fileSize)
            } else {
                // The file size is NA when is Download && state != InProgress && state != Success
                stringResource(id = R.string.kaleyra_fileshare_na)
            },
            style = MaterialTheme.typography.labelSmall,
            color = contentColorAlpha70
        )
    }
}

@Composable
private fun SharedFileInfoAndProgress(
    sharedFile: SharedFileUi,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        val progress by animateFloatAsState(targetValue = when (sharedFile.state) {
            is SharedFileUi.State.InProgress -> sharedFile.state.progress
            is SharedFileUi.State.Success -> 1f
            else -> 0f
        })

        EllipsizeText(
            text = sharedFile.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.titleSmall.fontSize,
            fontWeight = MaterialTheme.typography.titleSmall.fontWeight ?: FontWeight.Normal,
            ellipsize = Ellipsize.Middle
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .padding(vertical = 2.dp)
                .size(LinearProgressIndicatorWidth, LinearProgressIndicatorHeight)
                .clip(RoundedCornerShape(percent = 50))
                .testTag(ProgressIndicatorTag),
            drawStopIndicator = {},
            color = MaterialTheme.colorScheme.primary,
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(
                    id = if (sharedFile.isMine) R.drawable.ic_kaleyra_upload else R.drawable.ic_kaleyra_download
                ),
                contentDescription = stringResource(id = if (sharedFile.isMine) R.string.kaleyra_fileshare_upload else R.string.kaleyra_fileshare_download),
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (sharedFile.isMine) stringResource(id = R.string.kaleyra_fileshare_you) else sharedFile.sender,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = when (sharedFile.state) {
                    is SharedFileUi.State.InProgress -> stringResource(
                        id = R.string.kaleyra_fileshare_progress,
                        (sharedFile.state.progress * 100).roundToInt()
                    )

                    else -> TimestampUtils.parseTime(sharedFile.time)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButton(
    sharedFileState: SharedFileUi.State,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val onSurfaceAlpha50 = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)
    IconButton(
        onClick = onActionClick,
        interactionSource = interactionSource,
        modifier = modifier.highlightOnFocus(interactionSource)
    ) {
        Icon(
            painter = painterResource(
                id = when (sharedFileState) {
                    is SharedFileUi.State.Available -> R.drawable.ic_kaleyra_download
                    is SharedFileUi.State.Success -> R.drawable.ic_kaleyra_check
                    is SharedFileUi.State.Error -> R.drawable.ic_kaleyra_retry
                    else -> R.drawable.ic_kaleyra_fileshare_cancel
                }
            ),
            contentDescription = stringResource(
                id = when (sharedFileState) {
                    is SharedFileUi.State.Available -> R.string.kaleyra_fileshare_download_descr
                    is SharedFileUi.State.Success -> R.string.kaleyra_fileshare_open_file
                    is SharedFileUi.State.Error -> R.string.kaleyra_fileshare_retry
                    else -> R.string.kaleyra_fileshare_cancel
                }
            ),
            tint = when (sharedFileState) {
                is SharedFileUi.State.Error -> MaterialTheme.colorScheme.onError
                is SharedFileUi.State.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                is SharedFileUi.State.Available,
                is SharedFileUi.State.Cancelled,
                is SharedFileUi.State.InProgress,
                is SharedFileUi.State.Pending -> onSurfaceAlpha50
            },
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp)
                .border(
                    width = 2.dp,
                    color = when (sharedFileState) {
                        is SharedFileUi.State.Error -> MaterialTheme.colorScheme.error
                        is SharedFileUi.State.Success -> MaterialTheme.colorScheme.primary
                        is SharedFileUi.State.Available,
                        is SharedFileUi.State.Cancelled,
                        is SharedFileUi.State.InProgress,
                        is SharedFileUi.State.Pending -> onSurfaceAlpha50
                    },
                    shape = CircleShape
                )
                .background(
                    color = when (sharedFileState) {
                        is SharedFileUi.State.Success -> MaterialTheme.colorScheme.primary
                        is SharedFileUi.State.Error -> MaterialTheme.colorScheme.error
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                )
                .padding(8.dp)
        )
    }
}

private fun getFileType(context: Context, uri: Uri): String? {
    val mimeType = uri.getMimeType(context) ?: ""
    return when {
        mimeType.isImageMimeType() -> FileMediaType
        mimeType.isArchiveMimeType() -> FileArchiveType
        else -> null
    }
}

@Composable
private fun ErrorMessage(isMyMessage: Boolean) {
    Text(
        text = stringResource(
            id = if (isMyMessage) R.string.kaleyra_fileshare_upload_error else R.string.kaleyra_fileshare_download_error
        ),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.labelSmall
    )
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareItemInProgressPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile)
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareItemCancelledPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Cancelled))
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareItemErrorPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Error))
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareItemAvailablePreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Available))
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareItemPendingPreview() {
    FileShareItemPreview(sharedFile = mockUploadSharedFile.copy(state = SharedFileUi.State.Pending))
}

@DayModePreview
@NightModePreview
@Composable
internal fun FileShareItemSuccessPreview() {
    FileShareItemPreview(sharedFile = mockDownloadSharedFile.copy(state = SharedFileUi.State.Success(ImmutableUri(Uri.EMPTY))))
}

@Composable
private fun FileShareItemPreview(sharedFile: SharedFileUi) {
    KaleyraTheme {
        Surface {
            FileShareItem(
                sharedFile = sharedFile,
                onActionClick = {}
            )
        }
    }
}