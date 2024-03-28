package com.kaleyra.video_sdk.call.participantspanel.view

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.participantspanel.model.StreamsLayout

@Composable
internal fun StreamsLayoutSelector(
    streamsLayout: StreamsLayout,
    onLayoutClick: (streamsLayout: StreamsLayout) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Grid) it.primary else it.surfaceVariant },
                contentColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Grid) it.onPrimary else it.onSurfaceVariant }
            ),
            modifier = Modifier.weight(1f),
            onClick = { onLayoutClick(StreamsLayout.Grid) }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_grid),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.kaleyra_participants_component_grid), fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.width(14.dp))

        Button(
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Pin) it.primary else it.surfaceVariant },
                contentColor = MaterialTheme.colorScheme.let { if (streamsLayout == StreamsLayout.Pin) it.onPrimary else it.onSurfaceVariant }
            ),
            modifier = Modifier.weight(1f),
            onClick = { onLayoutClick(StreamsLayout.Pin) }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_participants_component_pin),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.kaleyra_participants_component_pin), fontWeight = FontWeight.SemiBold)
        }
    }
}