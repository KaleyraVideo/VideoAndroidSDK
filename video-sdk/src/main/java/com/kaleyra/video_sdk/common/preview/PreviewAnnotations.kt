package com.kaleyra.video_sdk.common.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Day Mode")
internal annotation class DayModePreview

@Preview(name = "Night Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
internal annotation class NightModePreview

@Preview(name = "Day Mode Tablet", device = Devices.TABLET)
internal annotation class DayModeTabletPreview

@Preview( name = "Night Mode Tablet", device = Devices.TABLET, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class NightModeTabletPreview

@Preview(name = "Day Mode Landscape", device = "spec:parent=pixel_5,orientation=landscape")
internal annotation class DayModeLandscapePreview

@Preview(name = "Night Mode Landscape", device = "spec:parent=pixel_5,orientation=landscape", uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class NightModeLandscapePreview

@DayModePreview
@NightModePreview
@DayModeLandscapePreview
@NightModeLandscapePreview
@DayModeTabletPreview
@NightModeTabletPreview
internal annotation class MultiConfigPreview