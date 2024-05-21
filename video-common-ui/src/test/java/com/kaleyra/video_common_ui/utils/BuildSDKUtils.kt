package com.kaleyra.video_common_ui.utils

import android.os.Build
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

internal fun mockSdkInt(sdkInt: Int) {
    val sdkIntField = Build.VERSION::class.java.getField("SDK_INT")
    sdkIntField.isAccessible = true

    val getDeclaredFields0: Method = Class::class.java.getDeclaredMethod("getDeclaredFields0", Boolean::class.javaPrimitiveType)
    getDeclaredFields0.isAccessible = true
    val fields = getDeclaredFields0.invoke(Field::class.java, false) as Array<Field>
    var modifiers: Field? = null
    for (each in fields) {
        if ("modifiers" == each.name) {
            modifiers = each
            break
        }
    }

    modifiers!!.isAccessible = true
    modifiers.setInt(sdkIntField, sdkIntField.modifiers and Modifier.FINAL.inv())

    sdkIntField.set(null, sdkInt)
}
