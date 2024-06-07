package com.kaleyra.video_sdk.call.streamnew.view.items

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

// TODO test this
@Composable
fun ScreenShareItem(
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_stream_screenshare_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
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
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ScreenShareItemPreview() {
    KaleyraM3Theme {
        Surface {
            ScreenShareItem(
                onStopClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}