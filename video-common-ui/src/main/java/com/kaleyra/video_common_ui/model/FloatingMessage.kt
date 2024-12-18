package com.kaleyra.video_common_ui.model

import java.util.UUID

/**
 * A class representing a floating message that can be used to display a
 * floating text and optionally a button with text and/or icon and a custom callback.
 * @property id String
 * @property body String
 * @property button Button?
 * @constructor
 */
class FloatingMessage(body: String, button: Button? = null) {

    val id = UUID.randomUUID().toString()

    var body: String = body
        set(value) {
            field = value
            onBodyUpdated?.invoke(field)
        }

    var button: Button? = button
        set(value) {
            field = value
            onButtonUpdated?.invoke(field)
        }

    internal var onDismissed: (() -> Unit)? = null
    internal var onBodyUpdated: ((String) -> Unit)? = null
    internal var onButtonUpdated: ((Button?) -> Unit)? = null
        set(value) {
            field = value
            button?.onButtonUpdated = field
        }

    /**
     * Class representing the button inside a floating message class
     * @property id String button identifier
     * @property text String button text
     * @property icon Int? optional button icon
     * @property action Function0<Unit> button action
     * @constructor
     */
    class Button(
        text: String,
        icon: Int? = null,
        action: () -> Unit
    ) {

        val id = UUID.randomUUID().toString()

        var text: String = text
            set(value) {
                field = value
                onButtonUpdated?.invoke(this)
            }
        var icon: Int? = icon
            set(value) {
                field = value
                onButtonUpdated?.invoke(this)
            }

        var action: () -> Unit = action
            set(value) {
                field = value
                onButtonUpdated?.invoke(this)
            }

        internal var onButtonUpdated: ((Button?) -> Unit)? = null

        override fun equals(other: Any?): Boolean {
            (other as? Button)?.let {
                return this.id == other.id &&
                    this.text == other.text &&
                    this.icon == other.icon &&
                    this.action == other.action
            }
            return false
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + text.hashCode()
            result = 31 * result + (icon ?: 0)
            result = 31 * result + action.hashCode()
            return result
        }
    }

    fun dismiss() {
        onDismissed?.invoke()
        onBodyUpdated = null
        onButtonUpdated = null
        onDismissed = null
    }

    override fun equals(other: Any?): Boolean {
        (other as? FloatingMessage)?.let {
            return this.id == other.id &&
                this.body == other.body &&
                this.button == other.button
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + body.hashCode()
        result = 31 * result + (button?.hashCode() ?: 0)
        return result
    }
}
