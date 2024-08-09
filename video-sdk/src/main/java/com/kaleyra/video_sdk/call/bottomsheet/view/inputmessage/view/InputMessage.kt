package com.kaleyra.video_sdk.call.bottomsheet.view.inputmessage.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun MicMessage(
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    InputMessage(
        modifier = modifier,
        inputPainter = if (enabled) {
            painterResource(R.drawable.ic_kaleyra_call_sheet_disable_microphone)
        } else painterResource(R.drawable.ic_kaleyra_call_sheet_enable_microphone),
        inputText = stringResource(R.string.kaleyra_call_sheet_microphone),
        stateText = if (enabled) stringResource(R.string.kaleyra_call_sheet_on) else stringResource(R.string.kaleyra_call_sheet_off)
    )
}

@Composable
internal fun CameraMessage(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    InputMessage(
        modifier = modifier,
        inputPainter = if (enabled) {
            painterResource(R.drawable.ic_kaleyra_call_sheet_disable_camera)
        } else painterResource(R.drawable.ic_kaleyra_call_sheet_enable_camera),
        inputText = stringResource(R.string.kaleyra_call_sheet_camera),
        stateText = if (enabled) stringResource(R.string.kaleyra_call_sheet_on) else stringResource(R.string.kaleyra_call_sheet_off)
    )
}

@Composable
private fun InputMessage(
    inputPainter: Painter,
    inputText: String,
    stateText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.inverseSurface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = inputPainter, contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            InputMessageText(inputText = inputText, stateText = stateText)
        }
    }
}

@Composable
internal fun MicMessageText(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    InputMessageText(
        modifier = modifier,
        inputText = stringResource(id = R.string.kaleyra_call_sheet_microphone),
        stateText = if (enabled) stringResource(id = R.string.kaleyra_call_sheet_on) else stringResource(id = R.string.kaleyra_call_sheet_off)
    )
}

@Composable
internal fun CameraMessageText(
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    InputMessageText(
        modifier = modifier,
        inputText = stringResource(id = R.string.kaleyra_call_sheet_camera),
        stateText = if (enabled) stringResource(id = R.string.kaleyra_call_sheet_on) else stringResource(id = R.string.kaleyra_call_sheet_off)
    )
}

@Composable
private fun InputMessageText(
    inputText: String,
    stateText: String,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = inputText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stateText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun MicOnInputMessagePreview() {
    KaleyraM3Theme {
        Surface {
            MicMessage(true)
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun MicOffInputMessagePreview() {
    KaleyraM3Theme {
        Surface {
            MicMessage(false)
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CameraOnInputMessagePreview() {
    KaleyraM3Theme {
        Surface {
            CameraMessage(true)
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun CameraOffInputMessagePreview() {
    KaleyraM3Theme {
        Surface {
            CameraMessage(false)
        }
    }
}
