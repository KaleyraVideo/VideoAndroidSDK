package com.kaleyra.video_sdk.call.bottomsheetnew.sheetpanel

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.call.screennew.AudioAction
import com.kaleyra.video_sdk.call.screennew.CallActionUI
import com.kaleyra.video_sdk.call.screennew.ChatAction
import com.kaleyra.video_sdk.call.screennew.FileShareAction
import com.kaleyra.video_sdk.call.screennew.FlipCameraAction
import com.kaleyra.video_sdk.call.screennew.VirtualBackgroundAction
import com.kaleyra.video_sdk.call.screennew.WhiteboardAction
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun SheetPanelContent(
    items: ImmutableList<CallActionUI>,
    onItemClick: (CallActionUI) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 12.dp),
        modifier = modifier
    ) {
        items(items = items.value, key = { it.id }) {
            SheetPanelItem(
                callAction = it,
                modifier = Modifier
                    .clickable(
                        role = Role.Button,
                        onClick = { onItemClick(it) }
                    )
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun SheetPanelContentPreview() {
    KaleyraTheme {
        Surface {
            SheetPanelContent(
                items = ImmutableList(
                    listOf(
                        FlipCameraAction(),
                        AudioAction(),
                        ChatAction(notificationCount = 4),
                        FileShareAction(notificationCount = 2),
                        WhiteboardAction(notificationCount = 3),
                        VirtualBackgroundAction()
                    )
                ),
                onItemClick = { }
            )
        }
    }
}