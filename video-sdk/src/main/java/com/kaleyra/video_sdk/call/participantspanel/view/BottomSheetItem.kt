package com.kaleyra.video_sdk.call.participantspanel.view

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Immutable
internal data class BottomSheetItemUi(
    val text: String? = null,
    val iconPainter: Painter? = null,
    val contentDescription: String? = text,
    val tint: Color? = null
)

@Composable
internal fun BottomSheetRowItem(
    bottomSheetItemUi: BottomSheetItemUi,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val isDarkMode = isSystemInDarkTheme()

    Row(
        modifier = modifier
            .clickable(
                onClickLabel = bottomSheetItemUi.contentDescription,
                role = Role.Button,
                onClick = { onClick() }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomSheetItemUi.iconPainter?.let {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .fillMaxWidth(),
                painter = bottomSheetItemUi.iconPainter,
                contentDescription = null,
                tint = bottomSheetItemUi.tint ?: if (isDarkMode) Color.White else Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        bottomSheetItemUi.text?.let { text ->
            Text(
                text = text,
                fontSize = 22.sp,
                color = bottomSheetItemUi.tint ?: if (isDarkMode) Color.White else Color.Black
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun BottomSheetRowItemPreview() = KaleyraTheme {
    var onClicked by remember { mutableStateOf(false) }

    if (onClicked) Toast.makeText(LocalContext.current, "pressed", Toast.LENGTH_LONG).show()

    BottomSheetRowItem(
        bottomSheetItemUi = BottomSheetItemUi("test", painterResource(id = R.drawable.ic_kaleyra_pin), "description", tint = Color.Red),
        onClick = { onClicked = true }
    )
}
