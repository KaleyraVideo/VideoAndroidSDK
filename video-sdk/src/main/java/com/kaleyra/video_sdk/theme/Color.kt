/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.theme
import androidx.compose.ui.graphics.Color

// CUSTOM COLORS
internal val kaleyra_answer_light_color = Color(0xff7ed321)
internal val kaleyra_answer_dark_color = Color(0xff48a100)

internal val kaleyra_hang_up_light_color = Color(0xffff0000)
internal val kaleyra_hang_up_dark_color = Color(0xffc20000)

// KALEYRA MATERIAL M2 THEME
internal val kaleyra_theme_light_primary = Color.White
internal val kaleyra_theme_light_primaryVariant = Color(0xFFEFEFEF)
internal val kaleyra_theme_light_onPrimary = Color.Black
internal val kaleyra_theme_light_secondary = Color(0xFFD80D30)
internal val kaleyra_theme_light_secondaryVariant = Color(0xFF6C6C6C)
internal val kaleyra_theme_light_onSecondary = Color.White
internal val kaleyra_theme_light_error = Color(0xFFC70000)
internal val kaleyra_theme_light_onError = Color.White
internal val kaleyra_theme_light_background = Color.White
internal val kaleyra_theme_light_onBackground = Color.Black
internal val kaleyra_theme_light_surface = Color.White
internal val kaleyra_theme_light_onSurface = Color.Black

internal val kaleyra_theme_dark_primary = Color(0xFF303030)
internal val kaleyra_theme_dark_primaryVariant = Color(0xFF1E1E1E)
internal val kaleyra_theme_dark_onPrimary = Color.White
internal val kaleyra_theme_dark_secondary = Color(0xFF9E000A)
internal val kaleyra_theme_dark_secondaryVariant = Color.White
internal val kaleyra_theme_dark_onSecondary = Color.White
internal val kaleyra_theme_dark_error = Color(0xFFC70000)
internal val kaleyra_theme_dark_onError = Color.White
internal val kaleyra_theme_dark_background = Color(0xFF0E0E0E)
internal val kaleyra_theme_dark_onBackground = Color.White
internal val kaleyra_theme_dark_surface = Color(0xFF242424)
internal val kaleyra_theme_dark_onSurface = Color.White

/**
// KALEYRA MATERIAL M3 THEME
// DE-COMMENT THIS WHEN THE MATERIAL 3 DEPENDENCY WILL BE ADDED
val kaleyra_m3_theme_light_primary = Color(0xFFB32735)
val kaleyra_m3_theme_light_onPrimary = Color(0xFFFFFFFF)
val kaleyra_m3_theme_light_primaryContainer = Color(0xFFFFDAD9)
val kaleyra_m3_theme_light_onPrimaryContainer = Color(0xFF410008)
val kaleyra_m3_theme_light_secondary = Color(0xFF9C413C)
val kaleyra_m3_theme_light_onSecondary = Color(0xFFFFFFFF)
val kaleyra_m3_theme_light_secondaryContainer = Color(0xFFFFDAD6)
val kaleyra_m3_theme_light_onSecondaryContainer = Color(0xFF410003)
val kaleyra_m3_theme_light_tertiary = Color(0xFF7C5800)
val kaleyra_m3_theme_light_onTertiary = Color(0xFFFFFFFF)
val kaleyra_m3_theme_light_tertiaryContainer = Color(0xFFFFDEA7)
val kaleyra_m3_theme_light_onTertiaryContainer = Color(0xFF271900)
val kaleyra_m3_theme_light_error = Color(0xFFBA1A1A)
val kaleyra_m3_theme_light_errorContainer = Color(0xFFFFDAD6)
val kaleyra_m3_theme_light_onError = Color(0xFFFFFFFF)
val kaleyra_m3_theme_light_onErrorContainer = Color(0xFF410002)
val kaleyra_m3_theme_light_background = Color(0xFFFFFBFF)
val kaleyra_m3_theme_light_onBackground = Color(0xFF201A1A)
val kaleyra_m3_theme_light_surface = Color(0xFFFFFBFF)
val kaleyra_m3_theme_light_onSurface = Color(0xFF201A1A)
val kaleyra_m3_theme_light_surfaceVariant = Color(0xFFF4DDDC)
val kaleyra_m3_theme_light_onSurfaceVariant = Color(0xFF524343)
val kaleyra_m3_theme_light_outline = Color(0xFF857372)
val kaleyra_m3_theme_light_inverseOnSurface = Color(0xFFFBEEED)
val kaleyra_m3_theme_light_inverseSurface = Color(0xFF362F2E)
val kaleyra_m3_theme_light_inversePrimary = Color(0xFFFFB3B2)
val kaleyra_m3_theme_light_shadow = Color(0xFF000000)
val kaleyra_m3_theme_light_surfaceTint = Color(0xFFB32735)
val kaleyra_m3_theme_light_outlineVariant = Color(0xFFD7C1C1)
val kaleyra_m3_theme_light_scrim = Color(0xFF000000)

val kaleyra_m3_theme_dark_primary = Color(0xFFFFB3B2)
val kaleyra_m3_theme_dark_onPrimary = Color(0xFF680013)
val kaleyra_m3_theme_dark_primaryContainer = Color(0xFF910721)
val kaleyra_m3_theme_dark_onPrimaryContainer = Color(0xFFFFDAD9)
val kaleyra_m3_theme_dark_secondary = Color(0xFFFFB3AD)
val kaleyra_m3_theme_dark_onSecondary = Color(0xFF5F1413)
val kaleyra_m3_theme_dark_secondaryContainer = Color(0xFF7E2A27)
val kaleyra_m3_theme_dark_onSecondaryContainer = Color(0xFFFFDAD6)
val kaleyra_m3_theme_dark_tertiary = Color(0xFFF7BD48)
val kaleyra_m3_theme_dark_onTertiary = Color(0xFF412D00)
val kaleyra_m3_theme_dark_tertiaryContainer = Color(0xFF5E4200)
val kaleyra_m3_theme_dark_onTertiaryContainer = Color(0xFFFFDEA7)
val kaleyra_m3_theme_dark_error = Color(0xFFFFB4AB)
val kaleyra_m3_theme_dark_errorContainer = Color(0xFF93000A)
val kaleyra_m3_theme_dark_onError = Color(0xFF690005)
val kaleyra_m3_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val kaleyra_m3_theme_dark_background = Color(0xFF201A1A)
val kaleyra_m3_theme_dark_onBackground = Color(0xFFECE0DF)
val kaleyra_m3_theme_dark_surface = Color(0xFF201A1A)
val kaleyra_m3_theme_dark_onSurface = Color(0xFFECE0DF)
val kaleyra_m3_theme_dark_surfaceVariant = Color(0xFF524343)
val kaleyra_m3_theme_dark_onSurfaceVariant = Color(0xFFD7C1C1)
val kaleyra_m3_theme_dark_outline = Color(0xFFA08C8B)
val kaleyra_m3_theme_dark_inverseOnSurface = Color(0xFF201A1A)
val kaleyra_m3_theme_dark_inverseSurface = Color(0xFFECE0DF)
val kaleyra_m3_theme_dark_inversePrimary = Color(0xFFB32735)
val kaleyra_m3_theme_dark_shadow = Color(0xFF000000)
val kaleyra_m3_theme_dark_surfaceTint = Color(0xFFFFB3B2)
val kaleyra_m3_theme_dark_outlineVariant = Color(0xFF524343)
val kaleyra_m3_theme_dark_scrim = Color(0xFF000000)

val seed = Color(0xFFAF2433)
**/
