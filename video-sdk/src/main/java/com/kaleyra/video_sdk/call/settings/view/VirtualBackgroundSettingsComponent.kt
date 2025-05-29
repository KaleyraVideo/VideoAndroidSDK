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
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.video_sdk.call.virtualbackground.model.VirtualBackgroundUiState

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
                    isEnabled = !virtualBackgroundUiState.isDeviceOverHeating,
                    onCheckedChange = {
                        onVirtualBackgroundRequested.invoke(virtualBackgroundUiState.backgroundList.value.first { it is VirtualBackgroundUi.Blur })
                    }
                )
                SettingsItemComponent(
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