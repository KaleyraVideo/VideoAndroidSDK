package com.kaleyra.video_sdk.call.settings.view


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video.noise_filter.DeepFilterNetLoader
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterUiState

@Composable
internal fun NoiseSuppressionSettingsComponent(
    noiseFilterUiState: NoiseFilterUiState,
    onNoiseSuppressionModeRequested: (NoiseFilterModeUi) -> Unit,
    modifier: Modifier = Modifier) {
    if (noiseFilterUiState.supportedNoiseFilterModesUi.value.isEmpty()) return

    Column(modifier = Modifier
        .testTag(NoiseSuppressionSettingsTag)
        .then(modifier!!)) {
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
                modifier = Modifier.padding(start = SettingsGroupHorizontalStartPadding, end = SettingsGroupHorizontalEndPadding, top = SettingsGroupVerticalPadding, bottom = SettingsGroupVerticalPadding)
            ) {
                if (noiseFilterUiState.supportedNoiseFilterModesUi.value.contains(NoiseFilterModeUi.DeepFilterAi)) {
                    SettingsItemComponent(
                        iconPainter = painterResource(R.drawable.ic_kaleyra_noise_filter_ai),
                        text = stringResource(R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai),
                        testTag = NoiseSuppressionDeepFilterOptionTag,
                        subtitle = subtitleFor(noiseFilterUiState.deepFilerLoadingState),
                        isToggleable = false,
                        isSelected = noiseFilterUiState.currentNoiseFilterModeUi is NoiseFilterModeUi.DeepFilterAi,
                        isEnabled = !noiseFilterUiState.isDeviceOverHeating,
                        onCheckedChange = {
                            onNoiseSuppressionModeRequested.invoke(NoiseFilterModeUi.DeepFilterAi)
                        }
                    )
                }
                if (noiseFilterUiState.supportedNoiseFilterModesUi.value.contains(NoiseFilterModeUi.Standard)) {
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
                }
                if (noiseFilterUiState.supportedNoiseFilterModesUi.value.contains(NoiseFilterModeUi.None)) {
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
}

@Composable
private fun subtitleFor(loadingState: DeepFilterNetLoader.LoadingState): String? = when (loadingState) {
    DeepFilterNetLoader.LoadingState.InProgress -> stringResource(R.string.kaleyra_strings_info_deep_filter_net_model_loading_in_progress)
    DeepFilterNetLoader.LoadingState.Unavailable -> stringResource(R.string.kaleyra_strings_info_deep_filter_net_model_unavailable)
    else -> null
}