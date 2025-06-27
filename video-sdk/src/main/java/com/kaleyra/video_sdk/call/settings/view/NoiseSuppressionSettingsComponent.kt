package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kaleyra.video.noise_filter.DeepFilterNetModuleLoader
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterModeUi
import com.kaleyra.video_sdk.call.settings.model.NoiseFilterUiState

private val NoiseSuppressionSettingsGroupBorderWidth = 1.dp
private val NoiseSuppressionSettingsGroupRoundCorner = 11.dp
private val NoiseSuppressionSettingsGroupHorizontalStartPadding = 24.dp
private val NoiseSuppressionSettingsGroupHorizontalEndPadding = 16.dp
private val NoiseSuppressionSettingsGroupTitleBottomPadding = 16.dp
private val NoiseSuppressionSettingsGroupVerticalPadding = 8.dp

internal const val NoiseSuppressionDeepFilterOptionTag = "NoiseSuppressionDeepFilterOptionTag"
internal const val NoiseSuppressionStandardOptionTag = "NoiseSuppressionStandardOptionTag"
internal const val VoiceSettingsTag = "VoiceSettingsTag"
internal const val NoiseSuppressionSettingsTag = "NoiseSuppressionSettingsTag"

@Composable
internal fun NoiseSuppressionSettingsComponent(
    noiseFilterUiState: NoiseFilterUiState,
    onNoiseSuppressionModeRequested: (NoiseFilterModeUi) -> Unit,
    modifier: Modifier = Modifier) {
    if (noiseFilterUiState.supportedNoiseFilterModesUi.value.isEmpty()) return

    Column(modifier = Modifier
        .testTag(NoiseSuppressionSettingsTag)
        .then(modifier)) {
        Text(
            modifier = Modifier.padding(bottom = NoiseSuppressionSettingsGroupTitleBottomPadding),
            text = stringResource(R.string.kaleyra_strings_info_noise_suppression),
            style = MaterialTheme.typography.bodyLarge)
        Card(
            border = BorderStroke(NoiseSuppressionSettingsGroupBorderWidth, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(NoiseSuppressionSettingsGroupRoundCorner)
        ) {
            Column {
                if (noiseFilterUiState.supportedNoiseFilterModesUi.value.contains(NoiseFilterModeUi.DeepFilterAi)) {
                    SettingsItemComponent(
                        modifier = Modifier
                            .height(SettingsItemComponentHeight + NoiseSuppressionSettingsGroupVerticalPadding + NoiseSuppressionSettingsGroupVerticalPadding / 2)
                            .padding(start = NoiseSuppressionSettingsGroupHorizontalStartPadding, end = NoiseSuppressionSettingsGroupHorizontalEndPadding, top = NoiseSuppressionSettingsGroupVerticalPadding, bottom = NoiseSuppressionSettingsGroupVerticalPadding / 2),
                        iconPainter = painterResource(R.drawable.ic_kaleyra_noise_filter_ai),
                        text = stringResource(R.string.kaleyra_strings_action_noise_suppression_deepfilter_ai),
                        testTag = NoiseSuppressionDeepFilterOptionTag,
                        subtitle = subtitleFor(noiseFilterUiState.deepFilerLoadingState),
                        isToggleable = false,
                        isSelected = noiseFilterUiState.currentNoiseFilterModeUi is NoiseFilterModeUi.DeepFilterAi,
                        isEnabled = !noiseFilterUiState.isDeviceOverHeating,
                        onCheckedChange = {
                            onNoiseSuppressionModeRequested.invoke(NoiseFilterModeUi.DeepFilterAi)
                        },
                        highlightFocusShape = RoundedCornerShape(topStart = NoiseSuppressionSettingsGroupRoundCorner, topEnd = NoiseSuppressionSettingsGroupRoundCorner)
                    )
                }
                if (noiseFilterUiState.supportedNoiseFilterModesUi.value.contains(NoiseFilterModeUi.Standard)) {
                    SettingsItemComponent(
                        modifier = Modifier
                            .height(SettingsItemComponentHeight + NoiseSuppressionSettingsGroupVerticalPadding + NoiseSuppressionSettingsGroupVerticalPadding / 2)
                            .padding(start = NoiseSuppressionSettingsGroupHorizontalStartPadding, end = NoiseSuppressionSettingsGroupHorizontalEndPadding, top = NoiseSuppressionSettingsGroupVerticalPadding / 2, bottom = NoiseSuppressionSettingsGroupVerticalPadding),
                        iconPainter = painterResource(R.drawable.ic_kaleyra_noise_filter_standard),
                        text = stringResource(R.string.kaleyra_strings_action_noise_suppression_standard),
                        testTag = NoiseSuppressionStandardOptionTag,
                        isToggleable = false,
                        isSelected = noiseFilterUiState.currentNoiseFilterModeUi is NoiseFilterModeUi.Standard,
                        isEnabled = true,
                        onCheckedChange = {
                            onNoiseSuppressionModeRequested.invoke(NoiseFilterModeUi.Standard)
                        },
                        highlightFocusShape = RoundedCornerShape(bottomStart = NoiseSuppressionSettingsGroupRoundCorner, bottomEnd = NoiseSuppressionSettingsGroupRoundCorner)
                    )
                }
            }
        }
    }
}

@Composable
private fun subtitleFor(loadingState: DeepFilterNetModuleLoader.LoadingState): String? = when (loadingState) {
    DeepFilterNetModuleLoader.LoadingState.InProgress -> stringResource(R.string.kaleyra_strings_info_deep_filter_net_model_loading_in_progress)
    DeepFilterNetModuleLoader.LoadingState.Unavailable -> stringResource(R.string.kaleyra_strings_info_deep_filter_net_model_unavailable)
    else -> null
}