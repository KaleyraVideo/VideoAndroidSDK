package com.kaleyra.video_sdk.call.bottomsheet.view.streammenu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.bottomsheet.view.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactions.view.CancelAction
import com.kaleyra.video_sdk.call.callactions.view.FullscreenAction
import com.kaleyra.video_sdk.call.callactions.view.PinAction
import com.kaleyra.video_sdk.call.callactions.view.ZoomAction
import com.kaleyra.video_sdk.call.stream.viewmodel.StreamViewModel
import com.kaleyra.video_sdk.common.preview.DayModePreview
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.preview.NightModePreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun VStreamMenuContent(
    selectedStreamId: String,
    onDismiss: () -> Unit,
    onFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StreamViewModel = viewModel<StreamViewModel>(
        factory = StreamViewModel.provideFactory(::requestCollaborationViewModelConfiguration)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFullscreenMenu by remember { mutableStateOf(false) }

    val onFullscreenClick: (Boolean) -> Unit = { isFullscreen ->
        if (!isFullscreen) {
            viewModel.clearFullscreenStream()
            showFullscreenMenu = false
            onDismiss()
        } else {
            viewModel.setFullscreenStream(selectedStreamId)
            showFullscreenMenu = true
            onFullscreen()
        }
    }

    BackHandler(enabled = showFullscreenMenu) {
        onFullscreenClick(false)
    }
    
    VStreamMenuContent(
        isFullscreen = uiState.streamItems.value.fastAny { it.id == selectedStreamId && it.isFullscreen() },
        hasVideo = uiState.streamItems.value.fastAny { it.id == selectedStreamId && it.hasVideoEnabled() },
        isPinned = uiState.streamItems.value.fastAny { it.id == selectedStreamId && it.isPinned() },
        isPinLimitReached = uiState.hasReachedMaxPinnedStreams,
        onCancelClick = onDismiss,
        onFullscreenClick = { isFullscreen -> onFullscreenClick(!isFullscreen) },
        onPinClick = { isPinned ->
            if (isPinned) viewModel.unpinStream(selectedStreamId)
            else viewModel.pinStream(selectedStreamId)
            onDismiss()
        },
        onZoomClick = { viewModel.zoom(selectedStreamId) },
        modifier = modifier
    )
}

@Composable
internal fun VStreamMenuContent(
    isFullscreen: Boolean,
    hasVideo: Boolean,
    isPinned: Boolean,
    isPinLimitReached: Boolean,
    onCancelClick: () -> Unit,
    onFullscreenClick: (Boolean) -> Unit,
    onPinClick: (Boolean) -> Unit,
    onZoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(14.dp)) {
        FullscreenAction(
            label = false,
            fullscreen = isFullscreen,
            onClick = { onFullscreenClick(isFullscreen) }
        )
        Spacer(modifier = Modifier.height(SheetItemsSpacing))
        if (hasVideo) {
            ZoomAction(onClick = onZoomClick, label = false)
            Spacer(modifier = Modifier.height(SheetItemsSpacing))
        }
        if (!isFullscreen) {
            PinAction(
                label = false,
                pin = isPinned,
                enabled = isPinned || !isPinLimitReached,
                onClick = { onPinClick(isPinned) }
            )
            Spacer(modifier = Modifier.height(SheetItemsSpacing))
        }
        CancelAction(
            label = false,
            onClick = onCancelClick
        )
    }
}

@MultiConfigPreview
@Composable
internal fun VStreamMenuContentPreview() {
    KaleyraTheme {
        Surface {
            VStreamMenuContent(false, true, false, false, {}, {}, {}, {})
        }
    }
}

@MultiConfigPreview
@Composable
internal fun VStreamFullscreenMenuContentPreview() {
    KaleyraTheme {
        Surface {
            VStreamMenuContent(true, true, false, false, {}, {}, {}, {})
        }
    }
}

@DayModePreview
@NightModePreview
@Composable
internal fun VStreamPinLimitMenuContentPreview() {
    KaleyraTheme {
        Surface {
            VStreamMenuContent(false, true, false, true, {}, {}, {}, {})
        }
    }
}