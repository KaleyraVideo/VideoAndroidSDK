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

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter.State.Empty.painter
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUi
import com.kaleyra.video_sdk.call.signature.model.SignDocumentUiState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.extensions.ModifierExtensions.drawCircleBorder
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.theme.KaleyraTheme.colors


@Composable
internal fun SignDocumentItem(
    signDocumentUi: SignDocumentUi,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surfaceContainerLowest)
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp, start = 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            painter = painterResource(R.drawable.ic_kaleyra_file),
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = signDocumentUi.name
        )
        Spacer(Modifier.size(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = signDocumentUi.name, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.size(2.dp))
            Text(text = signDocumentUi.sender, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp))
            Spacer(Modifier.size(2.dp))
            SingDocumentStateLabel(signDocumentUi.signState)
        }
        SignDocumentItemAction(signDocumentUi.signState, onActionClick)
    }
}

/**
 * Data class to hold the UI elements for different states of a sign document action.
 *
 * This approach significantly reduces the complexity of the composable function by
 * pre-calculating the UI configurations for each state.
 */
@Immutable
private data class SignDocumentActionUiConfig(
    val buttonColors: IconButtonColors,
    val borderColor: Color,
    val iconPainter: Painter,
    val contentDescription: String
)

/**
 * Helper function to calculate the UI configuration based on the sign document state.
 *
 * This keeps the composable function clean and focused on the UI rendering.
 */
@Composable
private fun calculateSignDocumentActionUiConfig(
    signDocumentUiState: SignDocumentUi.SignStateUi
): SignDocumentActionUiConfig {
    return when (signDocumentUiState) {
        SignDocumentUi.SignStateUi.Completed -> SignDocumentActionUiConfig(
            buttonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
            ),
            borderColor = MaterialTheme.colorScheme.primary,
            iconPainter = painterResource(R.drawable.ic_kaleyra_check),
            contentDescription = stringResource(R.string.kaleyra_signature_state_completed)
        )

        is SignDocumentUi.SignStateUi.Failed -> SignDocumentActionUiConfig(
            buttonColors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
                disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
                disabledContentColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.38f)
            ),
            borderColor = MaterialTheme.colorScheme.error,
            iconPainter = painterResource(R.drawable.ic_kaleyra_retry),
            contentDescription = stringResource(R.string.kaleyra_fileshare_retry)
        )

        SignDocumentUi.SignStateUi.Pending,
        SignDocumentUi.SignStateUi.Signing -> SignDocumentActionUiConfig(
            buttonColors = IconButtonDefaults.outlinedIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            ),
            borderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconPainter = painterResource(R.drawable.ic_kaleyra_arrow_right),
            contentDescription = when (signDocumentUiState) {
                SignDocumentUi.SignStateUi.Pending -> stringResource(R.string.kaleyra_signature_sign)
                else -> stringResource(R.string.kaleyra_signature_state_signing)
            }
        )
    }
}

/**
 * Composable function to display an icon button for signing a document,
 * with UI elements changing based on the document's signing state.
 *
 * @param signDocumentUiState The current state of the signing process.
 * @param onActionClick Callback to be executed when the button is clicked.
 */
@Composable
fun SignDocumentItemAction(
    signDocumentUiState: SignDocumentUi.SignStateUi,
    onActionClick: () -> Unit
) {
    val uiConfig = calculateSignDocumentActionUiConfig(signDocumentUiState)

    IconButton(
        modifier = Modifier.size(26.dp),
        colors = uiConfig.buttonColors,
        onClick = onActionClick
    ) {
        Icon(
            modifier = Modifier
                .drawCircleBorder(width = 3.dp, color =  uiConfig.borderColor)
                .padding(7.dp),
            painter = uiConfig.iconPainter,
            contentDescription = uiConfig.contentDescription
        )
    }
}

/**
 * Data class to hold the UI elements for different states of a sign document label.
 *
 * This approach significantly reduces the complexity of the composable function by
 * pre-calculating the UI configurations for each state.
 */
@Immutable
private data class SignDocumentLabelUiConfig(
    val textResId: Int,
    val tint: Color,
    val iconPainter: Painter
)

/**
 * Helper function to calculate the UI configuration based on the sign document state.
 *
 * This keeps the composable function clean and focused on the UI rendering.
 */
@Composable
private fun calculateSignDocumentLabelUiConfig(
    signStateUi: SignDocumentUi.SignStateUi
): SignDocumentLabelUiConfig {
    return when (signStateUi) {
        SignDocumentUi.SignStateUi.Completed -> SignDocumentLabelUiConfig(
            textResId = R.string.kaleyra_signature_state_completed,
            tint = MaterialTheme.colorScheme.primary,
            iconPainter = painterResource(R.drawable.ic_kaleyra_check)
        )
        is SignDocumentUi.SignStateUi.Failed -> SignDocumentLabelUiConfig(
            textResId = R.string.kaleyra_signature_state_failed,
            tint = MaterialTheme.colorScheme.error,
            iconPainter = painterResource(R.drawable.ic_kaleyra_call_sheet_error)
        )
        SignDocumentUi.SignStateUi.Pending -> SignDocumentLabelUiConfig(
            textResId = R.string.kaleyra_signature_state_pending,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            iconPainter = painterResource(R.drawable.ic_kaleyra_clock)
        )
        SignDocumentUi.SignStateUi.Signing -> SignDocumentLabelUiConfig(
            textResId = R.string.kaleyra_signature_state_signing,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            iconPainter = painterResource(R.drawable.ic_kaleyra_clock)
        )
    }
}

/**
 * Composable function to display a label indicating the state of a signed document.
 *
 * @param signStateUi The state of the signed document.
 */
@Composable
fun SingDocumentStateLabel(signStateUi: SignDocumentUi.SignStateUi) {
    val uiConfig = calculateSignDocumentLabelUiConfig(signStateUi)
    val text = stringResource(uiConfig.textResId)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            modifier = Modifier.size(12.dp),
            painter = uiConfig.iconPainter,
            tint = uiConfig.tint,
            contentDescription = text
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(text = text, color = uiConfig.tint, style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp))
    }
}

@Composable
@MultiConfigPreview
fun SignDocumentPreview() = KaleyraTheme {
    SignDocumentItem(
        signDocumentUi = SignDocumentUi(
            id = "id",
            name = "Sign Document",
            uri = ImmutableUri(Uri.parse("http://www.example.com")),
            sender = "sender",
            creationTime = 123L,
            signView = null,
            signState = SignDocumentUi.SignStateUi.Completed
        ),
        onActionClick = {},
    )
}

@Composable
@MultiConfigPreview
fun SignDocumentPreviewFailed() = KaleyraTheme {
    SignDocumentItem(
        signDocumentUi = SignDocumentUi(
            id = "id",
            name = "Sign Document",
            uri = ImmutableUri(Uri.parse("http://www.example.com")),
            sender = "sender",
            creationTime = 123L,
            signView = null,
            signState = SignDocumentUi.SignStateUi.Failed(Throwable("test error"))
        ),
        onActionClick = {},
    )
}

@Composable
@MultiConfigPreview
fun SignDocumentPreviewPending() = KaleyraTheme {
    SignDocumentItem(
        signDocumentUi = SignDocumentUi(
            id = "id",
            name = "Sign Document",
            uri = ImmutableUri(Uri.parse("http://www.example.com")),
            sender = "sender",
            creationTime = 123L,
            signView = null,
            signState = SignDocumentUi.SignStateUi.Pending
        ),
        onActionClick = {},
    )
}

@Composable
@MultiConfigPreview
fun SignDocumentPreviewSigning() = KaleyraTheme {
    SignDocumentItem(
        signDocumentUi = SignDocumentUi(
            id = "id",
            name = "Sign Document",
            uri = ImmutableUri(Uri.parse("http://www.example.com")),
            sender = "sender",
            creationTime = 123L,
            signView = null,
            signState = SignDocumentUi.SignStateUi.Signing
        ),
        onActionClick = {},
    )
}