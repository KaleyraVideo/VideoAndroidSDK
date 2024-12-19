package com.kaleyra.video_sdk.call.callinfo.model

import android.content.Context


sealed class TextRef(open val id: Int, open val args: List<Any>? = emptyList()) {

    abstract fun resolve(context: Context): String

    data class PlainText(val text: String?): TextRef(-1) {
        override fun resolve(context: Context) = text ?: ""
    }
    data class StringResource(override val id: Int, override val args: List<Any>? = emptyList()): TextRef(id, args) {
        override fun resolve(context: Context) = context.resources.getString(id, *args!!.toTypedArray())
    }
    data class PluralResource(override val id: Int, val quantity: Int, override val args: List<Any>? = emptyList()): TextRef(id, args) {
        override fun resolve(context: Context) = context.resources.getQuantityString(id, quantity, *args!!.toTypedArray())
    }
}