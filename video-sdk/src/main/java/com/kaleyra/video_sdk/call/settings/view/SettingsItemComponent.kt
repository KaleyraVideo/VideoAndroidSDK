package com.kaleyra.video_sdk.call.settings.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R

@Composable
fun SettingsItemComponent(
    iconPainter: Painter,
    text: String,
    subtitle: String? = null,
    isToggleable: Boolean? = null,
    testTag: String? = null,
    isSelected: Boolean,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(40.dp)) {
        Box(modifier = Modifier.clearAndSetSemantics {}) {
            Icon(
                painter = iconPainter,
                modifier = Modifier.size(24.dp),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEnabled) 1f else DisabledOptionAlpha),
            )
        }
        Spacer(Modifier.size(8.dp))
        val displayRadioButton = isToggleable == false
        val displaySwitch = isToggleable == true
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .semantics(mergeDescendants = true) {
                    contentDescription = text
                }
                .selectable(
                    selected = isSelected,
                    enabled = true,
                    role = when {
                        displaySwitch -> Role.Switch
                        displayRadioButton -> Role.RadioButton
                        else -> Role.Button
                    },
                    onClick = {
                        if (isEnabled) onCheckedChange(true)
                    }
                )
                .optionalTestTag(testTag),
            verticalArrangement = Arrangement.Center) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEnabled) 1f else DisabledOptionAlpha),
                style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge)
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
                    modifier = Modifier
                        .clearAndSetSemantics {},
                    checked = isSelected,
                    onCheckedChange = { if (isEnabled) onCheckedChange(it) },
                    enabled = isEnabled,
                )
            }

            displayRadioButton -> {
                RadioButton(modifier = Modifier
                    .padding(end = 0.dp)
                    .clearAndSetSemantics {},
                    selected = isSelected,
                    onClick = { if (isEnabled) onCheckedChange(true) },
                    enabled = isEnabled)
            }

            else -> {
                IconButton(modifier = Modifier
                    .size(24.dp)
                    .clearAndSetSemantics {},
                    enabled = isEnabled,
                    onClick = { if (isEnabled) onCheckedChange(true) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.kaleyra_f_chevron_right),
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (isEnabled) 1f else 0.4f),
                    )
                }
            }
        }
    }
}

private fun Modifier.optionalTestTag(tag: String?): Modifier {
    return if (tag != null) this.testTag(tag) else this
}