package com.kaleyra.video_sdk.call.stream.view.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isAtLeastMediumSizeWidth
import com.kaleyra.video_sdk.common.preview.MultiConfigPreview
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
fun ActiveScreenShareIndicator(
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
    ) {
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (maxWidth.isAtLeastMediumSizeWidth()) {
                HScreenShareIndicator(
                    onStopClick = onStopClick,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                VScreenShareIndicator(onStopClick)
            }
        }
    }
}

@Composable
private fun HScreenShareIndicator(
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_stream_screenshare_message),
//            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            onClick = onStopClick
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_screen_share),
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.kaleyra_stream_screenshare_action),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun VScreenShareIndicator(
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_stream_screenshare_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            onClick = onStopClick
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_screen_share),
                contentDescription = null
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(id = R.string.kaleyra_stream_screenshare_action),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@MultiConfigPreview
@Composable
internal fun ScreenShareItemPreview() {
    KaleyraTheme {
        Surface {
            ActiveScreenShareIndicator(
                onStopClick = {},
            )
        }
    }
}