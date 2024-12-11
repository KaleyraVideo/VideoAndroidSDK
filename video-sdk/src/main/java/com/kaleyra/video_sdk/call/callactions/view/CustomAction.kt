package com.kaleyra.video_sdk.call.callactions.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.kaleyra.video_sdk.call.bottomsheet.model.CustomCallAction

@Composable
internal fun CustomAction(
    modifier: Modifier = Modifier,
    icon: Int,
    buttonTexts: CustomCallAction.ButtonTexts,
    onClick: () -> Unit,
    buttonColors: CustomCallAction.ButtonsColors? = null,
    enabled: Boolean = true,
    label: Boolean = true,
) {
    CallAction(
        modifier = modifier,
        icon = painterResource(icon),
        contentDescription = buttonTexts.contentDescription ?: buttonTexts.text ?: "",
        enabled = enabled,
        buttonText = buttonTexts.text,
        label = if (label) buttonTexts.text else null,
        buttonColor = buttonColors?.buttonColor?.let { Color(it) } ?: CallActionDefaults.ContainerColor,
        buttonContentColor = buttonColors?.buttonContentColor?.let { Color(it) } ?: CallActionDefaults.ContentColor,
//            disabledButtonColor = buttonColors.disabledButtonColor,
//            disabledButtonContentColor = buttonColors.disabledButtonContentColor,,
        onClick = onClick
    )
}