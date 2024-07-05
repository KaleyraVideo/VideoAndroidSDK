package com.kaleyra.video_sdk.call.screennew

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.appbar.view.CallAppBarComponent
import com.kaleyra.video_sdk.call.bottomsheetnew.CallBottomSheetDefaults
import com.kaleyra.video_sdk.call.bottomsheetnew.CallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.inputmessage.model.InputMessage
import com.kaleyra.video_sdk.call.bottomsheetnew.rememberCallSheetState
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.VSheetContent
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetdragcontent.VSheetDragContent
import com.kaleyra.video_sdk.call.callactions.viewmodel.CallActionsViewModel
import com.kaleyra.video_sdk.call.callinfowidget.model.Logo
import com.kaleyra.video_sdk.call.callscreenscaffold.HCallScreenScaffold
import com.kaleyra.video_sdk.call.streamnew.model.core.StreamUi
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallScreen(
    windowSizeClass: WindowSizeClass,
    onBackPressed: () -> Unit,
) {
    val isCompactHeight = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

    val sheetState = rememberCallSheetState()
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var modalSheetComponent: ModalSheetComponent? by remember { mutableStateOf(null) }

    KaleyraM3Theme {
        if (isCompactHeight) {
            HCallScreen(
                windowSizeClass = windowSizeClass,
                sheetState = sheetState,
                modalSheetState = modalSheetState,
                modalSheetComponent = modalSheetComponent,
                onModalSheetComponentChange = { modalSheetComponent = it },
                onBackPressed = onBackPressed
            )
        } else {
            VCallScreen(
                windowSizeClass = windowSizeClass,
                sheetState = sheetState,
                modalSheetState = modalSheetState,
                modalSheetComponent = modalSheetComponent,
                onModalSheetComponentChange = { modalSheetComponent = it },
                onBackPressed = onBackPressed
            )
        }
    }
}

internal const val CompactScreenMaxActions = 5
internal const val LargeScreenMaxActions = 8

@Composable
internal fun rememberOnMicClick(callActionsViewModel: CallActionsViewModel): (Boolean) -> Unit {
    val activity = LocalContext.current.findActivity()
    return remember(callActionsViewModel) { { _: Boolean -> callActionsViewModel.toggleMic(activity) } }
}

@Composable
internal fun rememberOnCameraClick(callActionsViewModel: CallActionsViewModel): (Boolean) -> Unit {
    val activity = LocalContext.current.findActivity()
    return remember(callActionsViewModel) {
        { _: Boolean ->
            callActionsViewModel.toggleCamera(
                activity
            )
        }
    }
}

@Composable
internal fun rememberOnScreenShareClick(
    callActionsViewModel: CallActionsViewModel,
    onStartScreenShareClick: () -> Unit,
): (Boolean) -> Unit {
    return remember(callActionsViewModel) {
        { _: Boolean ->
            if (!callActionsViewModel.tryStopScreenShare()) onStartScreenShareClick()
        }
    }
}

@Composable
internal fun rememberOnChatClick(callActionsViewModel: CallActionsViewModel): () -> Unit {
    val activity = LocalContext.current.findActivity()
    return remember(callActionsViewModel) { { callActionsViewModel.showChat(activity) } }
}

@Composable
internal fun rememberOnHangUpClick(callActionsViewModel: CallActionsViewModel): () -> Unit {
    return remember(callActionsViewModel) { { callActionsViewModel.hangUp() } }
}

@Composable
internal fun callScreenScaffoldPaddingValues(horizontal: Dp, vertical: Dp): PaddingValues {
    return WindowInsets.systemBars
        .add(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
        .add(WindowInsets(left = horizontal, top = vertical, right = horizontal, bottom = vertical))
        .asPaddingValues()
}