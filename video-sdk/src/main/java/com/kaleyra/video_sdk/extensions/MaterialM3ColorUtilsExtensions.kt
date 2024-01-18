package com.kaleyra.video_sdk.extensions

import com.kaleyra.video_common_ui.material.colorutils.scheme.Scheme
import com.kaleyra.video_common_ui.material.colorutils.scheme.SchemeUtils.toColorScheme

/**
 * Generate a light color scheme from input argb seed
 * @receiver ColorScheme the resulting light color scheme
 * @param seed Int input argb seed
 */
fun lightColorSchemeFrom(seed: Int) = Scheme.light(seed).toColorScheme()


/**
 * Generate a dark color scheme from input argb seed
 * @receiver ColorScheme the resulting dark color scheme
 * @param seed Int input argb seed
 */
fun darkColorSchemeFrom(seed: Int) = Scheme.dark(seed).toColorScheme()