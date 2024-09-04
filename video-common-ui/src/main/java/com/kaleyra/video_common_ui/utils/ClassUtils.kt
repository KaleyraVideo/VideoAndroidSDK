package com.kaleyra.video_common_ui.utils



    internal fun <T> instantiateClassWithEmptyConstructor(className: String): T? = Class
        .forName(className)
        .getDeclaredConstructor()
        .newInstance() as? T