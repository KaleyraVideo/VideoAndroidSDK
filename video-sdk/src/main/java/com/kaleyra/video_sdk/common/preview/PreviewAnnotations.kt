package com.kaleyra.video_sdk.common.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Day Mode")
internal annotation class DayModePreview

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Night Mode")
internal annotation class NightModePreview

@Preview(device = Devices.TABLET, name = "Day Mode Tablet")
internal annotation class DayModeTabletPreview

@Preview(device = Devices.TABLET, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Night Mode Tablet")
internal annotation class NightModeTabletPreview

@Preview(name = "Day Mode Landscape", device = "spec:parent=pixel_5,orientation=landscape")
internal annotation class DayModeLandscapePreview

@Preview(name = "Night Mode Landscape", device = "spec:parent=pixel_5,orientation=landscape")
internal annotation class NightModeLandscapePreview

@DayModePreview
@NightModePreview
@DayModeLandscapePreview
@NightModeLandscapePreview
@DayModeTabletPreview
@NightModeTabletPreview
internal annotation class MultiConfigPreview