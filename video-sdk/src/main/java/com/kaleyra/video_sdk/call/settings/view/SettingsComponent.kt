package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video.noise_filter.DeepFilterNetLoader
import com.kaleyra.video_common_ui.requestCollaborationViewModelConfiguration
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.audiooutput.model.AudioOutputUiState
import com.kaleyra.video_sdk.call.audiooutput.view.painterFor
import com.kaleyra.video_sdk.call.audiooutput.viewmodel.AudioOutputViewModel
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterUiState
import com.kaleyra.video_sdk.call.settings.viewmodel.NoiseFilterViewModel
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState
import com.kaleyra.video_sdk.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.StackedUserMessageComponent
import com.kaleyra.video_sdk.theme.KaleyraTheme

private val SettingsGroupBorderWidth = 1.dp
private val SettingsGroupRoundCorner = 11.dp
private val SettingsGroupHorizontalPadding = 16.dp
private val SettingsGroupVerticalPadding = 8.dp

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

    Box(modifier = modifier) {
        Column(modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
        ) {
            SettingsAppBar(
                onBackPressed = onDismiss,
                lazyGridState = rememberLazyGridState(),
                isLargeScreen = isLargeScreen
            )
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
        if (!isTesting) StackedUserMessageComponent(modifier = Modifier.padding(top = 56.dp), onActionClick = onUserMessageActionClick)
    }
}

@Composable
internal fun VirtualBackgroundSettingsComponent(
    virtualBackgroundUiState: VirtualBackgroundUiState,
    onVirtualBackgroundRequested: (VirtualBackgroundUi) -> Unit
) {
    if (virtualBackgroundUiState.backgroundList.value.isEmpty()) return
    if (virtualBackgroundUiState.backgroundList.value.size == 1 && virtualBackgroundUiState.backgroundList.value.first() is VirtualBackgroundUi.None) return

    Column(modifier = Modifier.testTag(VirtualBackgroundSettingsTag)) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.kaleyra_strings_info_camera_settings),
            style = MaterialTheme.typography.bodyLarge)
        Box(modifier = Modifier
            .border(
                width = SettingsGroupBorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(SettingsGroupRoundCorner)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = SettingsGroupHorizontalPadding, vertical = SettingsGroupVerticalPadding)
            ) {
                SettingsItemComponent(
                    iconPainter = painterResource(R.drawable.ic_kaleyra_virtual_background_blur),
                    text = stringResource(R.string.kaleyra_virtual_background_blur),
                    testTag = VirtualBackgroundBlurOptionTag,
                    isToggleable = false,
                    isSelected = virtualBackgroundUiState.currentBackground is VirtualBackgroundUi.Blur,
                    isEnabled = true,
                    onCheckedChange = {
                        onVirtualBackgroundRequested.invoke(virtualBackgroundUiState.backgroundList.value.first { it is VirtualBackgroundUi.Blur })
                    }
                )
                SettingsItemComponent(
                    iconPainter = painterResource(R.drawable.ic_kaleyra_virtual_background_image),
                    text = stringResource(R.string.kaleyra_virtual_background_image),
                    testTag = VirtualBackgroundImageOptionTag,
                    isToggleable = false,
                    isSelected = virtualBackgroundUiState.currentBackground is VirtualBackgroundUi.Image,
                    isEnabled = true,
                    onCheckedChange = {
                        onVirtualBackgroundRequested.invoke(virtualBackgroundUiState.backgroundList.value.first { it is VirtualBackgroundUi.Image })
                    }
                )
                SettingsItemComponent(
                    iconPainter = painterResource(R.drawable.ic_kaleyra_virtual_background_none),
                    text = stringResource(R.string.kaleyra_virtual_background_none),
                    testTag = VirtualBackgroundNoneOptionTag,
                    isToggleable = false,
                    isSelected = virtualBackgroundUiState.currentBackground is VirtualBackgroundUi.None,
                    isEnabled = true,
                    onCheckedChange = {
                        onVirtualBackgroundRequested.invoke(virtualBackgroundUiState.backgroundList.value.first { it is VirtualBackgroundUi.None })
                    }
                )
            }
        }
    }
}

@Composable
internal fun NoiseSuppressionSettingsComponent(noiseFilterUiState: NoiseFilterUiState, onNoiseSuppressionModeRequested: (NoiseFilterModeUi) -> Unit) {
    if (noiseFilterUiState.supportedNoiseFilterModesUi.value.isEmpty()) return

    Column(modifier = Modifier.testTag(NoiseSuppressionSettingsTag)) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.kaleyra_strings_info_noise_suppression),
            style = MaterialTheme.typography.bodyLarge)
        Box(modifier = Modifier
            .border(
                width = SettingsGroupBorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(SettingsGroupRoundCorner)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = SettingsGroupHorizontalPadding, vertical = SettingsGroupVerticalPadding)
            ) {
                if (noiseFilterUiState.supportedNoiseFilterModesUi.value.contains(NoiseFilterModeUi.DeepFilterAi)) {
                    SettingsItemComponent(
                        iconPainter = painterResource(R.drawable.ic_kaleyra_noise_filter_ai),
                        text = stringResource(R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai),
                        testTag = NoiseSuppressionDeepFilterOptionTag,
                        subtitle = subtitleFor(noiseFilterUiState.deepFilerLoadingState),
                        isToggleable = false,
                        isSelected = noiseFilterUiState.currentNoiseFilterModeUi is NoiseFilterModeUi.DeepFilterAi,
                        isEnabled = noiseFilterUiState.deepFilerLoadingState !is DeepFilterNetLoader.LoadingState.Unavailable,
                        onCheckedChange = {
                            onNoiseSuppressionModeRequested.invoke(NoiseFilterModeUi.DeepFilterAi)
                        }
                    )
                }
                SettingsItemComponent(
                    iconPainter = painterResource(R.drawable.ic_kaleyra_noise_filter_standard),
                    text = stringResource(R.string.kaleyra_strings_action_noise_suppression_standard),
                    testTag = NoiseSuppressionStandardOptionTag,
                    isToggleable = false,
                    isSelected = noiseFilterUiState.currentNoiseFilterModeUi is NoiseFilterModeUi.Standard,
                    isEnabled = true,
                    onCheckedChange = {
                        onNoiseSuppressionModeRequested.invoke(NoiseFilterModeUi.Standard)
                    }
                )
                SettingsItemComponent(
                    iconPainter = painterResource(R.drawable.ic_kaleyra_noise_filter_none),
                    text = stringResource(R.string.kaleyra_strings_action_noise_suppression_none),
                    testTag = NoiseSuppressionNoneOptionTag,
                    isToggleable = false,
                    isSelected = noiseFilterUiState.currentNoiseFilterModeUi is NoiseFilterModeUi.None,
                    isEnabled = true,
                    onCheckedChange = {
                        onNoiseSuppressionModeRequested.invoke(NoiseFilterModeUi.None)
                    }
                )
            }
        }
    }
}

@Composable
private fun subtitleFor(loadingState: DeepFilterNetLoader.LoadingState): String? = when (loadingState) {
    DeepFilterNetLoader.LoadingState.InProgress -> stringResource(R.string.kaleyra_strings_info_deep_filter_net_model_loading_in_progress)
    DeepFilterNetLoader.LoadingState.Unavailable -> stringResource(R.string.kaleyra_strings_info_deep_filter_net_model_unavailable)
    else -> null
}

@Composable
internal fun VoiceSettingsComponent(
    audioOutputUiState: AudioOutputUiState,
    onDisableAllSoundsRequested: (disabled: Boolean) -> Unit,
    onChangeAudioOutputRequested: () -> Unit,
) {
    val currentAudioDeviceUi = audioOutputUiState.audioDeviceList.value.firstOrNull { it.id == audioOutputUiState.playingDeviceId }

    Column(modifier = Modifier.testTag(VoiceSettingsTag)) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = stringResource(R.string.kaleyra_strings_info_voice_settings),
            style = MaterialTheme.typography.bodyLarge)
        Box(modifier = Modifier
            .border(
                width = SettingsGroupBorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(SettingsGroupRoundCorner)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = SettingsGroupHorizontalPadding, vertical = SettingsGroupVerticalPadding)
            ) {
                SettingsItemComponent(
                    iconPainter = painterResource(R.drawable.ic_kaleyra_muted),
                    text = stringResource(R.string.kaleyra_strings_action_disable_all_sounds),
                    testTag = AudioDeviceMutedOptionsTag,
                    isToggleable = true,
                    isSelected = !audioOutputUiState.areCallSoundsEnabled,
                    isEnabled = true,
                    onCheckedChange = { onDisableAllSoundsRequested.invoke(it) }
                )
                SettingsItemComponent(
                    iconPainter = currentAudioDeviceUi?.let { painterFor(it) } ?: painterResource(R.drawable.ic_kaleyra_loud_speaker),
                    text = stringResource(R.string.kaleyra_strings_action_voice_change_audio_output),
                    isSelected = false,
                    isEnabled = true,
                    onCheckedChange = { onChangeAudioOutputRequested.invoke() }
                )
            }
        }
    }
}

@Composable
fun SettingsItemComponent(
    iconPainter: Painter,
    text: String,
    subtitle: String? = null,
    isToggleable: Boolean? = null,
    testTag: String? = null,
    isSelected: Boolean,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(40.dp)) {
        Box(modifier = Modifier.clearAndSetSemantics {}) {
            Icon(painter = iconPainter, modifier = Modifier.size(24.dp), contentDescription = text)
        }
        Spacer(Modifier.size(8.dp))
        val displayRadioButton = isToggleable == false
        val displaySwitch = isToggleable == true
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    contentDescription = text
                }
                .selectable(
                    selected = isSelected,
                    enabled = true,
                    role = when {
                        displaySwitch -> Role.Switch
                        displayRadioButton -> Role.RadioButton
                        else -> Role.Button
                    },
                    onClick = {
                        onCheckedChange(true)
                    }
                )
                .optionalTestTag(testTag),
            verticalArrangement = Arrangement.Center) {
            Text(text = text, style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        when {
            displaySwitch -> {
                Switch(
                    modifier = Modifier
                        .clearAndSetSemantics {},
                    checked = isSelected,
                    onCheckedChange = { onCheckedChange(it) },
                    enabled = isEnabled,
                )
            }

            displayRadioButton -> {
                RadioButton(modifier = Modifier
                    .padding(end = 0.dp)
                    .clearAndSetSemantics {},
                    selected = isSelected,
                    onClick = { onCheckedChange(true) },
                    enabled = isEnabled)
            }

            else -> {
                IconButton(modifier = Modifier
                    .size(24.dp)
                    .clearAndSetSemantics {},
                    enabled = isEnabled,
                    onClick = { onCheckedChange(true) }
                ) {
                    Icon(painter = painterResource(R.drawable.kaleyra_f_chevron_right), contentDescription = text)
                }
            }
        }
    }
}

private fun Modifier.optionalTestTag(tag: String?): Modifier {
    return if (tag != null) this.testTag(tag) else this
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