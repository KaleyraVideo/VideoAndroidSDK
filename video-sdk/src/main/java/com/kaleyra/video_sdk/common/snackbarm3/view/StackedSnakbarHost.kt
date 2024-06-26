package com.kaleyra.video_sdk.common.snackbarm3.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.snackbarm3.model.StackedSnackbarHostState
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage

@Composable
fun StackedSnackbarHost(
    hostState: StackedSnackbarHostState,
    modifier: Modifier = Modifier,
    onActionClick: (UserMessage.Action) -> Unit
) {
    val userMessages by hostState.userMessages.collectAsStateWithLifecycle(listOf())
    val alertMessage by hostState.alertMessages.collectAsStateWithLifecycle(listOf())

    StackedSnackbar(
        modifier = modifier,
        snackbarData = ImmutableList(alertMessage.plus(userMessages.toList())),
        onDismissClick = { hostState.removeUserMessage(it) },
        onActionClick = onActionClick
    )
}
