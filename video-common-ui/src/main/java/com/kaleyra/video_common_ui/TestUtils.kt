package com.kaleyra.video_common_ui

object TestUtils {

    inline fun <reified T> T.setPrivateField(field: String, value: Any): T  = apply {
        T::class.java.declaredFields
            .find { it.name == field}
            ?.also { it.isAccessible = true }
            ?.set(this, value)
    }

    inline fun <reified T, R> T.getPrivateField(field: String): R  {
        return T::class.java.declaredFields
            .find { it.name == field}
            ?.also { it.isAccessible = true }
            ?.get(this) as R
    }
}