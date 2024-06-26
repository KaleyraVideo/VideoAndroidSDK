package com.kaleyra.video_sdk.common.usermessages.model

/**
 * Pin Screenshare Message
 * @property userDisplayName String? user that shared its screen
 * @property action UserMessage.Action.PinScreenshare action associated with Pin Screenshare Message
 * @constructor
 */
class PinScreenshareMessage(val userDisplayName: String) : UserMessage() {

    override val action: Action = Action.PinScreenshare

    /**
     * @suppress
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PinScreenshareMessage

        if (userDisplayName != other.userDisplayName) return false
        if (id != other.id) return false

        return true
    }

    /**
     * @suppress
     */
    override fun hashCode(): Int {
        var result = userDisplayName.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
