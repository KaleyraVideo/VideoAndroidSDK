package com.kaleyra.video_sdk.common.usermessages.model

data class ThermalWarningMessage(val hasDisabledNoiseFilter: Boolean = false, val hasDisabledVirtualBackground: Boolean = false) : UserMessage()