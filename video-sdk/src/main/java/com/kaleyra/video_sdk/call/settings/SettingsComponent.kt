package com.kaleyra.video_sdk.call.settings

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
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.view.NoiseSuppressionSettingsComponent
import com.kaleyra.video_sdk.call.settings.view.SettingsAppBar
import com.kaleyra.video_sdk.call.settings.view.VirtualBackgroundSettingsComponent
import com.kaleyra.video_sdk.call.settings.view.VoiceSettingsComponent
import com.kaleyra.video_sdk.call.settings.viewmodel.NoiseFilterViewModel
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.snackbar.view.ThermalWarningSnackbar
import com.kaleyra.video_sdk.common.spacer.NavigationBarsSpacer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi

private val SettingsComponentHorizontalPadding = 16.dp
private val SettingsComponentVerticalSpacer = 24.dp
private val SettingsComponentTopPadding = 56.dp

@ExperimentalCoroutinesApi
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

        val horizontalPaddingModifier = Modifier.padding(horizontal = SettingsComponentHorizontalPadding)

        Column(modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
        ) {
            if (noiseFilterUiState.value.isDeviceOverHeating) {
                Spacer(modifier = Modifier.size(SettingsComponentVerticalSpacer))
                ThermalWarningSnackbar()
            }
            Spacer(modifier = Modifier.size(SettingsComponentVerticalSpacer))
            VoiceSettingsComponent(
                audioOutputUiState.value, { disableAllSounds ->
                    if (disableAllSounds) audioOutputViewModel.disableCallSounds()
                    else audioOutputViewModel.enableCallSounds()
                },
                onChangeAudioOutputRequested,
                horizontalPaddingModifier
            )
            if (noiseFilterUiState.value.supportedNoiseFilterModesUi.value.any { it is NoiseFilterModeUi.DeepFilterAi }) {
                Spacer(modifier = Modifier.size(SettingsComponentVerticalSpacer))
                NoiseSuppressionSettingsComponent(
                    noiseFilterUiState.value,
                    { requestedNoiseSuppressionMode ->
                        noiseFilterViewModel.setNoiseSuppressionMode(requestedNoiseSuppressionMode)
                    },
                    horizontalPaddingModifier
                )
            }
            Spacer(modifier = Modifier.size(SettingsComponentVerticalSpacer))
            VirtualBackgroundSettingsComponent(
                virtualBackgroundUiState.value,
                { requestedVirtualBackground ->
                    virtualBackgroundViewModel.setEffect(requestedVirtualBackground)
                },
                horizontalPaddingModifier
            )
            NavigationBarsSpacer()
        }
    }

    if (!isTesting) StackedUserMessageComponent(modifier = Modifier.padding(top = SettingsComponentTopPadding), onActionClick = onUserMessageActionClick)
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