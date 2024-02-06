@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package com.kaleyra.video_sdk.call.bottomsheetm3.view

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.callactions.model.CallAction
import com.kaleyra.video_sdk.call.callactionsm3.model.CallActionsM3UiState
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.theme.KaleyraM3Theme
import kotlinx.coroutines.launch

@Composable
fun MainScreen(windowSizeClass: WindowSizeClass) {
    val bottomSheetState = rememberCallUiBottomSheetState(initialValue = CallUiBottomSheetValue.HalfExpanded)
    bottomSheetState.peekOffset = 32.dp
    val coroutineScope = rememberCoroutineScope()

    val isSystemInDarkTheme = isSystemInDarkTheme()

    val callActionsUiState = remember {
        mutableStateOf(CallActionsM3UiState(
            primaryActionList = ImmutableList(
                listOf(
                    CallAction.HangUp(),
                    CallAction.Microphone(),
                    CallAction.Camera(),
                    CallAction.Answer(),
//                    CallAction.Audio(),
                )
            ),
            secondaryActionList = ImmutableList(
                listOf(
                    CallAction.Audio(),
                    CallAction.VirtualBackground(),
                    CallAction.Chat(),
//                    CallAction.ScreenShare(),
//                    CallAction.Whiteboard(),
                )
            ),
        ))
    }

    CallUiScaffold(
        windowSizeClass = windowSizeClass,
        bottomSheetState = bottomSheetState,
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
                    .padding(top = 24.dp)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "main content",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (bottomSheetState.isExpanded) bottomSheetState.halfExpand()
                            else bottomSheetState.expand()
                        }
                    }
                ) {
                    Text(text = "Click to show bottom sheet")
                }
            }
        },
        primaryActions = {
            CallUiPrimaryActions(callActionsUiState = callActionsUiState.value, isSystemInDarkTheme = isSystemInDarkTheme)
        },
        secondaryActions = {
            CallUiSecondaryActions(callActionsUiState = callActionsUiState.value, isSystemInDarkTheme = isSystemInDarkTheme)
        },
    )
}


@Preview(name = "Dark Mode", showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.PIXEL_4, showBackground = true)
@Preview(name = "Light Mode", showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.PIXEL_4, showBackground = true)
@Composable
internal fun BottomSheetPreview() {
    KaleyraM3Theme {
        MainScreen(
            runCatching { calculateWindowSizeClass(LocalContext.current.findActivity()) }.getOrNull()
                ?: WindowSizeClass.calculateFromSize(DpSize(781.dp, 392.dp))
        )
    }
}

@Preview(name = "Dark Mode", showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, device = Devices.PIXEL_4, widthDp = 781, heightDp = 392)
@Preview(name = "Light Mode", showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, device = Devices.PIXEL_4, widthDp = 781, heightDp = 392)
@Composable
internal fun LandscapeBottomSheetPreview() {
    KaleyraM3Theme {
        MainScreen(
            runCatching { calculateWindowSizeClass(LocalContext.current.findActivity()) }.getOrNull()
                ?: WindowSizeClass.calculateFromSize(DpSize(781.dp, 392.dp))
        )
    }
}

@Preview(name = "Dark Mode", showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true, device = Devices.PIXEL_TABLET)
@Preview(name = "Light Mode", showSystemUi = false, uiMode = Configuration.UI_MODE_NIGHT_NO, showBackground = true, device = Devices.PIXEL_TABLET)
@Composable
internal fun TabletBottomSheetPreview() {
    KaleyraM3Theme {
        MainScreen(
            runCatching { calculateWindowSizeClass(LocalContext.current.findActivity()) }.getOrNull()
                ?: WindowSizeClass.calculateFromSize(DpSize(1280.dp, 768.dp))
        )
    }
}
