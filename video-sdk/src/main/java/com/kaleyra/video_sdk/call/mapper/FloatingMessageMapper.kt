package com.kaleyra.video_sdk.call.mapper

import com.kaleyra.video_common_ui.model.FloatingMessage
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage

internal fun FloatingMessage.toCustomAlertMessage() =
    AlertMessage.CustomMessage(
        body = body,
        button = button?.let { button ->
            AlertMessage.CustomMessage.Button(
                text = button.text,
                icon = button.icon,
                action = button.action
            )
        }
    )
