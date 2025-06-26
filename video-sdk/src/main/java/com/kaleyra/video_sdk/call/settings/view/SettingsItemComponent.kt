package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus

private const val DisabledOptionAlpha = 0.4f
private val SettingsItemComponentIconSize = 24.dp
private val SettingsItemComponentHeight = 48.dp
private val SettingsItemComponentIconSpacer = 8.dp

@Composable
fun SettingsItemComponent(
    modifier: Modifier = Modifier,
    iconPainter: Painter,
    text: String,
    subtitle: String? = null,
    isToggleable: Boolean? = null,
    testTag: String? = null,
    isSelected: Boolean,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    highlightFocusShape: Shape = RectangleShape,
) {
    val displayRadioButton = isToggleable == false
    val displaySwitch = isToggleable == true
    var isChecked by remember { mutableStateOf(isSelected) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(SettingsItemComponentHeight)
            .optionalTestTag(testTag)
            .selectable(
                selected = isSelected,
                enabled = true,
                role = when {
                    displaySwitch -> Role.Switch
                    displayRadioButton -> Role.RadioButton
                    else -> Role.Button
                },
                onClick = {
                    if (!isEnabled) return@selectable
                    if (displaySwitch) {
                        isChecked = !isChecked
                        onCheckedChange(isChecked)
                    } else {
                        isChecked = true
                        onCheckedChange(true)
                    }
                },
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .highlightOnFocus(interactionSource, highlightFocusShape)
            .then(modifier)
    ) {
        Box(modifier = Modifier.clearAndSetSemantics {}
        ) {
            Icon(
                painter = iconPainter,
                modifier = Modifier.size(SettingsItemComponentIconSize),
                contentDescription = text,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEnabled) 1f else DisabledOptionAlpha)
            )
        }
        Spacer(Modifier.size(SettingsItemComponentIconSpacer))
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.Center) {
            val bodyLargeStyle = MaterialTheme.typography.bodyLarge
            Text(
                text = text,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEnabled) 1f else DisabledOptionAlpha),
                style = if (isSelected) MaterialTheme.typography.titleMedium.copy(letterSpacing = bodyLargeStyle.letterSpacing) else bodyLargeStyle
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        when {
            displaySwitch -> {
                Switch(
                    modifier = Modifier.clearAndSetSemantics {},
                    checked = isSelected,
                    onCheckedChange = {
                        isChecked = it
                        if (isEnabled) onCheckedChange(it)
                    },
                    enabled = isEnabled,
                )
            }

            displayRadioButton -> {
                RadioButton(
                    modifier = Modifier
                        .clearAndSetSemantics {},
                    selected = isSelected,
                    onClick = {
                        isChecked = true
                        if (isEnabled) onCheckedChange(true)
                    },
                    enabled = isEnabled)
            }
        }
    }
}

private fun Modifier.optionalTestTag(tag: String?): Modifier {
    return if (tag != null) this.testTag(tag) else this
}
