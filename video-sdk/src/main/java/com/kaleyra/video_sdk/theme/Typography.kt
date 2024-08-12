package com.kaleyra.video_sdk.theme

import com.kaleyra.video_common_ui.KaleyraFontFamily

internal val defaultTypography = androidx.compose.material3.Typography()
internal val typography = androidx.compose.material3.Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = KaleyraFontFamily.default),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = KaleyraFontFamily.default),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = KaleyraFontFamily.default),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = KaleyraFontFamily.default),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = KaleyraFontFamily.default),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = KaleyraFontFamily.default),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = KaleyraFontFamily.default),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = KaleyraFontFamily.default),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = KaleyraFontFamily.default),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = KaleyraFontFamily.default),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = KaleyraFontFamily.default),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = KaleyraFontFamily.default),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = KaleyraFontFamily.default),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = KaleyraFontFamily.default),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = KaleyraFontFamily.default)
)