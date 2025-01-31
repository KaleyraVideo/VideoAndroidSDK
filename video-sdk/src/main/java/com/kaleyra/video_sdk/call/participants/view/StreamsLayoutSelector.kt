package com.kaleyra.video_sdk.call.participants.view

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
    onLayoutClick: (streamsLayout: StreamsLayout) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val mosaicInteractionSource = remember { MutableInteractionSource() }
        val mosaicContainerColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Mosaic) it.primary else it.surfaceVariant }
        val mosaicText = stringResource(R.string.kaleyra_participants_component_mosaic)
        Button(
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = mosaicContainerColor,
                contentColor = contentColorFor(backgroundColor = mosaicContainerColor)
            ),
            modifier = Modifier
                .weight(1f)
                .highlightOnFocus(mosaicInteractionSource)
                .semantics {
                    contentDescription = mosaicText
                },
            interactionSource = mosaicInteractionSource,
            contentPadding = PaddingValues(8.dp),
            onClick = { onLayoutClick(StreamsLayout.Mosaic) }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_grid),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = mosaicText,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(14.dp))

        val autoInteractionSource = remember { MutableInteractionSource() }
        val autoContainerColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Auto) it.primary else it.surfaceVariant }
        val autoText = stringResource(R.string.kaleyra_participants_component_auto)
        Button(
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = autoContainerColor,
                contentColor = contentColorFor(backgroundColor = autoContainerColor)
            ),
            modifier = Modifier
                .weight(1f)
                .highlightOnFocus(autoInteractionSource)
                .semantics {
                    contentDescription = autoText
                },
            interactionSource = autoInteractionSource,
            contentPadding = PaddingValues(8.dp),
            onClick = { onLayoutClick(StreamsLayout.Auto) }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_pin),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = autoText,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1
            )
        }
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