package com.kaleyra.video_common_ui.theme.resource

/**
 * Represents a color resource that can have different values for light and dark themes.
 *
 * @property light The color resource ID to use in light theme.
 * @property dark The color resource ID to use in dark theme.
 */
data class ColorResource(val light: Int, val dark: Int) {
    /**
     * Creates a new [ColorResource] instance with the same color resource ID for both light and dark themes.
     *
     * This constructor is useful when you want to use the same color in both themes.
     *
     * @param color The color resource ID to use in both light and dark themes.
     */
    constructor(color: Int): this(color, color)
}