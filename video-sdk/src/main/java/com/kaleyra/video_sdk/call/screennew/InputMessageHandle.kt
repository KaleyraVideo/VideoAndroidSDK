package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.CameraMessageText
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.InputMessageHost
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.view.MicMessageText

internal const val InputMessageDragHandleTag = "InputMessageDragHandleTag"

@Composable
internal fun InputMessageHandle(inputMessage: InputMessage?) {
    val snackbarHostState = remember { SnackbarHostState() }
    val showHandle by remember {
        derivedStateOf { snackbarHostState.currentSnackbarData == null }
    }
    Box(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        if (showHandle) {
            CallBottomSheetDefaults.HDragHandle(Modifier.testTag(InputMessageDragHandleTag))
        }
        InputMessageHost(
            inputMessage = inputMessage,
            snackbarHostState = snackbarHostState,
            micMessage = { enabled -> MicMessageText(enabled) },
            cameraMessage = { enabled -> CameraMessageText(enabled) },
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}