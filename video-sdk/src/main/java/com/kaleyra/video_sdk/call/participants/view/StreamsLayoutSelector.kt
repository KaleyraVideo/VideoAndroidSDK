package com.kaleyra.video_sdk.call.participants.view

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participants.model.StreamsLayout
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun StreamsLayoutSelector(
    streamsLayout: StreamsLayout,
    onLayoutClick: (streamsLayout: StreamsLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth().then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LayoutButton(
            text = stringResource(R.string.kaleyra_participants_component_auto),
            painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_auto_layout),
            containerColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Auto) it.primary else it.surfaceVariant },
            interactionSource = remember { MutableInteractionSource() },
            onClick = { onLayoutClick(StreamsLayout.Auto) }
        )
        Spacer(Modifier.width(14.dp))
        LayoutButton(
            text = stringResource(R.string.kaleyra_participants_component_mosaic),
            painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_mosaic_layout),
            containerColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Mosaic) it.primary else it.surfaceVariant },
            interactionSource = remember { MutableInteractionSource() },
            onClick = { onLayoutClick(StreamsLayout.Mosaic) }
        )
    }
}

@Composable
private fun RowScope.LayoutButton(
    text: String,
    painter: Painter,
    containerColor: Color,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
    ) {
    Button(
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColorFor(backgroundColor = containerColor)
        ),
        modifier = Modifier
            .weight(1f)
            .highlightOnFocus(interactionSource)
            .semantics {
                contentDescription = text
            },
        interactionSource = interactionSource,
        contentPadding = PaddingValues(8.dp),
        onClick = onClick
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun StreamsLayoutSelectorPreview() {
    KaleyraTheme {
        StreamsLayoutSelector(StreamsLayout.Mosaic, onLayoutClick = {})
    }
}