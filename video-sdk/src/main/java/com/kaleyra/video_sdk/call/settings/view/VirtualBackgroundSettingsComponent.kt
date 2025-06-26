package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
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
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState

private val SettingsGroupBorderWidth = 1.dp
private val SettingsGroupRoundCorner = 11.dp
private val SettingsGroupTitleBottomPadding = 16.dp
private val SettingsGroupHorizontalStartPadding = 24.dp
private val SettingsGroupHorizontalEndPadding = 16.dp
private val SettingsGroupVerticalPadding = 8.dp

internal const val VirtualBackgroundSettingsTag = "VirtualBackgroundSettingsTag"
internal const val VirtualBackgroundBlurOptionTag = "VirtualBackgroundBlurOptionTag"
internal const val VirtualBackgroundImageOptionTag = "VirtualBackgroundImageOptionTag"
internal const val VirtualBackgroundNoneOptionTag = "VirtualBackgroundNoneOptionTag"

@Composable
internal fun VirtualBackgroundSettingsComponent(
    virtualBackgroundUiState: VirtualBackgroundUiState,
    onVirtualBackgroundRequested: (VirtualBackgroundUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (virtualBackgroundUiState.backgroundList.value.isEmpty()) return
    if (virtualBackgroundUiState.backgroundList.value.size == 1 && virtualBackgroundUiState.backgroundList.value.first() is VirtualBackgroundUi.None) return

    Column(modifier = Modifier
        .testTag(VirtualBackgroundSettingsTag)
        .then(modifier)) {
        Text(
            modifier = Modifier.padding(bottom = SettingsGroupTitleBottomPadding),
            text = stringResource(R.string.kaleyra_strings_info_camera_settings),
            style = MaterialTheme.typography.bodyLarge)
        Card(
            border = BorderStroke(SettingsGroupBorderWidth, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(SettingsGroupRoundCorner)
        ) {
            Column {
                SettingsItemComponent(
                    modifier = Modifier.padding(start = SettingsGroupHorizontalStartPadding, end = SettingsGroupHorizontalEndPadding, top = SettingsGroupVerticalPadding, bottom = SettingsGroupVerticalPadding / 2),
                    iconPainter = painterResource(R.drawable.ic_kaleyra_virtual_background_blur),
                    text = stringResource(R.string.kaleyra_virtual_background_blur),
                    testTag = VirtualBackgroundBlurOptionTag,
                    isToggleable = false,
                    isSelected = virtualBackgroundUiState.currentBackground is VirtualBackgroundUi.Blur,
                    isEnabled = !virtualBackgroundUiState.isDeviceOverHeating,
                    onCheckedChange = {
                        onVirtualBackgroundRequested.invoke(virtualBackgroundUiState.backgroundList.value.first { it is VirtualBackgroundUi.Blur })
                    },
                    highlightFocusShape = RoundedCornerShape(topStart = SettingsGroupRoundCorner, topEnd = SettingsGroupRoundCorner)
                )
                SettingsItemComponent(
                    modifier = Modifier.padding(start = SettingsGroupHorizontalStartPadding, end = SettingsGroupHorizontalEndPadding, top = SettingsGroupVerticalPadding / 2, bottom = SettingsGroupVerticalPadding / 2),
                    iconPainter = painterResource(R.drawable.ic_kaleyra_virtual_background_image),
                    text = stringResource(R.string.kaleyra_call_sheet_virtual_background),
                    testTag = VirtualBackgroundImageOptionTag,
                    isToggleable = false,
                    isSelected = virtualBackgroundUiState.currentBackground is VirtualBackgroundUi.Image,
                    isEnabled = !virtualBackgroundUiState.isDeviceOverHeating,
                    onCheckedChange = {
                        onVirtualBackgroundRequested.invoke(virtualBackgroundUiState.backgroundList.value.first { it is VirtualBackgroundUi.Image })
                    }
                )
                SettingsItemComponent(
                    modifier = Modifier.padding(start = SettingsGroupHorizontalStartPadding, end = SettingsGroupHorizontalEndPadding, top = SettingsGroupVerticalPadding / 2, bottom = SettingsGroupVerticalPadding),
                    iconPainter = painterResource(R.drawable.ic_kaleyra_virtual_background_none),
                    text = stringResource(R.string.kaleyra_virtual_background_none),
                    testTag = VirtualBackgroundNoneOptionTag,
                    isToggleable = false,
                    isSelected = virtualBackgroundUiState.currentBackground is VirtualBackgroundUi.None,
                    isEnabled = true,
                    onCheckedChange = {
                        onVirtualBackgroundRequested.invoke(virtualBackgroundUiState.backgroundList.value.first { it is VirtualBackgroundUi.None })
                    },
                    highlightFocusShape = RoundedCornerShape(bottomStart = SettingsGroupRoundCorner, bottomEnd = SettingsGroupRoundCorner)
                )
            }
        }
    }
}