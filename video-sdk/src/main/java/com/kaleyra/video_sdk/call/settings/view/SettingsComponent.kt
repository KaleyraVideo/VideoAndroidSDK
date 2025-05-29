package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.settings.viewmodel.NoiseFilterViewModel
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.theme.KaleyraTheme

val SettingsGroupBorderWidth = 1.dp
val SettingsGroupRoundCorner = 11.dp
val SettingsGroupHorizontalPadding = 16.dp
val SettingsGroupVerticalPadding = 8.dp
const val DisabledOptionAlpha = 0.4f

internal const val VoiceSettingsTag = "VoiceSettingsTag"
internal const val VirtualBackgroundSettingsTag = "VirtualBackgroundSettingsTag"
internal const val NoiseSuppressionSettingsTag = "NoiseSuppressionSettingsTag"

internal const val NoiseSuppressionDeepFilterOptionTag = "NoiseSuppressionDeepFilterOptionTag"
internal const val NoiseSuppressionStandardOptionTag = "NoiseSuppressionStandardOptionTag"
internal const val NoiseSuppressionNoneOptionTag = "NoiseSuppressionNoneOptionTag"

internal const val VirtualBackgroundBlurOptionTag = "VirtualBackgroundBlurOptionTag"
internal const val VirtualBackgroundImageOptionTag = "VirtualBackgroundImageOptionTag"
internal const val VirtualBackgroundNoneOptionTag = "VirtualBackgroundNoneOptionTag"

internal const val AudioDeviceMutedOptionsTag = "AudioDeviceMutedOptionsTag"


@Composable
internal fun SettingsComponent(
    audioOutputViewModel: AudioOutputViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AudioOutputViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    noiseFilterViewModel: NoiseFilterViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = NoiseFilterViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    virtualBackgroundViewModel: VirtualBackgroundViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = VirtualBackgroundViewModel.provideFactory(::requestCollaborationViewModelConfiguration)),
    scrollState: ScrollState = rememberScrollState(),
    onChangeAudioOutputRequested: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onUserMessageActionClick: (UserMessage) -> Unit = { },
    isLargeScreen: Boolean,
    isTesting: Boolean = false
) {
    val noiseFilterUiState = noiseFilterViewModel.uiState.collectAsStateWithLifecycle()
    val audioOutputUiState = audioOutputViewModel.uiState.collectAsStateWithLifecycle()
    val virtualBackgroundUiState = virtualBackgroundViewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose(onDismiss)
    }

    Column(modifier = modifier) {
        SettingsAppBar(
            onBackPressed = onDismiss,
            lazyGridState = rememberLazyGridState(),
            isLargeScreen = isLargeScreen
        )

        Column(modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
        ) {
            VoiceSettingsComponent(
                audioOutputUiState.value, { disableAllSounds ->
                    if (disableAllSounds) audioOutputViewModel.disableCallSounds()
                    else audioOutputViewModel.enableCallSounds()
                }, onChangeAudioOutputRequested)
            Spacer(modifier = Modifier.size(32.dp))
            NoiseSuppressionSettingsComponent(noiseFilterUiState.value) { requestedNoiseSuppressionMode ->
                noiseFilterViewModel.setNoiseSuppressionMode(requestedNoiseSuppressionMode)
            }
            Spacer(modifier = Modifier.size(32.dp))
            VirtualBackgroundSettingsComponent(virtualBackgroundUiState.value) { requestedVirtualBackground ->
                virtualBackgroundViewModel.setEffect(requestedVirtualBackground)
            }
        }
    }

    if (!isTesting) StackedUserMessageComponent(modifier = Modifier.padding(top = 56.dp), onActionClick = onUserMessageActionClick)
}

@MultiConfigPreview
@Composable
fun SettingsComponentPreview() {
    KaleyraTheme {
        Surface {
            SettingsComponent(
                onDismiss = {},
                onChangeAudioOutputRequested = {},
                isLargeScreen = false
            )
        }
    }
}