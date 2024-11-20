package com.kaleyra.video_sdk.common.snackbar.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.usermessages.model.AlertMessage
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage

@Composable
fun StackedUserMessageComponent(
    modifier: Modifier = Modifier,
    userMessages: ImmutableList<UserMessage> = ImmutableList(),
    alertMessages: ImmutableList<AlertMessage> = ImmutableList(),
    onActionClick: (UserMessage) -> Unit,
    onDismissClick: (UserMessage) -> Unit,
) {
    if (userMessages.isNotEmpty() || alertMessages.isNotEmpty()) {
        StackedSnackbar(
            modifier = modifier,
            snackbarData = ImmutableList(alertMessages.value.plus(userMessages.value)),
            onDismissClick = onDismissClick,
            onActionClick = onActionClick
        )
    }
}
