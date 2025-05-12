package com.kaleyra.video_common_ui.utils

object StringUtils {
    fun String.uppercaseFirstChar() = replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}