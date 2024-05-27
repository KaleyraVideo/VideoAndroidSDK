package com.kaleyra.video_sdk.call.bottomsheetnew.streammenu

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.bottomsheetnew.sheetcontent.sheetitemslayout.SheetItemsSpacing
import com.kaleyra.video_sdk.call.callactionnew.CancelAction
import com.kaleyra.video_sdk.call.callactionnew.FullscreenAction
import com.kaleyra.video_sdk.call.callactionnew.PinAction
import com.kaleyra.video_sdk.theme.KaleyraM3Theme

@Composable
internal fun VStreamMenuContent(
    fullscreen: Boolean,
    pin: Boolean,
    onCancelClick: () -> Unit,
    onFullscreenClick: (Boolean) -> Unit,
    onPinClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(14.dp)) {
        CancelAction(
            label = true,
            onClick = onCancelClick
        )
        Spacer(modifier = Modifier.height(SheetItemsSpacing))
        FullscreenAction(
            label = true,
            fullscreen = fullscreen,
            onClick = { onFullscreenClick(fullscreen) }
        )
        Spacer(modifier = Modifier.height(SheetItemsSpacing))
        PinAction(
            label = true,
            pin = pin,
            onClick = { onPinClick(pin) }
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun VStreamMenuContentPreview() {
    KaleyraM3Theme {
        Surface {
            VStreamMenuContent(false, false, {}, {}, {})
        }
    }
}