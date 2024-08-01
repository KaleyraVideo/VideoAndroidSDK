package com.kaleyra.video_sdk.call.bottomsheetnew.streammenu

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactionnew.CancelAction
import com.kaleyra.video_sdk.call.callactionnew.FullscreenAction
import com.kaleyra.video_sdk.call.callactionnew.PinAction
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.call.streamnew.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun HStreamMenuContent(
    selectedStreamId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = viewModel<StreamViewModel>(
        factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFullscreenMenu by remember { mutableStateOf(false) }

    HStreamMenuContent(
        fullscreen = uiState.fullscreenStream?.id == selectedStreamId,
        pin = uiState.pinnedStreams.value.fastAny { stream -> stream.id == selectedStreamId },
        onCancelClick = onDismiss,
        onFullscreenClick = { isFullscreen ->
            if (isFullscreen) viewModel.fullscreen(null)
            else viewModel.fullscreen(selectedStreamId)
            showFullscreenMenu = true
        },
        onPinClick = { isPinned ->
            if (isPinned) viewModel.unpin(selectedStreamId)
            else viewModel.pin(selectedStreamId)
            onDismiss()
        },
        modifier = modifier
    )
}

@Composable
internal fun HStreamMenuContent(
    fullscreen: Boolean,
    pin: Boolean,
    onCancelClick: () -> Unit,
    onFullscreenClick: (Boolean) -> Unit,
    onPinClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(14.dp)) {
        if (!fullscreen) {
            CancelAction(
                label = true,
                onClick = onCancelClick
            )
            Spacer(modifier = Modifier.width(SheetItemsSpacing))
            PinAction(
                label = true,
                pin = pin,
                onClick = { onPinClick(pin) }
            )
            Spacer(modifier = Modifier.width(SheetItemsSpacing))
        }
        FullscreenAction(
            label = true,
            fullscreen = fullscreen,
            onClick = { onFullscreenClick(fullscreen) }
        )
    }
}

@MultiConfigPreview
@Composable
internal fun HStreamMenuContentPreview() {
    KaleyraM3Theme {
        Surface {
            HStreamMenuContent(false, false, {}, {}, {})
        }
    }
}

@MultiConfigPreview
@Composable
internal fun HStreamFullscreenMenuContentPreview() {
    KaleyraM3Theme {
        Surface {
            HStreamMenuContent(true, false, {}, {}, {})
        }
    }
}